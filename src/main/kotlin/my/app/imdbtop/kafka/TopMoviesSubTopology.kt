package my.app.imdbtop.kafka

import my.app.imdbtop.model.AggregatedMovies
import my.app.imdbtop.model.DataSetType.RATINGS
import my.app.imdbtop.model.DataSetType.TITLE_BASICS
import my.app.imdbtop.model.TitleWithRatings
import my.app.imdbtop.model.TopTitle
import my.app.imdbtop.model.datasets.Ratings
import my.app.imdbtop.model.datasets.TitleBasics
import my.app.imdbtop.serde.Serde.aggregatedMoviesSerde
import my.app.imdbtop.serde.Serde.ratingsSerde
import my.app.imdbtop.serde.Serde.titleBasicsSerde
import my.app.imdbtop.serde.Serde.titleWithRatingsSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class TopMoviesSubTopology(
    private val kafkaTopicsProperties: KafkaTopicsProperties,
    @Value("\${kafka.outputTopics.top10.rated.movies}") val topTenMoviesTopic: String,
    @Value("\${rating-rules.min-num-votes}") val minNumOfVotes: Long,
    @Value("\${rating-rules.time-window-mins}") val timeWindowMins: Long,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun buildTop10MoviesSubTopology(builder: StreamsBuilder) =
        ratingsWithMinVotesAndTitleIdKey(builder)
            .join(
                titleBasicsWithTitleIdKey(builder),
                { ratings: Ratings, titleBasics: TitleBasics -> TitleWithRatings(
                    titleBasics.titleId,
                    titleBasics.primaryTitle,
                    titleBasics.originalTitle,
                    titleBasics.titleType,
                    ratings.averageRating,
                    ratings.numVotes
                ) },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(timeWindowMins)),
                StreamJoined.with(Serdes.String(), ratingsSerde(), titleBasicsSerde())
                    .withName("ratings-and-title-basics-join")
                    .withStoreName("ratings-and-title-basics-join-store")
            )
            .groupByKey(Grouped.with(Serdes.String(), titleWithRatingsSerde()))
            .aggregate(
                { AggregatedMovies() },
                { _, titleWithRatings, aggregate -> calculateTop10(aggregate, titleWithRatings) },
                Materialized.with(Serdes.String(), aggregatedMoviesSerde())
            )
            .filter { _, v -> v.movies.first().titleType == "movie" }
            .toStream()
            .peek { _, v -> logger.info("Top movies at the moment: $v") }
            .to(topTenMoviesTopic)

    private fun ratingsWithMinVotesAndTitleIdKey(builder: StreamsBuilder): KStream<String, Ratings> =
        builder.stream(
            kafkaTopicsProperties.getTopic(RATINGS).name,
            Consumed.with(Serdes.String(), ratingsSerde())
        ).filter { _, v -> v.numVotes > minNumOfVotes }
            .map { _, v -> KeyValue(v.titleId, v) }

    private fun titleBasicsWithTitleIdKey(builder: StreamsBuilder): KStream<String, TitleBasics> =
        builder.stream(
            kafkaTopicsProperties.getTopic(TITLE_BASICS).name,
            Consumed.with(Serdes.String(), titleBasicsSerde())
        ).map { _, v -> KeyValue(v.titleId, v) }

    private fun calculateTop10(aggregate: AggregatedMovies, titleWithRatings: TitleWithRatings): AggregatedMovies {
        val totalCount = aggregate.totalMoviesCount + 1
        val totalNumVotes = aggregate.totalVotingSum + titleWithRatings.numVotes
        val avgNumVotes = totalNumVotes / totalCount
        val potentialTopMovie = TopTitle(
            titleWithRatings.titleId,
            titleWithRatings.primaryTitle,
            titleWithRatings.originalTitle,
            titleWithRatings.titleType,
            (titleWithRatings.numVotes / avgNumVotes) * titleWithRatings.averageRating
        )
        return if (aggregate.movies.size < 10) {
            AggregatedMovies(
                aggregate.movies + setOf(potentialTopMovie),
                totalCount,
                totalNumVotes
            )
        } else {
            val lastMovie = aggregate.movies.minBy { it.calculatedRating }
            if (lastMovie.calculatedRating < potentialTopMovie.calculatedRating) {
                AggregatedMovies(
                    aggregate.movies - setOf(lastMovie) + setOf(potentialTopMovie),
                    totalCount,
                    totalNumVotes
                )
            } else aggregate
        }
    }

}
