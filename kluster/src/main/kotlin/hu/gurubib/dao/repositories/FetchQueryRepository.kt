package hu.gurubib.dao.repositories

import hu.gurubib.dao.models.FetchQueries
import hu.gurubib.dao.models.PFetchQuery
import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface FetchQueryRepository : ExposedCrudRepository<Int, PFetchQuery> {
    val db: Database
}

class FetchQueryRepositoryImpl(override val db: Database, ) : FetchQueryRepository {
    override suspend fun create(init: PFetchQuery.() -> Unit) = PFetchQuery.new(init)

    override suspend fun createTransactional(init: PFetchQuery.() -> Unit) = newSuspendedTransaction {
        PFetchQuery.new(init)
    }

    override suspend fun findAll() = newSuspendedTransaction {
        PFetchQuery.all().toList()
    }

    override suspend fun find(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        findWithLimitAndOffset(0, 0) {
            PFetchQuery.find(op)
        }
    }

    override suspend fun find(query: ExposedQueryBuilder) = newSuspendedTransaction {
        findWithLimitAndOffset(query.limit, query.offset) {
            PFetchQuery.find(query.buildQueryExpression())
        }
    }

    override suspend fun findById(id: Int) = newSuspendedTransaction { PFetchQuery.findById(id) }

    override suspend fun findByUuid(uuid: String) = newSuspendedTransaction {
        PFetchQuery.find { FetchQueries.uuid eq uuid }.firstOrNull()
    }

    override suspend fun countAll() = newSuspendedTransaction { PFetchQuery.all().count() }

    override suspend fun count(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        PFetchQuery.find(op).count()
    }

    override suspend fun count(query: ExposedQueryExpressionHolder) = newSuspendedTransaction {
        PFetchQuery.find(query.buildQueryExpression()).count()
    }

    override suspend fun updateTransactional(entity: PFetchQuery, updateAction: PFetchQuery.() -> Unit) {
        newSuspendedTransaction {
            entity.updateAction()
        }
    }
}