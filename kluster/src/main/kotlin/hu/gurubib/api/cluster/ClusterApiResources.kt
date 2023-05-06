package hu.gurubib.api.cluster

import io.ktor.resources.*

@Resource("/clusterings")
class Clusterings {
    @Resource("{id}")
    class Id(val parent: Clusterings = Clusterings(), val id: String) {
        @Resource("metrics")
        class Metrics(val parent: Id, val name: String = "SILHOUETTE")
    }
}

@Resource("/labellings")
class Labellings
