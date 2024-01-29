package my.app.imdbtop.model

data class AggregatedMovies(
    val movies: Set<TopTitle> = setOf(),
    val totalMoviesCount: Long = 0L,
    val totalVotingSum: Long = 0L
)