package my.app.imdbtop.model

data class TitleWithCreditedPeople(
    val titleId: String,        // alphanumeric unique identifier of the title
    val titles: Set<String>,   // the more popular title / the title used by the filmmakers on promotional materials at the point of release
    val people: Set<String>,      // the type/format of the title: S.g. movie, short, tv series, tv episode, video, etc)
)