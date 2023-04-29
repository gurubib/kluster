package hu.gurubib.dao.repositories

import hu.gurubib.dao.models.PPrice
import hu.gurubib.dao.models.Prices
import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface PriceRepository : ExposedCrudRepository<Int, PPrice> {
    val db: Database

    suspend fun findByUuid(uuid: String): PPrice?
}

class PriceRepositoryImpl(
    override val db: Database,
) : PriceRepository {

    override suspend fun create(init: PPrice.() -> Unit) = PPrice.new(init)

    override suspend fun createTransactional(init: PPrice.() -> Unit) = newSuspendedTransaction {
        PPrice.new(init)
    }

    override suspend fun findAll() = newSuspendedTransaction {
        PPrice.all().orderBy(Prices.date to ASC).toList()
    }

    override suspend fun find(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        findWithLimitAndOffset(0, 0) {
            PPrice.find(op).orderBy(Prices.date to ASC)
        }
    }

    override suspend fun find(query: ExposedQueryBuilder) = newSuspendedTransaction {
        findWithLimitAndOffset(query.limit, query.offset) {
            PPrice.find(query.buildQueryExpression()).orderBy(Prices.date to ASC)
        }
    }

    override suspend fun findById(id: Int) = newSuspendedTransaction { PPrice.findById(id) }

    override suspend fun findByUuid(uuid: String) = newSuspendedTransaction {
        PPrice.find { Prices.uuid eq uuid }.firstOrNull()
    }

    override suspend fun countAll() = newSuspendedTransaction { PPrice.all().count() }

    override suspend fun count(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        PPrice.find(op).count()
    }

    override suspend fun count(query: ExposedQueryExpressionHolder) = newSuspendedTransaction {
        PPrice.find(query.buildQueryExpression()).count()
    }

    override suspend fun updateTransactional(entity: PPrice, updateAction: PPrice.() -> Unit) {
        newSuspendedTransaction {
            entity.updateAction()
        }
    }
}