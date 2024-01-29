package my.app.imdbtop.model

enum class DataSetType {
    CREW,
    EPISODE,
    NAME_BASICS,
    PRINCIPALS,
    RATINGS,
    TITLE,
    TITLE_BASICS
}

//
//@Configuration
//class KafkaTopologyConfig(
//    kafkaTopicsProperties: KafkaTopicsProperties,
//) : Log() {
//
//    private val invalidStreamSuffix = "_invalid"
//    private val skippedStreamSuffix = "_skipped"
//    private val validStreamSuffix = "_valid"
//
//    private val usTopicConfig = kafkaTopicsProperties.getUSTopicConfig()
//
//    @Bean
//    fun topology(): Topology {
//        val topology = with(StreamsBuilder()) {
//            val contactDetailsStream = getValidStream(
//                ContactDetailsSubmitted,
//                { _, v -> isValidContactDetailsSubmittedEvent(v) },
//                { _, v -> shouldSkipContactDetailsSubmittedEvent(v) }
//            )
//
//            val usQuestionnaireStream = getValidStream(
//                UsQuestionnaire,
//                { _, v -> isValidUsQuestionnaireEvent(v) },
//                { _, v -> shouldSkipUsQuestionnaireEvent(v) }
//            )
//
//            contactDetailsStream
//                .join(
//                    usQuestionnaireStream,
//                    { cdsV, usqV -> CreateAction.fromEvents(cdsV, usqV) },
//                    JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(contactDetailsRetentionTimeMins)),
//                    StreamJoined.with(Serdes.String(), WrappedEnvelopeSerdes(), WrappedEnvelopeSerdes())
//                        .withName("userId-keyed")
//                        .withStoreName("join-store")
//                )
//                .peek { _, v ->
//                    logger.info("Created CREATE action after joining for user ${v.userId}")
//                    datadog.countJoined("createAction", v.userId)
//                }
//                .to(usTopicConfig.output, Produced.with(Serdes.String(), CreateActionSerdes()))
//
//            return@with build()
//        }
//
//        logger.info(topology.describe().toString())
//
//        return topology
//    }
//
//    private fun StreamsBuilder.getValidStream(
//        schemedRecordCompanion: SchemedRecordCompanion<*>,
//        isValid: (key: String?, value: SchemaWrapper<Envelope>) -> Boolean,
//        shouldBeSkipped: (key: String?, value: SchemaWrapper<Envelope>) -> Boolean
//    ): KStream<String, SchemaWrapper<Envelope>> {
//        val branches = stream(
//            usTopicConfig.input,
//            Consumed.with(Serdes.String(), WrappedEnvelopeSerdes())
//                .withTimestampExtractor(RecordTimestampExtractor())
//        )
//            .filter { _, v -> v.data.getEnvelopedSchema() == schemedRecordCompanion.schema.asString() }
//            .split(Named.`as`(schemedRecordCompanion.name))
//            .branch(
//                { k, v -> !isValid(k, v) },
//                Branched.`as`(invalidStreamSuffix)
//            )
//            .branch(
//                { k, v -> shouldBeSkipped(k, v) },
//                Branched.`as`(skippedStreamSuffix)
//            )
//            .branch(
//                { k, v -> isValid(k, v) && !shouldBeSkipped(k, v) },
//                Branched.`as`(validStreamSuffix)
//            ).noDefaultBranch()
//
//        sendInvalidToDlq(schemedRecordCompanion.name, branches["${schemedRecordCompanion.name}$invalidStreamSuffix"]!!)
//        countSkipped(schemedRecordCompanion.name, branches["${schemedRecordCompanion.name}$skippedStreamSuffix"]!!)
//        return setUserIdAsKeyAndCountValid(schemedRecordCompanion.name, branches["${schemedRecordCompanion.name}$validStreamSuffix"]!!)
//    }
//
//    class RecordTimestampExtractor : TimestampExtractor {
//        override fun extract(record: ConsumerRecord<Any, Any>?, partitionTime: Long): Long {
//            return record?.timestamp() ?: partitionTime
//        }
//    }
//
//    private fun setUserIdAsKeyAndCountValid(
//        eventName: String,
//        validStream: KStream<String, SchemaWrapper<Envelope>>
//    ): KStream<String, SchemaWrapper<Envelope>> =
//        validStream
//            .peek { _, validVal ->
//                val eventId = validVal.data.getEventId()
//                logger.info("Preparing $eventName event for joining, id $eventId, userId ${validVal.data.getContext<IdentityContext>()!!.userId}")
//                datadog.countValid(eventName, eventId)
//            }
//            .map { _, v -> KeyValue(v.data.getContext<IdentityContext>()!!.userId!!, v) }
//
//    private fun countSkipped(
//        eventName: String,
//        skippedStream: KStream<String, SchemaWrapper<Envelope>>
//    ) =
//        skippedStream
//            .peek { _, invalidVal ->
//                val eventId = invalidVal.data.getEventId()
//                logger.info("Skipping $eventName event, id $eventId")
//                datadog.countSkipped(eventName, eventId)
//            }
//
//    private fun sendInvalidToDlq(
//        eventName: String,
//        invalidStream: KStream<String, SchemaWrapper<Envelope>>
//    ) =
//        invalidStream
//            .peek { _, invalidVal ->
//                val eventId = invalidVal.data.getEventId()
//                logger.error("Sending invalid $eventName event to DLQ, id $eventId")
//                datadog.countInvalid(eventName, eventId)
//            }
//            .to(usTopicConfig.dlq, Produced.with(Serdes.String(), WrappedEnvelopeSerdes()))
//
//    private fun isValidUsQuestionnaireEvent(v: SchemaWrapper<Envelope>) = v.data.getContext<IdentityContext>()?.userId != null
//
//    private fun shouldSkipUsQuestionnaireEvent(v: SchemaWrapper<Envelope>) =
//        skippedUsQuestionnaireStepNames.contains(v.data.getEnvelopedEvent<UsQuestionnaire>()?.formStepName)
//
//    private fun isValidContactDetailsSubmittedEvent(v: SchemaWrapper<Envelope>) =
//        (
//                v.data.getEnvelopedEvent<ContactDetailsSubmitted>()?.phoneNumber != null &&
//                        v.data.getContext<IdentityContext>()?.userId != null &&
//                        v.data.getContext<IdentityContext>()?.humansCustomerAccountId != null &&
//                        v.data.getContext<ServiceChannelContext>() != null
//                )
//
//    private fun shouldSkipContactDetailsSubmittedEvent(v: SchemaWrapper<Envelope>) =
//        skippedServiceChannels.contains(v.data.getContext<ServiceChannelContext>()?.serviceChannelIdentifier)
//}
//}
