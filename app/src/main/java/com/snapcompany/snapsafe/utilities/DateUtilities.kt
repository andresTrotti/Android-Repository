package com.snapcompany.snapsafe.utilities

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DateUtilities {

    fun monthNumberToName(monthNumber: String): String {
        return when (monthNumber) {
            "1" -> "ene."
            "2" -> "feb."
            "3" -> "mar."
            "4" -> "abr."
            "5" -> "may."
            "6" -> "jun."
            "7" -> "jul."
            "8" -> "ago."
            "9" -> "set."
            "10" -> "oct."
            "11" -> "nov."
            "12" -> "dic."
            else -> "Mes no válido"

        }
    }

    /*fun getLocalUtc(): String {
        val zoneId = ZoneId.systemDefault() // Get the system's default time zone
        val instant = Instant.now() // Get the current instant in UTC
        return instant.atZone(zoneId).toString() // Format the instant with the zone ID
    }*/

    fun getLocalUtcOffset(): ZoneOffset {
        return ZoneId.systemDefault().rules.getOffset(Instant.now())
    }

    fun convert12to24Hour(time12Hour: String): String {

        val isAfternoon = time12Hour.contains("p.m.")
        val hour = time12Hour.split(":")[0]
        if(isAfternoon) {
            var time24hour = time12Hour.replace(hour, "${hour.toInt() + 12}")
            if(hour.toInt() == 24) time24hour =  time12Hour.replace("24", "00")
            return time24hour.substringBefore(" ")
        }
        else {
            if(hour == "12") return time12Hour.replace("12", "00")
            return time12Hour.substringBefore(" ")
        }


    }

    fun millisToDate(millis: Long, applyOffset: Boolean): List<String> {
        if(applyOffset){
            val offset = if(getLocalUtcOffset().toString().contains("-")) getLocalUtcOffset().toString().replace("-","+") else getLocalUtcOffset().toString().replace("+","-")
            val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC${offset}"))
            return listOf("${dateFormat.year}", "${dateFormat.monthValue}", "${dateFormat.dayOfMonth}")
        }
        else{
            val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
            return listOf("${dateFormat.year}", "${dateFormat.monthValue}", "${dateFormat.dayOfMonth}")
        }

    }

    fun millisToLocalDateArray(millis: Long): List<String> {
        val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return listOf("${dateFormat.year}", "${dateFormat.monthValue}", "${dateFormat.dayOfMonth}" )
    }
    fun millisToLocalTime(millis: Long): String {
        val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
        val time = dateFormat.format(DateTimeFormatter.ofPattern("hh:mm a"))
        var result = ""
        // here was an error example 06:14 p. m. was impossible to evaluate. (" ") != (NBSP)
        if(time.contains("p")){
            var correct = time.substringBefore(" ")
            correct += " p.m."
            result = correct
        }
        if(time.contains("a")){
            var correct = time.substringBefore(" ")
            correct += " a.m."
            result = correct
        }
        return result
    }

    /*fun millisToLocalDateWithoutTime(millis: Long): String {
        val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return "${dateFormat.year}/${dateFormat.monthValue}/${dateFormat.dayOfMonth}"
    }*/

    /*fun simplifyDate(dateString: String) : String{
        try {
            dateString.toLong()
        } catch (e: Exception){
            return "Fecha no valida"
        }

        val currentLocalTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.of("UTC-4")).toLocalDateTime()
        val currentDate = "${currentLocalTime.dayOfMonth}/${currentLocalTime.monthValue}/${currentLocalTime.year} a las: ${currentLocalTime.hour}:${currentLocalTime.minute}"

        val dateFormat = Instant.ofEpochMilli(dateString.toLong()).atZone(ZoneId.of("UTC-4")).toLocalDateTime()
        val requestDate = "${dateFormat.dayOfMonth}/${dateFormat.monthValue}/${dateFormat.year} a las: ${dateFormat.hour}:${dateFormat.minute}"

        return when{

            dateFormat.dayOfMonth == currentLocalTime.dayOfMonth -> {
                "Hoy a las: ${dateFormat.hour}:${dateFormat.minute}"
            }

            else -> {
                "${dateFormat.dayOfMonth}/${dateFormat.monthValue}/${dateFormat.year}"
            }
        }

    }*/

    fun millisToLocalDateTime(millis: Long): String {
        val dateFormat = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return "${dateFormat.dayOfMonth}/${dateFormat.monthValue}/${dateFormat.year} a las: ${dateFormat.hour}:${dateFormat.minute}"
    }
}