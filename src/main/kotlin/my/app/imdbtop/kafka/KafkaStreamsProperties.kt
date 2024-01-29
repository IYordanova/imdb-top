package my.app.imdbtop.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*


@ConfigurationProperties(prefix = "kafka.streams")
class KafkaStreamsProperties: Properties()
