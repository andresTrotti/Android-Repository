package com.snapcompany.snapsafe.utilities

fun <K,V> Map<String,String>.toGateData(): GateData {
    return GateData(
        gateId = this["gateId"] ?: "",
        gateName = this["gateName"] ?: "",
        mac = this["mac"] ?: "",
        password = this["password"] ?: "",
        pPassword = this["pPassword"] ?: "",
        service = this["service"] ?: "",
        characteristic = this["characteristic"] ?: "",
        characteristicRx = this["characteristicRx"] ?: "",
        gateType = this["gateType"] ?: "",
        independent = this["shareEnable"]?.toBoolean() ?: false,
        version = this["version"] ?: "",
        imageUri = this["imageUri"] ?: "",
    )
}