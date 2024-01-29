//package my.app.imdbtop
//
//import my.app.imdbtop.kafka.KafkaTopicsProperties
//import org.apache.kafka.clients.admin.AdminClient
//import org.apache.kafka.clients.admin.NewTopic
//import org.apache.kafka.clients.consumer.KafkaConsumer
//import org.apache.kafka.clients.producer.KafkaProducer
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.streams.KafkaStreams
//import org.apache.kafka.streams.Topology
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import org.testcontainers.containers.KafkaContainer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
//import org.testcontainers.lifecycle.Startables
//import org.testcontainers.utility.DockerImageName
//import java.time.Duration
//import java.time.Instant
//import java.util.Properties
//import java.util.UUID
//
//
//@SpringBootTest(
//    properties = [
//        "contact_details_submitted.retention.time.mins=5"
//    ]
//)
//@ActiveProfiles("test-it")
//@Testcontainers
//class KafkaTopologyIT(
//    @Autowired kafkaTopicsProperties: KafkaTopicsProperties,
//    @Autowired private val topology: Topology,
//    @Value("\${kafka.outputTopics.top10.rated.movies}") topTenOutputTopicName: String,
//) {
//
//    companion object {
//        @Container
//        private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
//
//        @JvmStatic
//        @AfterAll
//        fun afterAll() {
//            kafka.close()
//        }
//    }
//
//    init {
//        Startables.deepStart(kafka).join()
//    }
//
//    private lateinit var producer: KafkaProducer<String, String>
//    private lateinit var consumerUsDomainEventsDlq: KafkaConsumer<String, String>
//    private lateinit var consumerUsFcActions: KafkaConsumer<String, String>
//
//    init {
//        val kafkaCommonProps = Properties().apply {
//            put("application.id", "full-contact-transformers-test-app")
//            put("bootstrap.servers", kafka.bootstrapServers)
//            put("enable.auto.commit", "false")
//            put("auto.offset.reset", "earliest")
//            put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
//            put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
//        }
//
//        val kafkaAdmin = AdminClient.create(kafkaCommonProps)
//        val topicsToCreate = kafkaTopicsProperties.inputTopics
//            .map { NewTopic(it.name, 1, 1) } + listOf(NewTopic(topTenOutputTopicName, 1, 1) )
//        kafkaAdmin.createTopics(topicsToCreate)
//
//        producer = KafkaProducer(
//            Properties().apply {
//                put("bootstrap.servers", kafka.bootstrapServers)
//                put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//                put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//            }
//        )
//
//        consumerUsDomainEventsDlq = KafkaConsumer(
//            kafkaCommonProps + Properties().apply {
//                put("group.id", "fc-transformers-test-us-dedql-group")
//            }
//        )
//        consumerUsDomainEventsDlq.subscribe(listOf(usTopicConfig.dlq))
//
//        consumerUsFcActions = KafkaConsumer(
//            kafkaCommonProps + Properties().apply {
//                put("group.id", "fc-transformers-test-us-act-group")
//            }
//        )
//        consumerUsFcActions.subscribe(listOf(usTopicConfig.output))
//    }
//
//    @Test
//    fun `contact_details_submitted gets joined to us_questionnaire only within the configured window of time`() {
//        val userId = UUID.randomUUID().toString()
//        val cdsEvent = generateContactDetailsSubmittedEvent(userId)
//
//        val writtenRecord = Serdes.extendedMapper.asString(cdsEvent)
//        producer.send(
//            ProducerRecord(
//                usTopicConfig.input,
//                0,
//                Instant.now().toEpochMilli(),
//                "someOtherKey",
//                writtenRecord
//            )
//        )
//
//        val numUsQEvents = 10
//        (1..numUsQEvents).forEach {
//            val usQEvent = generateUSQuestionnaireEvent(userId = userId, formStepName = "formStep$it")
//            producer.send(
//                ProducerRecord(
//                    usTopicConfig.input,
//                    0,
//                    Instant.now()
//                        .toEpochMilli() + it * 60 * 1000, // with retention set to 5 mins, and increasing timestamp with a min on each, we should expect 4 actions
//                    "key$it",
//                    Serdes.extendedMapper.asString(usQEvent)
//                )
//            )
//        }
//
//        val actionsCreated = mutableMapOf<String, MutableList<String>>()
//        val failedEvents = mutableMapOf<String, MutableList<String>>()
//        val start = System.currentTimeMillis()
//        do {
//            val records = consumerUsFcActions.poll(Duration.ofSeconds(1))
//            records.forEach {
//                actionsCreated.putIfAbsent(it.key(), mutableListOf())
//                actionsCreated[it.key()]!!.add(it.value())
//            }
//
//            consumerUsDomainEventsDlq.poll(Duration.ofSeconds(1)).forEach {
//                failedEvents.putIfAbsent(it.key(), mutableListOf())
//                failedEvents[it.key()]!!.add(it.value())
//            }
//        } while ((System.currentTimeMillis() - start) < 2 * 60 * 1000 && (actionsCreated[userId] == null || actionsCreated[userId]!!.size < 4))
//
//        assertEquals(0, failedEvents.size)
//        assertEquals(1, actionsCreated.size)
//        assertEquals(4, actionsCreated[userId]!!.toSet().size)
//    }
//}
