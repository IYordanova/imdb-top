package my.app.imdbtop.kafka

import my.app.imdbtop.model.AggregatedPrincipals
import my.app.imdbtop.model.DataSetType
import my.app.imdbtop.model.PrincipalWithName
import my.app.imdbtop.model.datasets.NameBasics
import my.app.imdbtop.model.datasets.Principal
import my.app.imdbtop.serde.Serde.aggregatedPrincipalsSerde
import my.app.imdbtop.serde.Serde.nameBasicsSerde
import my.app.imdbtop.serde.Serde.principalSerde
import my.app.imdbtop.serde.Serde.principalWithNameSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class PeopleSubTopology(
    private val kafkaTopicsProperties: KafkaTopicsProperties,
    @Value("\${kafka.outputTopics.principals.names.per.movie}") private val principalsNamesPerMovieTopic: String,
    @Value("\${rating-rules.time-window-mins}") private val timeWindowMins: Long,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun buildPrincipalWithNamesAndTitleIdKey(builder: StreamsBuilder) =
        principalWithNameId(builder)
            .join(
                nameBasicsWithNameId(builder),
                { principal: Principal, nameBasics: NameBasics -> PrincipalWithName(
                    principal.titleId,
                    principal.nameId,
                    principal.job,
                    nameBasics.primaryName
                ) },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(timeWindowMins)),
                StreamJoined.with(Serdes.String(), principalSerde(), nameBasicsSerde())
                    .withName("principal-and-name-basics-join")
                    .withStoreName("principal-and-name-basics-join-store")
            )
            .map { _, value -> KeyValue(value.titleId, value) }
            .groupByKey(Grouped.with(Serdes.String(), principalWithNameSerde()))
            .aggregate(
                { AggregatedPrincipals() },
                { _, principalsWithNames, aggregate: AggregatedPrincipals ->
                    AggregatedPrincipals(
                        aggregate.principalPrimaryNames + listOf(principalsWithNames.primaryName),
                        principalsWithNames.titleId
                    )
                },
                Materialized.with(Serdes.String(), aggregatedPrincipalsSerde())
            )
            .toStream()
//            .process(getProcessorSupplier())
//            .groupByKey(Grouped.with(Serdes.String(), aggregatedPrincipalsSerde()))
//            .reduce { _, value2 -> value2 }
//            .toStream()
            .peek { _, v -> logger.info("Principal with Names: $v") }
            .to(principalsNamesPerMovieTopic)

    private fun nameBasicsWithNameId(builder: StreamsBuilder): KStream<String, NameBasics> =
        builder.stream(
            kafkaTopicsProperties.getTopic(DataSetType.NAME_BASICS).name,
            Consumed.with(Serdes.String(), nameBasicsSerde())
        ).map { _, v -> KeyValue(v.nameId, v) }

    private fun principalWithNameId(builder: StreamsBuilder): KStream<String, Principal> =
        builder.stream(
            kafkaTopicsProperties.getTopic(DataSetType.PRINCIPALS).name,
            Consumed.with(Serdes.String(), principalSerde())
        ).map { _, v -> KeyValue(v.nameId, v) }

//    private fun getProcessorSupplier() =
//        object : ProcessorSupplier<String, AggregatedPrincipals, String, AggregatedPrincipals> {
//            val STORE_NAME = "PeopleStore"
//            val storeBuilder = Stores.keyValueStoreBuilder(
//                Stores.persistentKeyValueStore(STORE_NAME),
//                Serdes.String(),
//                aggregatedPrincipalsSerde()
//            ).withCachingEnabled()
//
//            override fun get() = object : Processor<String, AggregatedPrincipals, String, AggregatedPrincipals> {
//                private lateinit var context: ProcessorContext<String, AggregatedPrincipals>
//                private lateinit var store: KeyValueStore<String, AggregatedPrincipals>
//
//                override fun init(context: ProcessorContext<String, AggregatedPrincipals>) {
//                    super.init(context)
//                    this.context = context
//                    this.store = context.getStateStore(STORE_NAME)
//                }
//
//                override fun process(record: Record<String, AggregatedPrincipals>) {
//                    val principals = store.get(record.key())
//                    if (principals == null) {
//
//                    }
//                }
//            }
//
//            override fun stores() = setOf(storeBuilder)
//        }

}
