package hu.gurubib.api.cluster

import io.ktor.resources.*

@Resource("/clusterings")
class Clusterings {
    @Resource("{id}")
    class Id(val parent: Clusterings = Clusterings(), val id: String) {
        @Resource("clusters")
        class Clusters(val parent: Id)
        @Resource("metrics")
        class Metrics(val parent: Id) {
            @Resource("{name}")
            class Name(val parent: Metrics, val name: String) {
                @Resource("{otherId}")
                class Similarity(val parent: Name, val otherId: String)
            }
        }
    }
}

@Resource("/labellings")
class Labellings
