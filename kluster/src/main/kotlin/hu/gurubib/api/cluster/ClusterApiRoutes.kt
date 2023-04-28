package hu.gurubib.api.cluster

import hu.gurubib.api.cluster.dtos.RCreateClusteringRes
import hu.gurubib.api.cluster.services.ClusterService
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
                val res = service.executeClustering(call.receive())
                call.respond(RCreateClusteringRes(res))
            }
            get<Clusterings.Id> {
                call.respondText { "Clustering" }
            }
        }
    }
}