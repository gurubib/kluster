package hu.gurubib.dao.repositories

import hu.gurubib.dao.models.MarketIndexConstituents
import hu.gurubib.dao.models.MarketIndexes
import hu.gurubib.dao.models.PMarketIndex
import hu.gurubib.dao.models.PMarketIndexConstituent
import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface MarketIndexRepository : ExposedCrudRepository<Int, PMarketIndex> {
    val db: Database

    suspend fun findByName(name: String): PMarketIndex?
    suspend fun findConstituentsFor(index: String): List<PMarketIndexConstituent>
    suspend fun createConstituentsFor(index: String, symbols: List<String>): List<PMarketIndexConstituent>
}

class MarketIndexRepositoryImpl(
    override val db: Database
) : MarketIndexRepository {
    override suspend fun create(init: PMarketIndex.() -> Unit): PMarketIndex = PMarketIndex.new(init)

    override suspend fun createTransactional(init: PMarketIndex.() -> Unit) = newSuspendedTransaction {
        PMarketIndex.new(init)
    }

    override suspend fun createConstituentsFor(index: String, symbols: List<String>) = newSuspendedTransaction {
        symbols.map {
            PMarketIndexConstituent.new {
                indexName = index
                stockSymbol = it
            }
        }
    }

    override suspend fun findAll() = newSuspendedTransaction {
        PMarketIndex.all().toList()
    }

    override suspend fun find(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        findWithLimitAndOffset(0, 0) {
            PMarketIndex.find(op)
        }
    }

    override suspend fun find(query: ExposedQueryBuilder) = newSuspendedTransaction {
        findWithLimitAndOffset(query.limit, query.offset) {
            PMarketIndex.find(query.buildQueryExpression())
        }
    }

    override suspend fun findById(id: Int) = newSuspendedTransaction { PMarketIndex.findById(id) }

    override suspend fun findByName(name: String) = newSuspendedTransaction {
        PMarketIndex.find { MarketIndexes.name eq name }.firstOrNull()
    }

    override suspend fun findConstituentsFor(index: String) = newSuspendedTransaction {
        PMarketIndexConstituent.find( MarketIndexConstituents.indexName eq index ).toList()
    }

    override suspend fun countAll() = newSuspendedTransaction { PMarketIndex.all().count() }

    override suspend fun count(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        PMarketIndex.find(op).count()
    }

    override suspend fun count(query: ExposedQueryExpressionHolder) = newSuspendedTransaction {
        PMarketIndex.find(query.buildQueryExpression()).count()
    }

    override suspend fun updateTransactional(entity: PMarketIndex, updateAction: PMarketIndex.() -> Unit) {
        newSuspendedTransaction {
            entity.updateAction()
        }
    }
}
