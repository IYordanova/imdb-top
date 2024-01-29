package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Ratings @JsonCreator constructor(
    @JsonProperty("tconst")
    val titleId: String,        // a tConst, an alphanumeric unique identifier of the title
    val averageRating: Double,  // weighted average of all the individual user ratings
    val numVotes: Long,       //  number of votes the title has received
): DataSet