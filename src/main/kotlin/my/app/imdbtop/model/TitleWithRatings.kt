package my.app.imdbtop.model

data class TitleWithRatings(
    val titleId: String,        // alphanumeric unique identifier of the title
    val primaryTitle: String,   // the more popular title / the title used by the filmmakers on promotional materials at the point of release
    val originalTitle: String,  // original title, in the original language
    val titleType: String,      // the type/format of the title: S.g. movie, short, tv series, tv episode, video, etc)
    val averageRating: Double,  // weighted average of all the individual user ratings
    val numVotes: Long,
)