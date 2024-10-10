package com.example.integration2

import ActivityUtils
import LOGGING
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
import java.io.FileWriter
import java.io.IOException
import java.util.Calendar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var joinRoomBTN: Button
    private lateinit var createRoomBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog
    private lateinit var customOverflowIcon: ImageView
    private lateinit var toolbar: Toolbar
    private val contextTAG: String = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joinRoomBTN = findViewById(R.id.join_room_btn_id)
        createRoomBTN = findViewById(R.id.create_room_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)
        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Display application icon in the toolbar
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setLogo(R.drawable.android_os)
        supportActionBar!!.setDisplayUseLogoEnabled(true)

        // Find the custom overflow icon ImageView
        customOverflowIcon = toolbar.findViewById(R.id.custom_overflow_icon)
        customOverflowIcon.setOnClickListener { openCustomMenu() }

        val testing = findViewById<Button>(R.id.testing_btn_id)
        testing.setOnClickListener { startActivity(Intent(this, TestingActivity::class.java)) }

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        joinRoomBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, "joinRoomBTN clicked")
            resultTV.visibility = View.INVISIBLE
            joinRoomDialog()
        }

        createRoomBTN.setOnClickListener {
            LOGGING.INFO(contextTAG, "createRoomBTN clicked")
            resultTV.visibility = View.INVISIBLE
            createRoomFunction()
        }

        if (userDataViewModel.navigateToLoginActivity) {
            ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
        } else if (!userDataViewModel.isRoomLengthLessThanOne) {
            ActivityUtils.navigateToActivity(this, Intent(this, RoomActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LOGGING.INFO(contextTAG, "onBackPressed clicked")
                finishAffinity()
            }
        })

    }

    private fun openCustomMenu() {
        val popupMenu = PopupMenu(this, customOverflowIcon)
        popupMenu.inflate(R.menu.toolbar_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    ActivityUtils.navigateToActivity(
                        this,
                        Intent(this, EditDetailsActivity::class.java)
                    )
                    true
                }

                R.id.menu_relaunch -> {
                    ActivityUtils.relaunch(this)
                    true
                }

                R.id.menu_contact_us -> {
                    ActivityUtils.navigateToActivity(
                        this,
                        Intent(this, ContactUsActivity::class.java)
                    )
                    true
                }

                R.id.menu_about -> {
                    ActivityUtils.showAboutDialog(this)
                    true
                }

                R.id.menu_logout -> {
                    LOGGING.INFO(contextTAG, "User Chosen to Logout from menu")
                    ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun joinRoomDialog() {

        val builderR = AlertDialog.Builder(this)
        val inflaterR = layoutInflater
        val viewR = inflaterR.inflate(R.layout.join_room_dialog_layout, null)
        val roomId = viewR.findViewById<EditText>(R.id.roomId_et_id)
        val cancelBTN = viewR.findViewById<Button>(R.id.cancel_btn_id)
        val joinBTN = viewR.findViewById<Button>(R.id.join_room_btn_id)
        val roomIdTIL = viewR.findViewById<TextInputLayout>(R.id.roomId_til_id)
        roomIdTIL.setStartIconTintList(null)
        builderR.setView(viewR)
        val alertDialogR = builderR.create()

        joinBTN.setOnClickListener {
            val enteredText = roomId.text.toString()
            if (enteredText.isEmpty()) {
                roomId.error = getString(R.string.roomId_should_not_be_empty)
            } else {
                alertDialogR.dismiss()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.you_entered, enteredText)
                joinRoomFunction(enteredText)
            }

        }

        cancelBTN.setOnClickListener {
            alertDialogR.dismiss()
        }
        alertDialogR.show()
    }

    private fun joinRoomFunction(roomID: String) {

        animationView.setAnimation(R.raw.profile_loading)
        animationView.playAnimation()
        alertDialog.show()
        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, "Join Room, Got response = $response")
                extractRoomJoiningJsonData(response, roomID)
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
            },
            { error ->
                LOGGING.INFO(contextTAG, " Join Room, Error = $error")
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "joinRoom",
                    "roomId" to roomID,
                    "userId" to userDataViewModel.userId
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }

    private fun extractRoomJoiningJsonData(jsonResponse: String, roomID: String) {

        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            if (jsonArray.length() > 0) {

                val roomIdStatus: String
                val result: String
                val jsonItem = jsonArray.getJSONObject(0)
                roomIdStatus = jsonItem.getBoolean("roomID_status").toString()
                result = jsonItem.getString("result").toString()

                when {
                    result.toBoolean() -> {
                        animationView.setAnimation(R.raw.protected_shield)
                        animationView.playAnimation()
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        LOGGING.INFO(
                            contextTAG,
                            " Room Joining Success - userID = ${jsonItem.getString("id")}, RoomID = ${
                                jsonItem.getString("roomId")
                            }"
                        )
                        storeRoomId(roomID)
                        Handler(Looper.getMainLooper()).postDelayed({
                            ActivityUtils.navigateToActivity(
                                this,
                                Intent(this, RoomActivity::class.java)
                            )
                        }, 2000)
                    }

                    !roomIdStatus.toBoolean() -> {
                        LOGGING.INFO(
                            contextTAG,
                            "Room Joining Failed with Invalid RoomID = ${getString(R.string.invalid_roomId)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        Toast.makeText(this, "Invalid Room ID", Toast.LENGTH_SHORT).show()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.invalid_roomId)

                    }

                    else -> {
                        LOGGING.INFO(
                            contextTAG,
                            " Room Joining Failed, Reason - ${getString(R.string.something_went_wrong)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                LOGGING.INFO(
                    contextTAG,
                    " Room Joining Failed, Reason - ${getString(R.string.no_data_found)}"
                )
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createRoomFunction() {

        animationView.setAnimation(R.raw.love_is_blind)
        animationView.playAnimation()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, "Create Room, Got response = $response")
                extractRoomCreationJsonData(response)
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
            },
            { error ->
                LOGGING.INFO(contextTAG, "Create Room, Got Error = $error")
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()

            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "createRoom",
                    "userId" to userDataViewModel.userId,
                    "roomId" to createRoomId(),
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }

    private fun createRoomId(): String {
        val calendar = Calendar.getInstance()

        // Extract date components
        val year = calendar.get(Calendar.YEAR) % 100 // Take last two digits
        val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based, so add 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val random = generateRandomString()

        val finalRoomId = "$minute$year${random[0]}$hour$month${random[1]}$day$second${random[2]}"
        LOGGING.INFO(contextTAG, "Created Room ID = $finalRoomId")

        return finalRoomId
    }

    private fun generateRandomString(): String {
        val alphabet = getString(R.string.alphabets)
        val random = Random(System.currentTimeMillis())

        return (1..3)
            .map { alphabet[random.nextInt(0, alphabet.length)] }
            .joinToString("")
    }

    private fun extractRoomCreationJsonData(jsonResponse: String) {
        val userIdStatus: String
        val createRoomStatus: String
        val roomID: String
        val adminStatus: String

        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            if (jsonArray.length() > 0) {

                val jsonItem = jsonArray.getJSONObject(0)
                userIdStatus = jsonItem.getBoolean("userID_status").toString()
                createRoomStatus = jsonItem.getBoolean("result").toString()
                roomID = jsonItem.getString("roomID").toString()
                adminStatus = jsonItem.getBoolean("adminStatus").toString()

                when {
                    createRoomStatus.toBoolean() -> {
                        LOGGING.INFO(contextTAG, "Room Creation Success in Google Spreadsheets")
                        animationView.setAnimation(R.raw.done)
                        animationView.playAnimation()
                        storeRoomIdAndAdminStatus(roomID, adminStatus)
                        Toast.makeText(this, "Room Created", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            ActivityUtils.navigateToActivity(
                                this,
                                Intent(this, RoomActivity::class.java)
                            )
                        }, 2000)
                    }

                    !userIdStatus.toBoolean() -> {
                        LOGGING.INFO(
                            contextTAG,
                            "Room creation failed, Reason - ${getString(R.string.user_id_not_found)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.user_id_not_found)
                    }

                    else -> {
                        LOGGING.INFO(
                            contextTAG,
                            "Room creation failed, Reason - ${getString(R.string.something_went_wrong)}"
                        )
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                LOGGING.INFO(
                    contextTAG,
                    "Room Creation failed, Reason - ${getString(R.string.no_data_found)}"
                )
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {
            LOGGING.INFO(contextTAG, "JSONException ${e.message}")
            e.printStackTrace()
        }
    }

    private fun storeRoomIdAndAdminStatus(roomID: String, adminStatus: String) {
        try {

            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)

            userData.put("roomId", roomID)
            userData.put("adminStatus", adminStatus)

            FileWriter(ActivityUtils.userDataFile).use { fileWriter ->
                fileWriter.write(userData.toString())
                fileWriter.flush()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun storeRoomId(roomID: String) {
        try {

            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)

            userData.put("roomId", roomID)

            FileWriter(ActivityUtils.userDataFile).use { fileWriter ->
                fileWriter.write(userData.toString())
                fileWriter.flush()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}