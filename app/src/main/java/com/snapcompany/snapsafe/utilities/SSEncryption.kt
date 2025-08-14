package com.snapcompany.snapsafe.utilities

import kotlin.experimental.xor
import kotlin.random.Random

class SSEncryption {

    private fun createPublicKey(arraySize: Int = 8): ByteArray {

        val byteArray = ByteArray(arraySize)

        // Fill the ByteArray with random bytes between 33 and 126
        for (i in 0 until arraySize) {
            byteArray[i] = Random.nextInt(33, 127).toByte()
        }

        // Print the ByteArray
        println("Random ByteArray (33-126): ${byteArray.contentToString()}")
        return byteArray
    }

    private fun fusionPassword(passwordAscii: ByteArray, publicKeyAscii: ByteArray): List<Byte> {
        val fusion = ByteArray(8)
        for(index in passwordAscii.indices){
            fusion[index] = passwordAscii[index] xor publicKeyAscii[index]
        }
        return fusion.toList()
    }


    fun encrypt(order: String, password: String): String{

        println("current password: $password")

        val publicKeyAscii = createPublicKey()
        val passwordAscii = password.toByteArray()
        val requestAscii = order.subSequence(0,12).toString().toByteArray()

        println("requestAscii ${requestAscii.toList()}")
        println("passwordAscii ${passwordAscii.toList()}")
        println("publicKeyAscii ${publicKeyAscii.toList()}")

        val fusion = fusionPassword(passwordAscii, publicKeyAscii)
        println("fusion: $fusion")

        var completeRequest = requestAscii.toStringFromBytes() + publicKeyAscii.toStringFromBytes()
        println("completeRequest: $completeRequest")

        val completeRequestAscii = completeRequest.toByteArray(Charsets.UTF_8)
        println("completeRequestAscii: $completeRequestAscii")

        //step 1
        completeRequestAscii[0] = completeRequestAscii[0] xor fusion[0]
        completeRequestAscii[1] = completeRequestAscii[1] xor fusion[3]
        completeRequestAscii[2] = completeRequestAscii[2] xor fusion[2]
        completeRequestAscii[3] = completeRequestAscii[3] xor fusion[1]
        completeRequestAscii[4] = completeRequestAscii[4] xor fusion[7]
        completeRequestAscii[5] = completeRequestAscii[5] xor fusion[6]
        completeRequestAscii[6] = completeRequestAscii[6] xor fusion[5]
        completeRequestAscii[7] = completeRequestAscii[7] xor fusion[4]
        completeRequestAscii[8] = completeRequestAscii[8] xor fusion[2]
        completeRequestAscii[9] = completeRequestAscii[9] xor fusion[5]
        completeRequestAscii[10] = completeRequestAscii[10] xor fusion[1]
        completeRequestAscii[11] = completeRequestAscii[11] xor fusion[3]

        completeRequest = completeRequestAscii.toStringFromBytes() + publicKeyAscii.toStringFromBytes()
        println("completeRequest after step 1: $completeRequest")



        return completeRequest

    }



    private fun ByteArray.toStringFromBytes(): String {
        val result = StringBuilder()
        for (char in this) {
            result.append(char.toChar())
        }
        return result.toString()
    }



}


/*
encriptacion

//
//  Encryption.swift
//  MySafe
//
//  Created by Andres on 17/1/24.
//

import Foundation

extension Array<UInt8>{

    func toString() -> String{
        var array : Array<UInt8> = self
        var result = ""
        for char in array{
            result = result + String(UnicodeScalar(char))
        }

        return result
    }
}

extension String {
    func toAsciiArray() -> Array<UInt8>{
        var stringIntoArray = Array(self)
        var arrayInAscii = Array<UInt8>()
        for char in stringIntoArray {
            arrayInAscii.append(char.asciiValue!)
        }
        print("result of arrayInAscii: <\(arrayInAscii)>")
        return arrayInAscii
    }
}

extension String {
    func stringToBinary() -> String {
        let st = self
        var result = ""
        for char in st.utf8 {
            var tranformed = String(char, radix: 2)
            while tranformed.count < 8 {
                tranformed = "0" + tranformed
            }
            let binary = "\(tranformed) "
            result.append(binary)
        }
        return result
    }
}

class base {

    func encryption(request: String, vector: String, password: String) -> String{

        let vectorAscii = vector.toAsciiArray()
        var passwordAscii = password.toAsciiArray()
        var requestAscii = request.toAsciiArray()

        var encryptedRequest = ""

        var passwordLength = password.count



        requestAscii[0] = vectorAscii[1] ^ requestAscii[0]
        requestAscii[1] = vectorAscii[7] ^ requestAscii[1]
        requestAscii[2] = vectorAscii[2] ^ requestAscii[2]
        requestAscii[3] = vectorAscii[4] ^ requestAscii[3]
        requestAscii[4] = vectorAscii[3] ^ requestAscii[4]
        requestAscii[5] = vectorAscii[5] ^ requestAscii[5]
        requestAscii[6] = vectorAscii[0] ^ requestAscii[6]
        requestAscii[7] = vectorAscii[1] ^ requestAscii[7]
        requestAscii[8] = vectorAscii[2] ^ requestAscii[8]
        requestAscii[9] = vectorAscii[4] ^ requestAscii[9]
        requestAscii[10] = vectorAscii[5] ^ requestAscii[10]
        requestAscii[11] = vectorAscii[3] ^ requestAscii[11]



        print("requestAscii after 1: \(requestAscii)")


             var fusion = Array<UInt8>(arrayLiteral: 0,0,0,0,0,0,0,0)
             var fusionIndex = 0
             for passwordChar in passwordAscii {
                 fusion[fusionIndex] = passwordChar ^ vectorAscii[fusionIndex]
                 fusionIndex += 1
             }
             print("fusion: \(String(describing: fusion))")


             for index in 0...11 {
                 for fusionChar in fusion {
                     requestAscii[index] = (fusionChar ^ requestAscii[index])
                 }
             }



        print("requestAscii after 1: \(requestAscii)")


        for index in 0...11{

            if index <= 6 {requestAscii[index] = requestAscii[index] ^ passwordAscii[6-index]}
            if index >= 7 {requestAscii[index] = requestAscii[index] ^ passwordAscii[12-index]}
        }

        print("requestAscii after 2: \(requestAscii)")

        requestAscii[0] = vectorAscii[2] ^ requestAscii[0]
        requestAscii[1] = vectorAscii[5] ^ requestAscii[1]
        requestAscii[2] = vectorAscii[1] ^ requestAscii[2]
        requestAscii[3] = vectorAscii[7] ^ requestAscii[3]
        requestAscii[4] = vectorAscii[4] ^ requestAscii[4]
        requestAscii[5] = vectorAscii[3] ^ requestAscii[5]
        requestAscii[6] = vectorAscii[0] ^ requestAscii[6]
        requestAscii[7] = vectorAscii[6] ^ requestAscii[7]
        requestAscii[8] = vectorAscii[7] ^ requestAscii[8]
        requestAscii[9] = vectorAscii[1] ^ requestAscii[9]
        requestAscii[10] = vectorAscii[0] ^ requestAscii[10]
        requestAscii[11] = vectorAscii[4] ^ requestAscii[11]


        print("requestAscii after 3: \(requestAscii)")
        encryptedRequest = requestAscii.toString() + vector
        print("Encrypted request: \(encryptedRequest)")

        return encryptedRequest

    }
}

 */