package com.example.integration2

import ActivityUtils
import CustomAdapter
import Item
import LOGGING
import Section
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetDataFragment : Fragment() {
    private var adapter: ListAdapter? = null
    private lateinit var listView: ListView
    private lateinit var totalAmountTV: TextView
    private var totalAmount: Double = 0.0
    private lateinit var warningTV: TextView
    private lateinit var roomActivity: RoomActivity
    private var bottomSheetState: Boolean = true
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private var groupedItemsJson = JSONObject()
    private val contextTAG: String = "GetDataFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_get_data, container, false)
        listView = v.findViewById(R.id.lv_items)
        totalAmountTV = v.findViewById(R.id.total_Amount_id)
        warningTV = v.findViewById(R.id.get_data_warning_id)
        roomActivity = activity as RoomActivity
        val btmsheet = v.findViewById<FrameLayout>(R.id.bottom_sheet_id)

        BottomSheetBehavior.from(btmsheet).apply {
            peekHeight = 0
            this.state = BottomSheetBehavior.STATE_EXPANDED
        }

        if (GlobalAccess.isUserAddedNewData) {
            getItems()
        } else {
            loadUserExpensesFromStorage()
        }

//        // Set item click listener
        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position)
            if (selectedItem is Item) {
                val userName = selectedItem.userName
                val date = selectedItem.date
                val amount = selectedItem.amount
                val id = selectedItem.id
                val fullDescription = selectedItem.fullDescription

                delete(id, userName, date, amount, fullDescription)
            } else {
                bottomSheetState =
                    BottomSheetBehavior.STATE_EXPANDED == BottomSheetBehavior.from(btmsheet).state

                if (!bottomSheetState) {
                    BottomSheetBehavior.from(btmsheet).state = BottomSheetBehavior.STATE_EXPANDED
                    bottomSheetState = true
                } else {
                    BottomSheetBehavior.from(btmsheet).state = BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheetState = false
                }
            }
        }
        return v
    }

    private fun getItems() {

        val userId = userDataViewModel.userId
        val roomId = userDataViewModel.roomId

        val param = "?action=getItem&userId=$userId&roomId=$roomId"
        val url = resources.getString(R.string.spreadsheet_url)
        roomActivity.animationView.setAnimation(R.raw.card_loading)
        roomActivity.animationView.playAnimation()
        roomActivity.alertDialog.show()
        val stringRequest = StringRequest(
            Request.Method.GET, url + param,
            { response ->
                FileWriter(ActivityUtils.userExpensesFile).use { it.write(response) }
                loadUserExpensesFromStorage()
            }
        ) { error ->
            LOGGING.DEBUG(contextTAG, "Get Expenses, Got error = $error")
        }
        val socketTimeOut = 50000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.setRetryPolicy(policy)
        val queue = Volley.newRequestQueue(activity)
        queue.add(stringRequest)
    }

    private fun loadUserExpensesFromStorage() {
        try {
            if (!ActivityUtils.userExpensesFile.exists()) {
                ActivityUtils.userExpensesFile.createNewFile()
                getItems()
            } else {
                val content = ActivityUtils.userExpensesFile.readText()
                parseItems(content)
            }

        } catch (e: IOException) {
            LOGGING.DEBUG(contextTAG, "Loading User Expenses from storage error - $e")
            e.printStackTrace()
        }
    }

    private fun parseItems(jsonResponse: String) {
        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val jo = jsonArray.getJSONObject(i)

                val dateFormats = convertDateFormat(jo.getString("date"))
                val monthKey = dateFormats.format2

                if (groupedItemsJson.has(monthKey)) {
                    val monthData =
                        groupedItemsJson.getJSONObject(monthKey).getJSONArray("MonthData")
                    val monthTotal =
                        groupedItemsJson.getJSONObject(monthKey).getDouble("MonthTotal")

                    val newData = JSONObject().apply {
                        put("position1", userDataViewModel.userName)
                        put("position2", limitDescription(jo.getString("description")))
                        put("position3", dateFormats.format1)
                        put("position4", "₹ ${jo.getString("amount")}")
                        put("position5", jo.getString("dataId"))
                        put("position6", jo.getString("foodId"))
                        put("position7", jo.getString("description"))
                    }
                    totalAmount += jo.getString("amount").toDouble()
                    monthData.put(newData)
                    groupedItemsJson.getJSONObject(monthKey)
                        .put("MonthTotal", monthTotal + jo.getString("amount").toDouble())
                } else {
                    val newDataArray = JSONArray()
                    val newData = JSONObject().apply {
                        put("position1", userDataViewModel.userName)
                        put("position2", limitDescription(jo.getString("description")))
                        put("position3", dateFormats.format1)
                        put("position4", "₹ ${jo.getString("amount")}")
                        put("position5", jo.getString("dataId"))
                        put("position6", jo.getString("foodId"))
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

            val months = groupedItemsJson.keys().asSequence().toList()
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val dateList = months.map { dateFormat.parse(it) }
            val sortedDescending = dateList.sortedDescending()
            val sortedMonths = sortedDescending.map { dateFormat.format(it) }

            categorizeItems(sortedMonths)
        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "error - $e")
            warningTV.visibility = View.VISIBLE
            warningTV.text = jsonResponse
            roomActivity.alertDialog.dismiss()
            e.printStackTrace()
        }
    }

    private fun categorizeItems(months: List<String>) {
        val dataList = mutableListOf<Any>()
        val avatars = intArrayOf(
            R.drawable.food_1,
            R.drawable.food_2,
            R.drawable.food_3,
            R.drawable.food_4,
            R.drawable.food_5,
            R.drawable.food_6,
            R.drawable.food_7,
            R.drawable.food_8,
            R.drawable.food_9,
            R.drawable.food_10,
            R.drawable.food_11,
            R.drawable.food_12,
            R.drawable.food_13,
            R.drawable.food_14,
            R.drawable.food_15,
            R.drawable.food_16
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
                            avatars[itemData.getString("position6").toInt() - 1],
                            itemData.getString("position7")
                        )
                    )
                }
            }
        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "error - $e")
            e.printStackTrace()
        }

        adapter = CustomAdapter(requireContext(), dataList)
        listView.adapter = adapter
        roomActivity.alertDialog.dismiss()
        totalAmountTV.text = totalAmount.toString()
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

    private fun delete(
        id: String,
        userName: String,
        date: String,
        amount: String,
        fullDescription: String
    ) {
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        val userNameD = view1.findViewById<TextView>(R.id.user_confirm_id)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        val cancel = view1.findViewById<Button>(R.id.cancel_confirm_id)
        val deleteBTN = view1.findViewById<Button>(R.id.confirm_confirm_id)
        val ll1 = view1.findViewById<LinearLayout>(R.id.ll1)
        val anm1 = view1.findViewById<LottieAnimationView>(R.id.lottie_animation_1)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        userNameD.text = getString(R.string.data_id_dialog_DD, userName)
        dateD.text = getString(R.string.date_dialog_DD, date)
        amountD.text = getString(R.string.amount_dialog_DD, amount)
        descriptionD.text = getString(R.string.description_dialog_DD, fullDescription)
        dialog1.setCanceledOnTouchOutside(false)
        dialog1.show()

        deleteBTN.setOnClickListener {
            ll1.visibility = View.GONE
            anm1.visibility = View.VISIBLE
            anm1.setAnimation(R.raw.please_wait)
            anm1.playAnimation()
            val url = resources.getString(R.string.spreadsheet_url)
            LOGGING.INFO(contextTAG, "Requesting delete item -> userId=${userDataViewModel.userId}, roomId=${userDataViewModel.roomId}, rowID=$id")
            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        LOGGING.INFO(contextTAG, "Delete Item Request, Got Response $response")
                        anm1.setAnimation(R.raw.delete_file)
                        anm1.playAnimation()
                        Handler(Looper.getMainLooper()).postDelayed({
                            adapter = null
                            dialog1.dismiss()
                            getItems()
                        }, 2000)
                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener { error ->
                        LOGGING.DEBUG(contextTAG, "Delete Item Request, Got error $error")
                        anm1.setAnimation(R.raw.error)
                        anm1.playAnimation()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val param: MutableMap<String, String> =
                            HashMap()
                        //here we pass params
                        param["action"] = "deleteItem"
                        param["userId"] = userDataViewModel.userId
                        param["roomId"] = userDataViewModel.roomId
                        param["dataId"] = id
                        return param
                    }
                }
            val socketTimeOut = 50000 // u can change this .. here it is 50 seconds
            val retryPolicy: RetryPolicy =
                DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            stringRequest.setRetryPolicy(retryPolicy)
            val queue = Volley.newRequestQueue(activity)
            queue.add(stringRequest)
        }
        cancel.setOnClickListener { dialog1.dismiss() }
    }


}