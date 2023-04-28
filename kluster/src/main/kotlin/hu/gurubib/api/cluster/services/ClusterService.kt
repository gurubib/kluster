package hu.gurubib.api.cluster.services

import hu.gurubib.api.cluster.dtos.RCreateClusteringReq
import org.jetbrains.exposed.sql.Database

interface ClusterService {
    suspend fun executeClustering(req: RCreateClusteringReq): List<List<String>>
}

class ClusterServiceImpl(private val db: Database) : ClusterService {
    override suspend fun executeClustering(req: RCreateClusteringReq): List<List<String>> {
        return listOf(listOf())
    }
}