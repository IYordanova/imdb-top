package my.app.imdbtop.model

data class AggregatedPrincipals(
    val principalPrimaryNames: Set<String> = setOf(),
    val titleId: String = ""
)