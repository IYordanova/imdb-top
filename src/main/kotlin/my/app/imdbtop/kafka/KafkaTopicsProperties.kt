package my.app.imdbtop.kafka

import my.app.imdbtop.model.DataSetType
import my.app.imdbtop.model.Topic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties


@EnableConfigurationProperties
@ConfigurationProperties(prefix = "kafka")
data class KafkaTopicsProperties(
    val inputTopics: List<Topic>
) {
    fun getTopic(dataSetType: DataSetType) = inputTopics.first { it.dataSetType == dataSetType }
}
