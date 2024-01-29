package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Crew @JsonCreator constructor(
    @JsonProperty("tconst")
    val titleId: String,           //  alphanumeric unique identifier of the title
    @JsonProperty("directors")
    val directorNameIds: List<String>, //  director(s) of the given title
    @JsonProperty("writers")
    val writerNameIds: List<String>    // writer(s) of the given title
): DataSet