package hu.gurubib.dao.repositories

import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder

interface ExposedCrudRepository<ID: Comparable<ID>, T: Entity<Int>> {
    suspend fun create(init: T.() -> Unit): T
    suspend fun createTransactional(init: T.() -> Unit): T
    suspend fun findAll(): List<T>
    suspend fun find(op: SqlExpressionBuilder.()-> Op<Boolean>): List<T>
    suspend fun find(query: ExposedQueryBuilder): List<T>
    suspend fun findById(id: Int): T?
    suspend fun findByUuid(uuid: String): T?

    suspend fun countAll(): Long
    suspend fun count(op: SqlExpressionBuilder.()-> Op<Boolean>): Long
    suspend fun count(query: ExposedQueryExpressionHolder): Long

    suspend fun updateTransactional(entity: T, updateAction: T.() -> Unit)
}

inline fun <T> findWithLimitAndOffset(limit: Int, offset: Int, findOperation: () -> SizedIterable<T>): List<T> =
    if (limit == 0) {
        findOperation().limit(Int.MAX_VALUE, offset = offset.toLong()).toList()
    } else {
        findOperation().limit(limit, offset = offset.toLong()).toList()
    }