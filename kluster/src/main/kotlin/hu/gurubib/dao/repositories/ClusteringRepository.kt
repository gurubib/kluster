package hu.gurubib.dao.repositories

import hu.gurubib.dao.models.ClusteredObjects
import hu.gurubib.dao.models.Clusterings
import hu.gurubib.dao.models.PClusteredObject
import hu.gurubib.dao.models.PClustering
import hu.gurubib.util.dao.ExposedQueryBuilder
import hu.gurubib.util.dao.ExposedQueryExpressionHolder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface ClusteringRepository : ExposedCrudRepository<Int, PClustering> {
    val db: Database

    suspend fun findByUuid(uuid: String): PClustering?
    suspend fun createClusteredObjectsFor(uuid: String, clusters: List<List<String>>): List<PClusteredObject>
    suspend fun findClusteredObjectsFor(uuid: String): List<PClusteredObject>
}

class ClusteringRepositoryImpl(
    override val db: Database,
) : ClusteringRepository {
    override suspend fun create(init: PClustering.() -> Unit): PClustering = PClustering.new(init)

    override suspend fun createTransactional(init: PClustering.() -> Unit) = newSuspendedTransaction {
        PClustering.new(init)
    }

    override suspend fun createClusteredObjectsFor(uuid: String, clusters: List<List<String>>) =
        newSuspendedTransaction {
            clusters.flatMapIndexed { i, cluster ->
                cluster.map { o ->
                    PClusteredObject.new {
                        clusteringUuid = uuid
                        objectId = o
                        clusterId = ('A' + i).toString()
                    }
                }
            }
        }

    override suspend fun findAll() = newSuspendedTransaction {
        PClustering.all().toList()
    }

    override suspend fun find(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        findWithLimitAndOffset(0, 0) {
            PClustering.find(op)
        }
    }

    override suspend fun find(query: ExposedQueryBuilder) = newSuspendedTransaction {
        findWithLimitAndOffset(query.limit, query.offset) {
            PClustering.find(query.buildQueryExpression())
        }
    }

    override suspend fun findById(id: Int) = newSuspendedTransaction {
        PClustering.findById(id)
    }

    override suspend fun findByUuid(uuid: String) = newSuspendedTransaction {
        PClustering.find { Clusterings.uuid eq uuid }.firstOrNull()
    }

    override suspend fun findClusteredObjectsFor(uuid: String) = newSuspendedTransaction {
        PClusteredObject.find { ClusteredObjects.clusteringUuid eq uuid }.toList()
    }

    override suspend fun countAll() = newSuspendedTransaction {
        PClustering.all().count()
    }

    override suspend fun count(op: SqlExpressionBuilder.() -> Op<Boolean>) = newSuspendedTransaction {
        PClustering.find(op).count()
    }

    override suspend fun count(query: ExposedQueryExpressionHolder) = newSuspendedTransaction {
        PClustering.find(query.buildQueryExpression()).count()
    }

    override suspend fun updateTransactional(entity: PClustering, updateAction: PClustering.() -> Unit) =
        newSuspendedTransaction {
            entity.updateAction()
        }
}
