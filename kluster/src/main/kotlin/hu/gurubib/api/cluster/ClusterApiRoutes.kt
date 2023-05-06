package hu.gurubib.api.cluster

import hu.gurubib.api.cluster.dtos.*
import hu.gurubib.api.cluster.services.ClusterService
import hu.gurubib.api.stock.dtos.dto
import hu.gurubib.plugins.API_ROOT_PATH
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val CLUSTER_API_VERSION = "v1"
const val CLUSTER_API_NAME = "cluster"
const val CLUSTER_API_PATH = "${API_ROOT_PATH}/${CLUSTER_API_NAME}/${CLUSTER_API_VERSION}"

fun Application.configureClusterApiRoutes() {

    val service: ClusterService by inject()

    routing {
        route(CLUSTER_API_PATH) {
            post<Clusterings> {
                val req = call.receive<RCreateClusteringReq>()
                val clustering = service.createClustering(req.domain())
                call.respond(clustering.dto())
            }

            post<Labellings> {
                val req = call.receive<RLabelling>()
                val labelling = service.createLabelling(req.domain()).map { stocks -> stocks.map { it.dto() } }
                call.respond(labelling)
            }

            get<Clusterings.Id.Metrics> {
                val metric = service.getMetric(it.parent.id, it.name)
                call.respond(metric.dto())
            }
        }
    }
}