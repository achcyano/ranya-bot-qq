package me.achqing.ranya1.utils

fun String.startsWithAny(vararg prefixes: String): Boolean {
    return prefixes.any { this.startsWith(it) }
}

