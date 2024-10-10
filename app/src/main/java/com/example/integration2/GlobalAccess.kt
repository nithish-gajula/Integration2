package com.example.integration2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GlobalAccess {

    var isUserAddedNewData: Boolean = false


    fun convertDateFormat(dateString: String): String {
        // Parsing the input date string
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateString) ?: return ""

        // Formatting the date to "dd MMM yyyy" format
        val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return outputDateFormat.format(date)
    }

}