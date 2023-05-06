package hu.gurubib.dao.models

import hu.gurubib.domain.stock.models.Clustering
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object Clusterings : IntIdTable("clusterings") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val fromDate = date("from_date")
    val toDate = date("to_date")
    val normalise = varchar("normalise", 50)
    val distance = varchar("distance", 50)
    val algorithm = varchar("algorithm", 50)
    val numOfClusters = integer("num_of_clusters")
}

class PClustering(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PClustering>(Clusterings)

    var uuid by Clusterings.uuid
    var fromDate by Clusterings.fromDate
    var toDate by Clusterings.toDate
    var normalise by Clusterings.normalise
    var distance by Clusterings.distance
    var algorithm by Clusterings.algorithm
    var numOfClusters by Clusterings.numOfClusters
}

fun PClustering.domain(): Clustering = Clustering(
    uuid = uuid,
    from = fromDate,
    to = toDate,
    normalise =  normalise,
    distance = distance,
    algorithm = algorithm,
    numOfClusters = numOfClusters,
)

fun Clustering.toInitializer(): PClustering.() -> Unit = {
    uuid = this@toInitializer.uuid
    fromDate = this@toInitializer.from
    toDate = this@toInitializer.to
    normalise = this@toInitializer.normalise
    distance = this@toInitializer.distance
    algorithm = this@toInitializer.algorithm
    numOfClusters = this@toInitializer.numOfClusters
}

object ClusteredObjects: IntIdTable("clustered_objects") {
    val clusteringUuid = varchar("uuid", 36).index()
    val objectId = varchar("object_id", length = 10)
    val clusterId = varchar("cluster_id", length = 10)
}

class PClusteredObject(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PClusteredObject>(ClusteredObjects)

    var clusteringUuid by ClusteredObjects.clusteringUuid
    var objectId by ClusteredObjects.objectId
    var clusterId by ClusteredObjects.clusterId
}
