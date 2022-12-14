package pl.ncms.moviere.movie.dao

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import pl.ncms.moviere.config.CrudDAO
import pl.ncms.moviere.config.Provider
import pl.ncms.moviere.movie.Movie
import java.util.*

interface MovieDAO : CrudDAO<Movie, Int> {

    companion object : Provider<MovieDAO> {
        override fun provide(): MovieDAO {
            return StdMovieDao()
        }
    }

    fun getAllPageable(page: Int, itemPerPage: Int): List<Movie>

    fun getStd(id: Int): Movie?

    fun findByTitleLike(title: String): List<Movie>

    fun findByTitle(title: String): List<Movie>

}

class StdMovieDao : MovieDAO {

    override fun create(entity: Movie): Int = transaction {
        val id = Movies.insert {
            if (entity.id != 0) {
                it[id] = entity.id
            }
            it[title] = entity.title
        } get Movies.id

        id
    }

    override fun update(entity: Movie): Movie {
        TODO("Not yet implemented")
    }

    override fun delete(id: Int) {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<Movie> = transaction {
        Movies.selectAll().map { mapToObj(it) }
    }

    override fun getAllPageable(page: Int, itemPerPage: Int): List<Movie> = transaction {
        Movies.selectAll()
            .limit(itemPerPage, (page * itemPerPage).toLong())
            .map { mapToObj(it) }
    }

    override fun get(id: Int): Movie? = transaction {
        val row = Movies.select {
            (Movies.id eq id)
        }
            .singleOrNull()

        row?.let { mapToObj(it) }
    }

    override fun getStd(id: Int): Movie? = transaction {
        val row = Movies.select {
            (Movies.id eq id) and
            (Movies.numOfVotes greater 150)
        }
            .singleOrNull()

        row?.let { mapToObj(it) }
    }

    override fun findByTitleLike(title: String): List<Movie> = transaction {
        val titleLike = title.uppercase(Locale.getDefault())
        Movies.select {
            (UpperCase(Movies.title) like "%${titleLike}%") and
            (Movies.numOfVotes greater 150)
        }
            .map { mapToObj(it) }
    }

    override fun findByTitle(title: String): List<Movie> = transaction {
        Movies.select(
            Movies.title eq title and
                    (Movies.numOfVotes greater 150)
        )
            .map { mapToObj(it) }
    }

    private fun mapToObj(row: ResultRow): Movie {
        return Movie(
            row[Movies.id],
            row[Movies.title],
            row[Movies.year],
            row[Movies.isAdult] == null || row[Movies.isAdult]!! > 0,
            row[Movies.runtime],
            row[Movies.genre1],
            row[Movies.genre2],
            row[Movies.genre3],
        )
    }

}