package my.app.imdbtop.serde

import my.app.imdbtop.model.*
import my.app.imdbtop.model.datasets.*
import my.app.imdbtop.serde.CustomObjectMapper.fromByteArray
import my.app.imdbtop.serde.CustomObjectMapper.toByteArray
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

object Serde {
    fun crewSerde() = object : Serde<Crew> {
        override fun serializer() = Serializer<Crew> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<Crew> { _, s -> fromByteArray(s) }
    }

    fun episodeSerde() = object : Serde<Episode> {
        override fun serializer() = Serializer<Episode> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<Episode> { _, s -> fromByteArray(s) }
    }

    fun nameBasicsSerde() = object : Serde<NameBasics> {
        override fun serializer() = Serializer<NameBasics> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<NameBasics> { _, s -> fromByteArray(s) }
    }

    fun principalSerde() = object : Serde<Principal> {
        override fun serializer() = Serializer<Principal> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<Principal> { _, s -> fromByteArray(s) }
    }

    fun ratingsSerde() = object : Serde<Ratings> {
        override fun serializer() = Serializer<Ratings> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<Ratings> { _, s -> fromByteArray(s) }
    }

    fun titleBasicsSerde() = object : Serde<TitleBasics> {
        override fun serializer() = Serializer<TitleBasics> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<TitleBasics> { _, s -> fromByteArray(s) }
    }

    fun titleSerde() = object : Serde<Title> {
        override fun serializer() = Serializer<Title> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<Title> { _, s -> fromByteArray(s) }
    }

    fun titleWithRatingsSerde() = object : Serde<TitleWithRatings> {
        override fun serializer() = Serializer<TitleWithRatings> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<TitleWithRatings> { _, s -> fromByteArray(s) }
    }

    fun principalWithNameSerde() = object : Serde<PrincipalWithName> {
        override fun serializer() = Serializer<PrincipalWithName> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<PrincipalWithName> { _, s -> fromByteArray(s) }
    }

    fun aggregatedMoviesSerde() = object : Serde<AggregatedMovies> {
        override fun serializer() = Serializer<AggregatedMovies> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<AggregatedMovies> { _, s -> fromByteArray(s) }
    }

    fun aggregatedPrincipalsSerde() = object : Serde<AggregatedPrincipals> {
        override fun serializer() = Serializer<AggregatedPrincipals> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<AggregatedPrincipals> { _, s -> fromByteArray(s) }
    }

    fun topTitleSerde() = object : Serde<TopTitle> {
        override fun serializer() = Serializer<TopTitle> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<TopTitle> { _, s -> fromByteArray(s) }
    }

    fun titleWithCreditedPeopleSerde() = object : Serde<TitleWithCreditedPeople> {
        override fun serializer() = Serializer<TitleWithCreditedPeople> { _, t -> toByteArray(t) }
        override fun deserializer() = Deserializer<TitleWithCreditedPeople> { _, s -> fromByteArray(s) }
    }

}