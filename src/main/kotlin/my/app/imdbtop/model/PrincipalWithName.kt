package my.app.imdbtop.model

data class PrincipalWithName(
    val titleId: String,        // alphanumeric unique identifier of the title
    val nameId: String,     // alphanumeric unique identifier of the name/person
    val job: String,        // the specific job title if applicable, else '\N'
    val primaryName: String, // name by which the person is most often credited
)