package my.app.imdbtop

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import my.app.imdbtop.kafka.KafkaStreamsProperties
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ConfigurationPropertiesScan
class ImdbTopApplication (
	private val topology: Topology,
	private val kafkaStreamsProperties: KafkaStreamsProperties
){
	private lateinit var streams: KafkaStreams

	@PostConstruct
	fun startStream(){
		streams = KafkaStreams(topology, kafkaStreamsProperties)
	}

	@PreDestroy
	fun stopStream(){
		streams.close()
	}

}

fun main(args: Array<String>) {
	runApplication<ImdbTopApplication>(*args)
}
