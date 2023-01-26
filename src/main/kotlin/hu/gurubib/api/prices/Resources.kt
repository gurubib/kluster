package hu.gurubib.api.prices

import io.ktor.resources.*

@Resource("/fetches")
class Fetches {
    @Resource("{id}")
    class Id(val parent: Fetches = Fetches(), val id: Long)
}