package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Episode @JsonCreator constructor(
    @JsonProperty("tconst")
    val episodeId: String,    // alphanumeric identifier of episode
    val parentTconst: String, // alphanumeric identifier of the parent TV Series
    val seasonNumber: Int,    // season number the episode belongs to
    val episodeNumber: Int    // episode number of the tconst in the TV series
) : DataSet