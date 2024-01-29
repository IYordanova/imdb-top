package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Principal @JsonCreator constructor(
    @JsonProperty("tconst")
    val titleId: String,    // a tConst, an alphanumeric unique identifier of the title
    val ordering: Int,      // a number to uniquely identify rows for a given titleId
    @JsonProperty("nconst")
    val nameId: String,     // alphanumeric unique identifier of the name/person
    val category: String,   // the category of job that person was in
    val job: String,        // the specific job title if applicable, else '\N'
    val characters: String, // the name of the character played if applicable, else '\N'
) : DataSet