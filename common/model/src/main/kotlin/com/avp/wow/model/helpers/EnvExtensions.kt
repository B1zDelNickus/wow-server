package com.avp.wow.model.helpers

fun envIntOrDefault(key: String, default: Int) = System.getenv()[key]?.toIntOrNull()?:default
fun envByteOrDefault(key: String, default: Byte) = System.getenv()[key]?.toByteOrNull()?:default
fun envBoolOrDefault(key: String, default: Boolean) = System.getenv()[key]?.toBoolean()?:default
fun envStringOrDefault(key: String, default: String) = System.getenv()[key] ?:default
