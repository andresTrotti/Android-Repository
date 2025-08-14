package com.snapcompany.snapsafe.utilities

// 33: ! 126: ~

fun charAsciiValidation(char: Char): Boolean {
    val charToAsciiCode = char.code
    return charToAsciiCode in 33..126
}

fun textAsciiValidation(text: String): Boolean {
    for (char in text) {
        if (!charAsciiValidation(char)) {
            return false
        }
    }
    return true
}