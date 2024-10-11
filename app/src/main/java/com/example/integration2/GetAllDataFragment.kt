package com.example.integration2

import CustomAdapter
import Item
import LOGGING
import Section
import android.os.Bundle
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
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

        val v = inflater.inflate(R.layout.fragment_get_all_data, container, false)
        listView = v.findViewById(R.id.lv_items2)
        roomActivity = activity as RoomActivity
        warningTV = v.findViewById(R.id.get_all_data_warning_id)

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
        return v
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

        val policy: RetryPolicy =
            DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    private fun parseItems(jsonResponse: String) {
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val jo1 = jsonArray.getJSONObject(i)

                val jo2 = jo1.getJSONArray("records")

                for (j in 0 until jo2.length()) {
                    val jo = jo2.getJSONObject(j)

                    val dateFormats = convertDateFormat(jo.getString("date"))
                    val monthKey = dateFormats.format2

                    if (groupedItemsJson.has(monthKey)) {
                        val monthData =
                            groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                        val monthTotal =
                            groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

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
                        groupedItemsJson.getJSONObject(monthKey)
                            .put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
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
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }

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


    private fun popUpDetails(userName: String, date: String, amount: String, fullDescription: String ) {
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
}