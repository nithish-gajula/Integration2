package com.example.integration2

import ActivityUtils
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout

class EditDetailsActivity : AppCompatActivity() {

    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneNumberET: EditText
    private lateinit var ageET: EditText
    private lateinit var saveBTN: Button
    private lateinit var resultTV: TextView
    private lateinit var resetPasswordTV: TextView
    private lateinit var profileImage : ShapeableImageView
    private lateinit var requestQueue: RequestQueue
    private var contextTAG: String = "EditDetails"
    private lateinit var animationView: LottieAnimationView
    private lateinit var alertDialog: AlertDialog
    private var latestProfileImage : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_details)

        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        profileImage = findViewById(R.id.profile_image_id)
        nameET = findViewById(R.id.name_et_id)
        emailET = findViewById(R.id.email_et_id)
        phoneNumberET = findViewById(R.id.phone_no_et_id)
        ageET = findViewById(R.id.age_et_id)
        saveBTN = findViewById(R.id.save_btn_id)
        resultTV = findViewById(R.id.result_tv_id)
        resetPasswordTV = findViewById(R.id.reset_password_tv_id)
        requestQueue = Volley.newRequestQueue(applicationContext)
        val nameTIL = findViewById<TextInputLayout>(R.id.name_til_id)
        val emailTIL = findViewById<TextInputLayout>(R.id.email_til_id)
        val phoneNumberTIL = findViewById<TextInputLayout>(R.id.phone_no_til_id)
        val ageTIL = findViewById<TextInputLayout>(R.id.age_til_id)
        nameTIL.setStartIconTintList(null)
        emailTIL.setStartIconTintList(null)
        phoneNumberTIL.setStartIconTintList(null)
        ageTIL.setStartIconTintList(null)

        latestProfileImage = userDataViewModel.profileId.toInt()
        profileImage.setImageResource(ActivityUtils.avatars[userDataViewModel.profileId.toInt()-1])


        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        nameET.setText(userDataViewModel.userName)
        emailET.setText(userDataViewModel.email)
        phoneNumberET.setText(userDataViewModel.phoneNumber)
        ageET.setText(userDataViewModel.age)

        profileImage.setOnClickListener { selectImagePopUp() }
        saveBTN.setOnClickListener { savaDataFunction() }
        resetPasswordTV.setOnClickListener { ActivityUtils.navigateToActivity(this, Intent(this, ForgotPasswordActivity::class.java)) }

    }

    private fun selectImagePopUp(){
        Toast.makeText(this, "profile Image Clicked",Toast.LENGTH_SHORT).show()

        val builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.select_profile_image, null)
        val avatar1 = view.findViewById<ShapeableImageView>(R.id.select_avatar_1_id)
        val avatar2 = view.findViewById<ShapeableImageView>(R.id.select_avatar_2_id)
        val avatar3 = view.findViewById<ShapeableImageView>(R.id.select_avatar_3_id)
        val avatar4 = view.findViewById<ShapeableImageView>(R.id.select_avatar_4_id)
        val avatar5 = view.findViewById<ShapeableImageView>(R.id.select_avatar_5_id)
        val avatar6 = view.findViewById<ShapeableImageView>(R.id.select_avatar_6_id)
        val avatar7 = view.findViewById<ShapeableImageView>(R.id.select_avatar_7_id)
        val avatar8 = view.findViewById<ShapeableImageView>(R.id.select_avatar_8_id)
        val avatar9 = view.findViewById<ShapeableImageView>(R.id.select_avatar_9_id)
        val avatar10 = view.findViewById<ShapeableImageView>(R.id.select_avatar_10_id)
        val avatar11 = view.findViewById<ShapeableImageView>(R.id.select_avatar_11_id)
        val avatar12 = view.findViewById<ShapeableImageView>(R.id.select_avatar_12_id)

        builder.setView(view)
        val dialog = builder.create()

        avatar1.setOnClickListener {
            latestProfileImage = 1
            profileImage.setImageDrawable(avatar1.drawable)
            dialog.dismiss()

        }

        avatar2.setOnClickListener {
            latestProfileImage = 2
            profileImage.setImageDrawable(avatar2.drawable)
            dialog.dismiss()

        }

        avatar3.setOnClickListener {
            latestProfileImage = 3
            profileImage.setImageDrawable(avatar3.drawable)
            dialog.dismiss()

        }

        avatar4.setOnClickListener {
            latestProfileImage = 4
            profileImage.setImageDrawable(avatar4.drawable)
            dialog.dismiss()

        }

        avatar5.setOnClickListener {
            latestProfileImage = 5
            profileImage.setImageDrawable(avatar5.drawable)
            dialog.dismiss()

        }

        avatar6.setOnClickListener {
            latestProfileImage = 6
            profileImage.setImageDrawable(avatar6.drawable)
            dialog.dismiss()

        }

        avatar7.setOnClickListener {
            latestProfileImage = 7
            profileImage.setImageDrawable(avatar7.drawable)
            dialog.dismiss()

        }

        avatar8.setOnClickListener {
            latestProfileImage = 8
            profileImage.setImageDrawable(avatar8.drawable)
            dialog.dismiss()

        }

        avatar9.setOnClickListener {
            latestProfileImage = 9
            profileImage.setImageDrawable(avatar9.drawable)
            dialog.dismiss()

        }

        avatar10.setOnClickListener {
            latestProfileImage = 10
            profileImage.setImageDrawable(avatar10.drawable)
            dialog.dismiss()

        }

        avatar11.setOnClickListener {
            latestProfileImage = 11
            profileImage.setImageDrawable(avatar11.drawable)
            dialog.dismiss()

        }

        avatar12.setOnClickListener {
            latestProfileImage = 12
            profileImage.setImageDrawable(avatar12.drawable)
            dialog.dismiss()

        }

        dialog.show()


    }

    private fun savaDataFunction() {
        val name = nameET.text.toString().trim()
        val email = emailET.text.toString().trim()
        val phoneNumber = phoneNumberET.text.toString().trim()
        val age = ageET.text.toString().trim()



        if (name.isEmpty() || name.any { it.isDigit() }) {
            nameET.error = getString(R.string.enter_valid_name)
            return
        }
        if (phoneNumber.length != 10) {
            phoneNumberET.error = getString(R.string.enter_valid_phone_number)
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
                Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                LOGGING.INFO(contextTAG, response)
                alertDialog.dismiss()
                ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
            },
            { error ->
                animationView.setAnimation(R.raw.error)
                animationView.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    LOGGING.INFO(contextTAG,error.toString())
                    alertDialog.dismiss()
                }, 2000)
                resultTV.visibility = View.VISIBLE
                resultTV.text = error.toString()

            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "editDetails",
                    "userId" to userDataViewModel.userId,
                    "roomId" to userDataViewModel.roomId,
                    "userName" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "age" to age,
                    "profileId" to latestProfileImage.toString()
                )
            }
        }
        val socketTimeOut = 50000
        val policy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        requestQueue.add(stringRequest)
    }
}