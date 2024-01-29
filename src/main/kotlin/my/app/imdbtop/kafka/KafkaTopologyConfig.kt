package my.app.imdbtop.kafka

import my.app.imdbtop.model.AggregatedPrincipals
import my.app.imdbtop.model.TitleWithCreditedPeople
import my.app.imdbtop.model.TopTitle
import my.app.imdbtop.serde.Serde.aggregatedMoviesSerde
import my.app.imdbtop.serde.Serde.aggregatedPrincipalsSerde
import my.app.imdbtop.serde.Serde.titleWithCreditedPeopleSerde
import my.app.imdbtop.serde.Serde.topTitleSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.StreamJoined
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


@Configuration
class KafkaTopologyConfig(
    private val peopleSubTopology: PeopleSubTopology,
    private val topMoviesSubTopology: TopMoviesSubTopology,
    @Value("\${kafka.outputTopics.top10.rated.movies}") val topTenMoviesTopic: String,
    @Value("\${kafka.outputTopics.principals.names.per.movie}") private val principalsNamesPerMovieTopic: String,
    @Value("\${kafka.outputTopics.top10.rated.movies.with.people}") val topTenMoviesPeopleOutputTopic: String,
    @Value("\${rating-rules.time-window-mins}") val timeWindowMins: Long,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Bean
    fun topology(): Topology {
        val topology = with(StreamsBuilder()) {
            topMoviesSubTopology.buildTop10MoviesSubTopology(this)
            peopleSubTopology.buildPrincipalWithNamesAndTitleIdKey(this)
            combine(this)
            return@with build()
        }
        logger.info(topology.describe().toString())
        return topology
    }

    private fun combine(builder: StreamsBuilder) =
        builder.stream(
            topTenMoviesTopic,
            Consumed.with(Serdes.String(), aggregatedMoviesSerde())
        )
            .flatMap { _, value -> value.movies.map { KeyValue(it.titleId, it) } }
            .join(
                builder.stream(
                    principalsNamesPerMovieTopic,
                    Consumed.with(Serdes.String(), aggregatedPrincipalsSerde())
                ),
                { topMovie: TopTitle, aggregatedPrincipals: AggregatedPrincipals ->
                    TitleWithCreditedPeople(
                        topMovie.titleId,
                        setOf(topMovie.primaryTitle, topMovie.originalTitle),
                        aggregatedPrincipals.principalPrimaryNames
                    )
                },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(timeWindowMins)),
                StreamJoined.with(Serdes.String(), topTitleSerde(), aggregatedPrincipalsSerde())
                    .withName("aggregated-movie-with-people-join")
                    .withStoreName("aggregated-movie-with-people-join-store")
            )
            .to(topTenMoviesPeopleOutputTopic, Produced.with(Serdes.String(), titleWithCreditedPeopleSerde()))


}
