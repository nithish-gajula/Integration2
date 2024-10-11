package com.example.integration2

import ActivityUtils
import LOGGING
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var resetBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog

    private val contextTAG: String = "ForgotPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailET = findViewById(R.id.email_et_id)
        passwordET = findViewById(R.id.password_et_id)
        confirmPasswordET = findViewById(R.id.confirm_password_et_id)
        resetBTN = findViewById(R.id.reset_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        val emailTIL = findViewById<TextInputLayout>(R.id.email_til_id)
        val passwordTIL = findViewById<TextInputLayout>(R.id.password_til_id)
        val confirmPasswordTIL = findViewById<TextInputLayout>(R.id.confirm_password_til_id)
        emailTIL.setStartIconTintList(null)
        passwordTIL.setStartIconTintList(null)
        confirmPasswordTIL.setStartIconTintList(null)

        resetBTN.setOnClickListener {
            resultTV.visibility = View.INVISIBLE
            resetPasswordFunction()
        }
    }

    private fun resetPasswordFunction() {

        val email = emailET.text.trim().toString()
        val password = passwordET.text.trim().toString()
        val rePassword = confirmPasswordET.text.trim().toString()

        if (!email.endsWith("@gmail.com") || email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(
                email
            ).matches()
        ) {
            emailET.error = getString(R.string.enter_valid_email)
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

        animationView.setAnimation(R.raw.secure_password)
        animationView.playAnimation()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, getString(R.string.spreadsheet_url),
            { response ->
                LOGGING.INFO(contextTAG, "Password reset request, Got Response $response")
                extractResetJsonData(response)
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                }, 2000)
            },
            { error ->
                LOGGING.DEBUG(contextTAG, "Password reset request, Got Error $error")
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
                    "action" to "forgotPassword",
                    "email" to email,
                    "password" to rePassword,
                )
            }
        }

        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }

    private fun extractResetJsonData(jsonResponse: String) {
        val emailStatus: String
        val resetStatus: String

        try {
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("items")

            if (jsonArray.length() > 0) {
                val jsonItem = jsonArray.getJSONObject(0)
                emailStatus = jsonItem.getBoolean("email_status").toString()
                resetStatus = jsonItem.getBoolean("result").toString()

                when {
                    resetStatus.toBoolean() -> {
                        animationView.setAnimation(R.raw.protected_shield)
                        animationView.playAnimation()
                        LOGGING.INFO(contextTAG, "Password Reset Success")
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
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
                        LOGGING.DEBUG(contextTAG, "Password Reset Error, Reason - ${getString(R.string.no_user_data_found)}")
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.no_user_data_found)
                    }

                    else -> {
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                        LOGGING.DEBUG(contextTAG, "Password Reset Error, Reason - ${getString(R.string.something_went_wrong)}")
                        resultTV.visibility = View.VISIBLE
                        resultTV.text = getString(R.string.something_went_wrong)
                    }
                }

            } else {
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                LOGGING.DEBUG(contextTAG, "Password Reset Error, Reason - ${getString(R.string.no_data_found)}")
                resultTV.visibility = View.VISIBLE
                resultTV.text = getString(R.string.no_data_found)
            }
        } catch (e: JSONException) {
            LOGGING.DEBUG(contextTAG, "Password Reset Error, Reason - ${e.printStackTrace()}")
            e.printStackTrace()
        }
    }
}