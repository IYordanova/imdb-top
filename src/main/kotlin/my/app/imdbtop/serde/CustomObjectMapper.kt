package my.app.imdbtop.serde

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

object CustomObjectMapper {
    val objectMapper = Jackson2ObjectMapperBuilder()
        .failOnUnknownProperties(false)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .modules(
            KotlinModule.Builder().build(),
            Jdk8Module(),
            ParameterNamesModule()
        ).build<ObjectMapper>()

    inline fun <reified T> toString(t: T): String = objectMapper.writeValueAsString(t)

    inline fun <reified T> toByteArray(t: T): ByteArray = toString(t).toByteArray()

    inline fun <reified T> fromString(s: String): T = fromByteArray(s.toByteArray())

    inline fun <reified T> fromByteArray(s: ByteArray): T = objectMapper.readValue(s, object : TypeReference<T>() {})
}