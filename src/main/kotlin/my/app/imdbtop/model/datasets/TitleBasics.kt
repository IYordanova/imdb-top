package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class TitleBasics @JsonCreator constructor(
    @JsonProperty("tconst")
    val titleId: String,        // alphanumeric unique identifier of the title
    val titleType: String,      // the type/format of the title: S.g. movie, short, tv series, tv episode, video, etc)
    val primaryTitle: String,   // the more popular title / the title used by the filmmakers on promotional materials at the point of release
    val originalTitle: String,  // original title, in the original language
    val isAdult: Boolean,       // 0: non-adult title; 1: adult title
    val startYear: String,      // YYYY - represents the release year of a title. In the case of TV Series, it is the series start year
    val endYear: String,        // YYYY - TV Series end year. ‘\N’ for all other title types
    val runtimeMinutes: Long,   // primary runtime of the title, in minutes
    val genres: List<String>,   // includes up to three genres associated with the title
) : DataSet