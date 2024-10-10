package com.example.integration2

import ActivityUtils
import LOGGING
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class UserDataViewModel : ViewModel() {

    var isRoomLengthLessThanOne = false
    var navigateToLoginActivity = false

    lateinit var userId: String
    lateinit var userName: String
    lateinit var age: String
    lateinit var email: String
    lateinit var phoneNumber: String
    private lateinit var loginTime: String
    lateinit var roomId: String
    private lateinit var adminStatus: String
    lateinit var profileId: String
    private val contextTAG: String = "UserDataViewModel"

    init {
        loadUserData()
    }

    private fun loadUserData() {

        // Check if directory exists, if not, handle it
        if (!ActivityUtils.directory.exists()) {
            ActivityUtils.directory.mkdirs() // Create the directory if not present
        }

        // Check if the user data file exists, if not, handle it
        if (!ActivityUtils.userDataFile.exists()) {
            ActivityUtils.userDataFile.createNewFile() // Create the file if not present
        }

        try {
            val content = ActivityUtils.userDataFile.readText()
            val userData = JSONObject(content)

            userId = userData.getString("id")
            userName = userData.getString("userName")
            age = userData.getString("age")
            email = userData.getString("email")
            phoneNumber = userData.getString("phoneNumber")
            loginTime = userData.getString("loginTime")
            roomId = userData.getString("roomId")
            adminStatus = userData.getString("adminStatus")
            profileId = userData.getString("profileId")

            val info = """
                userid : $userId
                userName :  $userName
                age :  $age
                email :  $email
                phoneNumber :  $phoneNumber
                loginTime :  $loginTime
                roomId :  $roomId
                adminStatus :  $adminStatus
                profileId :  $profileId
            """.trimIndent()

            LOGGING.INFO(contextTAG, info)

            if (roomId.length <= 1) {
                isRoomLengthLessThanOne = true
            }

        } catch (e: JSONException) {
            navigateToLoginActivity = true
        } catch (e: IOException) {
            e.printStackTrace()
            navigateToLoginActivity = true
        }
    }
}
