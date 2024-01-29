package my.app.imdbtop

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import my.app.imdbtop.kafka.KafkaTopicsProperties
import my.app.imdbtop.kafka.KafkaTopologyConfig
import my.app.imdbtop.kafka.PeopleSubTopology
import my.app.imdbtop.kafka.TopMoviesSubTopology
import my.app.imdbtop.model.AggregatedMovies
import my.app.imdbtop.model.AggregatedPrincipals
import my.app.imdbtop.model.DataSetType
import my.app.imdbtop.model.TitleWithCreditedPeople
import my.app.imdbtop.model.datasets.NameBasics
import my.app.imdbtop.model.datasets.Principal
import my.app.imdbtop.model.datasets.Ratings
import my.app.imdbtop.model.datasets.TitleBasics
import my.app.imdbtop.serde.Serde.aggregatedMoviesSerde
import my.app.imdbtop.serde.Serde.aggregatedPrincipalsSerde
import my.app.imdbtop.serde.Serde.nameBasicsSerde
import my.app.imdbtop.serde.Serde.principalSerde
import my.app.imdbtop.serde.Serde.ratingsSerde
import my.app.imdbtop.serde.Serde.titleBasicsSerde
import my.app.imdbtop.serde.Serde.titleWithCreditedPeopleSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.random.Random


@SpringBootTest(
    classes = [
        KafkaTopologyConfig::class,
        KafkaTopicsProperties::class,
        PeopleSubTopology::class,
        TopMoviesSubTopology::class
    ]
)
@ActiveProfiles("test")
class KafkaTopologyTest(
    @Autowired private val topology: Topology,
    @Autowired private val kafkaTopicsProperties: KafkaTopicsProperties,
    @Value("\${kafka.outputTopics.top10.rated.movies}") topTenOutputTopicName: String,
    @Value("\${kafka.outputTopics.top10.rated.movies.with.people}") topTenMoviesPeopleOutputTopicName: String,
    @Value("\${kafka.outputTopics.principals.names.per.movie}") principalsNamesPerMovieTopicName: String,
    @Value("\${rating-rules.min-num-votes}") val minNumOfVotes: Long,
) {

    private val driver = TopologyTestDriver(
        topology,
        Properties().apply {
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java.name)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray()::class.java.name)
        }
    )

    private val ratingsTopic = driver.createInputTopic(
        kafkaTopicsProperties.getTopic(DataSetType.RATINGS).name,
        Serdes.String().serializer(),
        ratingsSerde().serializer()
    )
    private val titleBasicsTopic = driver.createInputTopic(
        kafkaTopicsProperties.getTopic(DataSetType.TITLE_BASICS).name,
        Serdes.String().serializer(),
        titleBasicsSerde().serializer()
    )
    private val nameBasicsTopic = driver.createInputTopic(
        kafkaTopicsProperties.getTopic(DataSetType.NAME_BASICS).name,
        Serdes.String().serializer(),
        nameBasicsSerde().serializer()
    )
    private val principalsTopic = driver.createInputTopic(
        kafkaTopicsProperties.getTopic(DataSetType.PRINCIPALS).name,
        Serdes.String().serializer(),
        principalSerde().serializer()
    )

    private val topTenOutputTopic = driver.createOutputTopic(
        topTenOutputTopicName,
        Serdes.String().deserializer(),
        aggregatedMoviesSerde().deserializer()
    )
    private val topTenMoviesPeopleOutputTopic = driver.createOutputTopic(
        topTenMoviesPeopleOutputTopicName,
        Serdes.String().deserializer(),
        titleWithCreditedPeopleSerde().deserializer()
    )

    private val principalsNamesPerMovieTopic = driver.createOutputTopic(
        principalsNamesPerMovieTopicName,
        Serdes.String().deserializer(),
        aggregatedPrincipalsSerde().deserializer()
    )

    @AfterEach
    fun close() {
        try {
            driver.close()
        } catch (e: Exception) {
            // ignored
        }
    }

    private fun createTitleBasics(titleId: String, titleType: String) = TitleBasics(
        titleId,
        titleType,
        "$titleId primary",
        "$titleId original",
        false,
        Random.nextInt(1950, 2024).toString(),
        if (Random.nextBoolean()) Random.nextInt(1950, 2024).toString() else "\\N",
        Random.nextLong(30, 210),
        listOf("genre1")
    )

    private fun createPrincipal(titleId: String, nameId: String) = Principal(
        titleId,
        Random.nextInt(1, 10),
        nameId,
        "$nameId category",
        "$nameId job",
        "$nameId characters",
    )


    private fun createNameBasics(nameId: String) = NameBasics(
        nameId,
        "$nameId Name",
        Random.nextInt(1950, 2024).toString(),
        Random.nextInt(1950, 2024).toString(),
        listOf(),
        listOf()
    )

    @Test
    fun `test top 10 movies`() {
        val titleBasics = mutableListOf<TitleBasics>()
        val ratings = mutableListOf<Ratings>()
        (1..100).forEach {
            titleBasics.add(createTitleBasics("title$it", if (it / 2 == 0) "short" else "movie"))
            ratings.add(Ratings("title$it", Random.nextDouble(1.0, 5.0), Random.nextLong(300, 1000)))
        }

        ratings.forEach { ratingsTopic.pipeInput(TestRecord(null, it)) }
        titleBasics.forEach { titleBasicsTopic.pipeInput(TestRecord(null, it)) }

        val expectedTitleNums =
            ratings.count { it.numVotes > minNumOfVotes } - titleBasics.count { it.titleType != "movie" }

        val topRecords = topTenOutputTopic.readRecordsToList()
        assertEquals(expectedTitleNums, topRecords.size)
    }


    @Test
    fun `top 10 movies with people`() = runBlocking {
        val titleBasics = mutableListOf<TitleBasics>()
        val ratings = mutableListOf<Ratings>()
        val principals = mutableListOf<Principal>()
        val nameBasics = mutableListOf<NameBasics>()
        (1..5).forEach {
            titleBasics.add(createTitleBasics("title$it", if (it / 2 == 0) "short" else "movie"))
            ratings.add(Ratings("title$it", Random.nextDouble(1.0, 5.0), Random.nextLong(300, 1000)))
            (0..Random.nextInt(1, 3)).forEach { i ->
                principals.add(createPrincipal("title$it", "name$i _title$it"))
                principals.add(createPrincipal("title$it", "name$i _title$it"))
                nameBasics.add(createNameBasics("name$i _title$it"))
            }
        }

        val jobs = setOf(
            launch { principals.forEach { principalsTopic.pipeInput(TestRecord(null, it)) } },
            launch { nameBasics.forEach { nameBasicsTopic.pipeInput(TestRecord(null, it)) } },
        )
        ratings.forEach { ratingsTopic.pipeInput(TestRecord(null, it)) }
        titleBasics.forEach { titleBasicsTopic.pipeInput(TestRecord(null, it)) }
        jobs.joinAll()

        val expectedTitleNums =
            ratings.count { it.numVotes > minNumOfVotes } - titleBasics.count { it.titleType != "movie" }

        val topRecords = topTenOutputTopic.readRecordsToList()
        val aggregatedPeopleRecords = principalsNamesPerMovieTopic.readRecordsToList()

        val topWithPeopleRecords = topTenMoviesPeopleOutputTopic.readRecordsToList()

        println(topRecords)
        println(aggregatedPeopleRecords)
        println(topWithPeopleRecords)
    }

    @Test
    fun `test average num of votes is correctly calculated`() {
    }

