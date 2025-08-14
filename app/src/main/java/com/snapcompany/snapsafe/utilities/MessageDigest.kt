package com.snapcompany.snapsafe.utilities
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

    fun String.toSha256(): String {
        return try {
            // 1. Obtener una instancia de MessageDigest para SHA-256
            val digest = MessageDigest.getInstance("SHA-256")

            // 2. Convertir la cadena de entrada a bytes (usando UTF-8)
            val inputBytes = this.toByteArray(StandardCharsets.UTF_8)

            // 3. Actualizar el digest con los bytes de entrada (opcional si solo es una entrada)
            // digest.update(inputBytes) // No es estrictamente necesario si solo llamas a digest() una vez

            // 4. Calcular el hash
            val hashedBytes = digest.digest(inputBytes) // digest() también puede tomar los bytes directamente

            // 5. Convertir el hash de bytes a una cadena hexadecimal
            bytesToHex(hashedBytes)
        } catch (e: Exception) {
            // Manejar excepciones, por ejemplo, NoSuchAlgorithmException
            e.printStackTrace()
            "" // O lanzar una excepción personalizada
        }
    }

    // Función auxiliar para convertir un array de bytes a una cadena hexadecimal
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF // Convertir byte a int sin signo
            hexChars[j * 2] = "0123456789abcdef"[v ushr 4] // Dígito hexadecimal superior
            hexChars[j * 2 + 1] = "0123456789abcdef"[v and 0x0F] // Dígito hexadecimal inferior
        }
        return String(hexChars)
    }

    // --- Ejemplo de Uso ---
    fun main() { // O dentro de tu código Android
        val originalString = "Hola, este es un secreto!"
        val hashedString = originalString.toSha256()

        println("Original: $originalString")
        println("SHA-256 Hash: $hashedString")

        val anotherString = "Hola, este es un secreto!"
        val anotherHashedString = anotherString.toSha256()
        println("Misma entrada, mismo hash: ${hashedString == anotherHashedString}") // Debería ser true

        val differentString = "Texto diferente"
        val differentHashedString = differentString.toSha256()
        println("Entrada diferente, hash diferente: $differentHashedString")
        println("Hash diferente: ${hashedString != differentHashedString}") // Debería ser true
    }

