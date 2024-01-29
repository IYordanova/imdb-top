package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class NameBasics @JsonCreator constructor(
    @JsonProperty("nconst")
    val nameId: String, // alphanumeric unique identifier of the name/person
    val primaryName: String, // name by which the person is most often credited
    val birthYear: String, // in YYYY format
    val deathYear: String, // in YYYY format if applicable, else '\N'
    val primaryProfession: List<String>, // the top-3 professions of the person
    @JsonProperty("knownForTitles")
    val knownForTitleIds: List<String> // titleIds the person is known for
) : DataSet