package hu.gurubib.util

import java.time.LocalDateTime
import java.util.UUID

fun uuid(): String = UUID.randomUUID().toString()

fun now(): LocalDateTime = LocalDateTime.now()

inline fun <reified T : Enum<T>> isValidEnumValue(value: String?): Boolean =
    enumValues<T>().asSequence().map { it.name.uppercase() }.contains(value?.uppercase())

inline fun <reified T : Enum<T>> enumValueOfOrNull(value: String): T? =
    if (isValidEnumValue<T>(value)) {
        enumValueOf<T>(value)
    } else {
        null
    }

inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    if (value == null) {
        default
    } else {
        enumValueOfOrNull<T>(value) ?: default
    }
