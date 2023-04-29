package hu.gurubib.dao.repositories

import hu.gurubib.dao.models.PStock
import hu.gurubib.dao.models.Stocks
import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface StockRepository : ExposedCrudRepository<Int, PStock> {
    val db: Database

    suspend fun findBySymbol(symbol: String): PStock?
}

class StockRepositoryImpl(
    override val db: Database
) : StockRepository {
    override suspend fun create(init: PStock.() -> Unit): PStock = PStock.new(init)

    override suspend fun createTransactional(init: PStock.() -> Unit) = newSuspendedTransaction {
        PStock.new(init)
    }

    override suspend fun findAll() = newSuspendedTransaction {
        PStock.all().toList()
    }

    override suspend fun find(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        findWithLimitAndOffset(0, 0) {
            PStock.find(op)
        }
    }

    override suspend fun find(query: ExposedQueryBuilder) = newSuspendedTransaction {
        findWithLimitAndOffset(query.limit, query.offset) {
            PStock.find(query.buildQueryExpression())
        }
    }

    override suspend fun findById(id: Int) = newSuspendedTransaction { PStock.findById(id) }

    override suspend fun findBySymbol(symbol: String) = newSuspendedTransaction {
        PStock.find { Stocks.symbol eq symbol }.firstOrNull()
    }

    override suspend fun countAll() = newSuspendedTransaction { PStock.all().count() }

    override suspend fun count(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        PStock.find(op).count()
    }

    override suspend fun count(query: ExposedQueryExpressionHolder) = newSuspendedTransaction {
        PStock.find(query.buildQueryExpression()).count()
    }

    override suspend fun updateTransactional(entity: PStock, updateAction: PStock.() -> Unit) {
        newSuspendedTransaction {
            entity.updateAction()
        }
    }
}