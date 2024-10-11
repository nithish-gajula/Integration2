package com.example.integration2

import ActivityUtils
import LOGGING
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import kotlin.random.Random

class SignupActivity : AppCompatActivity() {

    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneNumberET: EditText
    private lateinit var ageET: EditText
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var signupBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var loginTV: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog
    private var contextTAG: String = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameET = findViewById(R.id.name_et_id)
        emailET = findViewById(R.id.email_et_id)
        phoneNumberET = findViewById(R.id.phone_no_et_id)
        ageET = findViewById(R.id.age_et_id)
        passwordET = findViewById(R.id.password_et_id)
        confirmPasswordET = findViewById(R.id.confirm_password_et_id)
        signupBTN = findViewById(R.id.signup_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        loginTV = findViewById(R.id.login_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        val nameTIL = findViewById<TextInputLayout>(R.id.name_til_id)
        val emailTIL = findViewById<TextInputLayout>(R.id.email_til_id)
        val phoneNumberTIL = findViewById<TextInputLayout>(R.id.phone_no_til_id)
        val ageTIL = findViewById<TextInputLayout>(R.id.age_til_id)
        val passwordTIL = findViewById<TextInputLayout>(R.id.password_til_id)
        val confirmPasswordTIL = findViewById<TextInputLayout>(R.id.confirm_password_til_id)
        nameTIL.setStartIconTintList(null)
        emailTIL.setStartIconTintList(null)
        phoneNumberTIL.setStartIconTintList(null)
        ageTIL.setStartIconTintList(null)
        passwordTIL.setStartIconTintList(null)
        confirmPasswordTIL.setStartIconTintList(null)

        loginTV.setOnClickListener {
            ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
        }

        signupBTN.setOnClickListener {
            resultTV.visibility = View.INVISIBLE
            signupFunction()
        }
    }

    private fun signupFunction() {
        val name = nameET.text.toString().trim()
        val email = emailET.text.toString().trim()
        val phoneNumber = phoneNumberET.text.toString().trim()
        val password = passwordET.text.toString().trim()
        val age = ageET.text.toString().trim()
        val rePassword = confirmPasswordET.text.toString().trim()

        if (name.isEmpty() || name.any { it.isDigit() }) {
            nameET.error = getString(R.string.enter_valid_name)
            return
        }
        if (!email.endsWith("@gmail.com") || email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(
                email
            ).matches()
        ) {
            emailET.error = getString(R.string.enter_valid_email)
            return
        }
        if (phoneNumber.length != 10) {
            phoneNumberET.error = getString(R.string.enter_valid_phone_number)
            return
        }
        if (password.isEmpty()) {
            passwordET.error = getString(R.string.password_should_not_be_empty)
            return
        }
        if (rePassword.isEmpty()) {
            confirmPasswordET.error = getString(R.string.password_should_not_be_empty)
            return
        }
        if (password != rePassword) {
            confirmPasswordET.error = getString(R.string.password_not_matched)
            return
        }
        if (age.toIntOrNull() !in 10..100) {
            ageET.error = getString(R.string.enter_valid_age)
            return
        }

        animationView.setAnimation(R.raw.profile_loading)
        animationView.playAnimation()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, "Signup Request, Got Response $response")
                extractSignupJsonData(response)
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
            },
            { error ->
                LOGGING.DEBUG(contextTAG, "Signup Request, Got Error $error")
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
                    "action" to "signup",
                    "id" to createUserId(),
                    "userName" to name,
                    "email" to email,
                    "password" to rePassword,
                    "phoneNumber" to phoneNumber,
                    "age" to age
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }

    private fun extractSignupJsonData(jsonResponse: String) {
        val emailStatus: String
        val signupStatus: String

        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            if (jsonArray.length() > 0) {
                val jsonItem = jsonArray.getJSONObject(0)
                emailStatus = jsonItem.getBoolean("email_status").toString()
                signupStatus = jsonItem.getBoolean("result").toString()

                when {
                    signupStatus.toBoolean() -> {
                        animationView.setAnimation(R.raw.protected_shield)
                        animationView.playAnimation()
                        LOGGING.INFO(contextTAG, "Signup Success")
                        Toast.makeText(this, "Signup Success", Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            ActivityUtils.navigateToActivity(
                                this,
                                Intent(this, LoginActivity::class.java)
                            )
                        }, 2000)
                    }

                    !emailStatus.toBoolean() -> {
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.user_already_exist)
                    }

                    else -> {
                        LOGGING.DEBUG(contextTAG, "Signup Failed, Reason - ${getString(R.string.something_went_wrong)}")
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                LOGGING.DEBUG(contextTAG, "Signup Failed, Reason - ${getString(R.string.no_data_found)}")
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "Signup Failed, Reason - ${e.printStackTrace()}")
            Log.e(contextTAG, "Signup Failed, Reason - ${e.printStackTrace()}")
            e.printStackTrace()
        }
    }

    private fun createUserId(): String {
        val calendar = Calendar.getInstance()

        // Extract date components
        val year = calendar.get(Calendar.YEAR) % 100 // Take last two digits
        val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based, so add 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val weekday = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val userChar = nameET.text.toString()[0]
        val random = generateRandomString()

        val finalUserID = "${random[0]}$year$month$userChar$day$hour${random[1]}$weekday$minute$second${random[2]}"
        LOGGING.INFO(contextTAG, "UserID created as $finalUserID")

        return finalUserID
    }

    private fun generateRandomString(): String {
        val alphabet = getString(R.string.alphabets)
        val random = Random(System.currentTimeMillis())
        return (1..3)
            .map { alphabet[random.nextInt(0, alphabet.length)] }
            .joinToString("")
    }
}