//    @Test
//    fun `test average num of votes is correctly calculated`() {
//        val ratings = listOf(
//            TestRecord("title1", Ratings("title1", 1.0, 10)), // 10
//            TestRecord("title3", Ratings("title3", 2.0, 30)), // (10+30)/2 = 40/2 = 20
//            TestRecord("title1", Ratings("title1", 3.0, 11)), // (40+11)/3 = 51/3 = 17
//            TestRecord("title1", Ratings("title1", 5.0, 12)), // (51+12)/4 = 63/4 = 15.75
//            TestRecord("title3", Ratings("title3", 2.0, 31)), // (63+31)/5 = 94/5 = 18.8
//            TestRecord("title2", Ratings("title2", 4.5, 20))  // (94+20)/6 = 114/6 = 19.0
//        )
//
//        ratings.forEach { ratingsTopic.pipeInput(it) }
//
//        val avgRecords = topicShouldHaveNRecords(avgVotesTopic, ratings.size)
//        assertEquals(avgRecords.map { it.value }, listOf(10.0, 20.0, 17.0, 15.75, 18.8, 19.0))
//    }

    private fun topicShouldBeEmpty(topic: TestOutputTopic<String, *>) = topicShouldHaveNRecords(topic, 0)

    private fun topicShouldHaveNRecords(
        topic: TestOutputTopic<String, *>,
        expectedNum: Int,
        message: String? = null
    ): List<TestRecord<String, *>> {
        val records = readRecords(topic)
        assertEquals(
            expectedNum,
            records.size,
            "Should have written $expectedNum record(s) to $topic. $message"
        )
        return records
    }

    private fun readRecords(topic: TestOutputTopic<String, *>): List<TestRecord<String, *>> {
        return topic.readRecordsToList()
    }
}
