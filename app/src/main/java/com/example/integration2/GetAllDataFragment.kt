package com.example.integration2

import ActivityUtils
import CustomAdapter
import Item
import LOGGING
import Section
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GetAllDataFragment : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var listView: ListView
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val groupedItemsJson = JSONObject()
    private lateinit var warningTV: TextView
    private lateinit var roomActivity: RoomActivity
    private val contextTAG: String = "GetAllDataFragment"

    /****** Implement below things ****
    Groceries
    Vegetables
    Non veg (meat)
    Shopping
    Movies
    Party
    Travelling */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_get_all_data, container, false)
        listView = view.findViewById(R.id.lv_items2)
        roomActivity = activity as RoomActivity
        warningTV = view.findViewById(R.id.get_all_data_warning_id)

        getItems()

        // Set item click listener
        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position)
            if (selectedItem is Item) {
                val userName = selectedItem.userName
                val date = selectedItem.date
                val amount = selectedItem.amount
                val fullDescription = selectedItem.fullDescription

                popUpDetails(userName, date, amount, fullDescription)
            }
        }
        return view
    }

    private fun getItems() {
        val roomId = userDataViewModel.roomId
        val param = "?action=getTotal&roomId=$roomId"
        val url = resources.getString(R.string.spreadsheet_url)
        roomActivity.animationView.setAnimation(R.raw.files_loading)
        roomActivity.animationView.playAnimation()
        roomActivity.alertDialog.show()
        val stringRequest = StringRequest(
            Request.Method.GET, url + param,
            { response -> parseItems(response) }
        ) { error ->
            LOGGING.INFO(contextTAG, "Got error = $error")
        }

        val policy: RetryPolicy = DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    private fun parseItems(jsonResponse: String) {
        try {
            val jsonObj = JSONObject(jsonResponse)
            val resultJson = createCustomJson(jsonResponse)
            createAndWriteToFile(resultJson)
            Log.d(contextTAG,resultJson.toString())
            val jsonArray = jsonObj.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val jo1 = jsonArray.getJSONObject(i)

                val jo2 = jo1.getJSONArray("records")

                for (j in 0 until jo2.length()) {
                    val jo = jo2.getJSONObject(j)

                    val dateFormats = convertDateFormat(jo.getString("date"))
                    val monthKey = dateFormats.format2

                    if (groupedItemsJson.has(monthKey)) {
                        val monthData = groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                        val monthTotal = groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", limitDescription(jo.getString("description")))
                            put("position3", dateFormats.format1)
                            put("position4", "₹ ${jo.getString("amount")}")
                            put("position5", jo.getString("dataId"))
                            put("position6", jo.getString("profileId"))
                            put("position7", jo.getString("description"))
                        }

                        monthData.put(newData)
                        groupedItemsJson.getJSONObject(monthKey).put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
                    } else {
                        val newDataArray = JSONArray()
                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", limitDescription(jo.getString("description")))
                            put("position3", dateFormats.format1)
                            put("position4", "₹ ${jo.getString("amount")}")
                            put("position5", jo.getString("dataId"))
                            put("position6", jo.getString("profileId"))
                            put("position7", jo.getString("description"))
                        }
                        newDataArray.put(newData)

                        val monthObject = JSONObject().apply {
                            put("MonthName", monthKey)
                            put("MonthData", newDataArray)
                            put("MonthTotal", jo.getString("amount").toDouble())
                        }

                        groupedItemsJson.put(monthKey, monthObject)
                    }

                }

            }

            val months = groupedItemsJson.keys().asSequence().toList()
            Log.d(contextTAG, "Type of months: ${months.javaClass.simpleName}")
            Log.d(contextTAG, "Type of months: ${months.javaClass.name}")
            Log.d(contextTAG, "Months list size: ${months.size}")
            Log.d(contextTAG, "Months list contents: $months")
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }
            Log.d(contextTAG, "Type of sorted months: ${sortedMonths.javaClass.simpleName}")
            Log.d(contextTAG, "Type of sorted months: ${sortedMonths.javaClass.name}")
            Log.d(contextTAG, "Sorted Months list size: ${sortedMonths.size}")
            Log.d(contextTAG, "Sorted Months list contents: $sortedMonths")

            categorizeItems(sortedMonths)
        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "Got error $e")
            warningTV.visibility = View.VISIBLE
            warningTV.text = jsonResponse
            roomActivity.alertDialog.dismiss()
            e.printStackTrace()
        }
    }

    fun createCustomJson(jsonResponse: String): JSONObject {
        // Parse the input JSON
        val jsonObj = JSONObject(jsonResponse)
        val items = jsonObj.getJSONArray("items")

        // A map to hold the grouped data
        val groupedData = mutableMapOf<String, MutableList<MutableList<Int>>>()

        // Iterate through each user item
        for (i in 0 until items.length()) {
            val userItem = items.getJSONObject(i)
            val records = userItem.getJSONArray("records")

            // Process each record for the current user
            for (j in 0 until records.length()) {
                val record = records.getJSONObject(j)

                // Extract the date and amount
                val date = record.getString("date")
                val amount = record.getInt("amount")

                // Parse the date to get the month and year
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(date)!!

                val monthYearKey = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)

                // Initialize the list for the month/year if it doesn't exist
                if (!groupedData.containsKey(monthYearKey)) {
                    groupedData[monthYearKey] = mutableListOf(mutableListOf(), mutableListOf())
                }

                // Add the amount to the correct list based on userId
                if (userItem.getString("userId") == "m2410P1718R5625Z") {
                    groupedData[monthYearKey]!![0].add(amount) // First user's amounts
                } else {
                    groupedData[monthYearKey]!![1].add(amount) // Second user's amounts
                }
            }
        }

        // Convert the map to JSONObject
        val finalJson = JSONObject()
        for ((key, value) in groupedData) {
            finalJson.put(key, JSONArray(value))
        }

        return finalJson
    }


    private fun parseItems_new(jsonResponse: String) {
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            // Get the months first
            val months = groupedItemsJson.keys().asSequence().toList()

            // Initialize the map to hold user monthly totals
            val userMonthlyTotals = mutableMapOf<String, MutableList<Double>>()

            // Iterate over each item
            for (i in 0 until jsonArray.length()) {
                val jo1 = jsonArray.getJSONObject(i)
                val jo2 = jo1.getJSONArray("records")

                for (j in 0 until jo2.length()) {
                    val jo = jo2.getJSONObject(j)

                    val dateFormats = convertDateFormat(jo.getString("date"))
                    val monthKey = dateFormats.format2

                    // Initialize the user's list in the map if it doesn't exist
                    val userName = jo.getString("userName")
                    if (userMonthlyTotals[userName] == null) {
                        userMonthlyTotals[userName] = MutableList(months.size) { 0.0 } // Create a list for each month
                    }

                    if (groupedItemsJson.has(monthKey)) {
                        val monthData = groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                        val monthTotal = groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", limitDescription(jo.getString("description")))
                            put("position3", dateFormats.format1)
                            put("position4", "₹ ${jo.getString("amount")}")
                            put("position5", jo.getString("dataId"))
                            put("position6", jo.getString("profileId"))
                            put("position7", jo.getString("description"))
                        }

                        monthData.put(newData)

                        // Update user's monthly total for the current month
                        val monthIndex = months.indexOf(monthKey)
                        if (monthIndex != -1) {
                            userMonthlyTotals[userName]!![monthIndex] += jo.getString("amount").toDouble()
                        }

                        groupedItemsJson.getJSONObject(monthKey).put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
                    } else {
                        val newDataArray = JSONArray()
                        val newData = JSONObject().apply {
                            put("position1", jo.getString("userName"))
                            put("position2", limitDescription(jo.getString("description")))
                            put("position3", dateFormats.format1)
                            put("position4", "₹ ${jo.getString("amount")}")
                            put("position5", jo.getString("dataId"))
                            put("position6", jo.getString("profileId"))
                            put("position7", jo.getString("description"))
                        }
                        newDataArray.put(newData)

                        val monthObject = JSONObject().apply {
                            put("MonthName", monthKey)
                            put("MonthData", newDataArray)
                            put("MonthTotal", jo.getString("amount").toDouble())
                        }

                        groupedItemsJson.put(monthKey, monthObject)

                        // Update user's monthly total for the current month
                        val monthIndex = months.indexOf(monthKey)
                        if (monthIndex != -1) {
                            userMonthlyTotals[userName]!![monthIndex] += jo.getString("amount").toDouble()
                        }
                    }
                }
            }

            Log.d(contextTAG, "Months list contents: $months")

            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }

            Log.d(contextTAG, "Sorted Months list contents: $sortedMonths")

            // Prepare monthlyTotal from the userMonthlyTotals map
            val monthlyTotal = userMonthlyTotals.values.toList()
            Log.d(contextTAG, "Monthly Total: $monthlyTotal")

            categorizeItems(sortedMonths)

        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "Got error $e")
            warningTV.visibility = View.VISIBLE
            warningTV.text = jsonResponse
            roomActivity.alertDialog.dismiss()
            e.printStackTrace()
        }
    }


    private fun categorizeItems(months: List<String>) {
        val dataList = mutableListOf<Any>()
        val avatars = intArrayOf(
            R.mipmap.avatar1,
            R.mipmap.avatar2,
            R.mipmap.avatar3,
            R.mipmap.avatar4,
            R.mipmap.avatar5,
            R.mipmap.avatar6,
            R.mipmap.avatar7,
            R.mipmap.avatar8,
            R.mipmap.avatar9,
            R.mipmap.avatar10,
            R.mipmap.avatar11,
            R.mipmap.avatar12
        )
        try {
            for (i in months.indices) {
                val monthJsonObject = groupedItemsJson.getJSONObject(months[i])
                dataList.add(
                    Section(
                        monthJsonObject.getString("MonthName"),
                        monthJsonObject.getString("MonthTotal")
                    )
                )

                val monthData = monthJsonObject.getJSONArray("MonthData")
                for (j in 0 until monthData.length()) {
                    val itemData = monthData.getJSONObject(j)
                    dataList.add(
                        Item(
                            itemData.getString("position1"),
                            itemData.getString("position2"),
                            itemData.getString("position3"),
                            itemData.getString("position4"),
                            itemData.getString("position5"),
                            avatars[itemData.getInt("position6") - 1],
                            itemData.getString("position7")

                        )
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        adapter = CustomAdapter(requireContext(), dataList)
        listView.adapter = adapter

        roomActivity.alertDialog.dismiss()
    }

    private fun limitDescription(description: String): String {
        return if (description.length >= 20) "${description.substring(0, 20)}.." else description
    }

    private data class DateFormats(val format1: String, val format2: String)

    private fun convertDateFormat(dateString: String): DateFormats {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateString) ?: return DateFormats("", "")

        val outputDateFormat1 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val outputDateFormat2 = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val formattedDate1 = outputDateFormat1.format(date)
        val formattedDate2 = outputDateFormat2.format(date)

        return DateFormats(formattedDate1, formattedDate2)
    }


    private fun popUpDetails(userName: String, date: String, amount: String, fullDescription: String) {
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.popup_details, null)
        val userNameD = view1.findViewById<TextView>(R.id.user_confirm_id)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        userNameD.text = getString(R.string.data_id_dialog_DD, userName)
        dateD.text = getString(R.string.date_dialog_DD, date)
        amountD.text = getString(R.string.amount_dialog_DD, amount)
        descriptionD.text = getString(R.string.description_dialog_DD, fullDescription)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.show()
    }

    private fun createAndWriteToFile(jsonObject: JSONObject) {
        try {
            // Ensure the directory exists
            if (!ActivityUtils.directory.exists()) {
                ActivityUtils.directory.mkdirs()
            }

            // Ensure the file exists
            if (!ActivityUtils.roomMontlyExpensesFile.exists()) {
                ActivityUtils.roomMontlyExpensesFile.createNewFile()
            }

            // Write the JSON string to the file
            FileWriter(ActivityUtils.roomMontlyExpensesFile).use { writer ->
                writer.write(jsonObject.toString(4)) // Pretty print with indentation level of 4
            }

            LOGGING.DEBUG(contextTAG, "Successfully wrote JSON to file.")

        } catch (e: IOException) {
            LOGGING.DEBUG(contextTAG, "Writing to file failed: ${e.message}")
            e.printStackTrace()
        }
    }

}