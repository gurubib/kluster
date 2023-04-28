package hu.gurubib.util

import java.time.LocalDateTime
import java.util.UUID

fun uuid(): String = UUID.randomUUID().toString()

fun now(): LocalDateTime = LocalDateTime.now()
