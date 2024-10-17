package com.example.integration2

import LOGGING
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import kotlin.math.abs

class AddDataFragment : Fragment() {
    private lateinit var simplifiedDescription: String
    private lateinit var amount: EditText
    private lateinit var description: EditText
    private lateinit var date: EditText
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private lateinit var id: String
    private lateinit var userName: String
    private lateinit var roomId: String
    private lateinit var viewPager2: ViewPager2
    private lateinit var imageList: ArrayList<Int>
    private lateinit var adapter: ImageAdapter
    private lateinit var uploadData: Button
    private lateinit var clearData: Button
    private lateinit var dateTil: TextInputLayout
    private lateinit var amountTil: TextInputLayout
    private lateinit var descriptionTil: TextInputLayout
    private var foodId: Int = 0
    private val contextTAG: String = "AddDataFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_data, container, false)

        uploadData = view.findViewById(R.id.uploadid)
        clearData = view.findViewById(R.id.clear_id)
        amount = view.findViewById(R.id.amountid)
        description = view.findViewById(R.id.descriptionid)
        date = view.findViewById(R.id.dateid)
        viewPager2 = view.findViewById(R.id.viewpagerImageSlider_id)
        dateTil = view.findViewById(R.id.date_til)
        amountTil = view.findViewById(R.id.amount_til)
        descriptionTil = view.findViewById(R.id.description_til)

        onCreateSetup()

        return view
    }

    private fun onCreateSetup() {

        id = userDataViewModel.userId
        userName = userDataViewModel.userName
        roomId = userDataViewModel.roomId

        imageList = ArrayList()
        imageList.add(R.drawable.food_1)
        imageList.add(R.drawable.food_2)
        imageList.add(R.drawable.food_3)
        imageList.add(R.drawable.food_4)
        imageList.add(R.drawable.food_5)
        imageList.add(R.drawable.food_6)
        imageList.add(R.drawable.food_7)
        imageList.add(R.drawable.food_8)
        imageList.add(R.drawable.food_9)
        imageList.add(R.drawable.food_10)
        imageList.add(R.drawable.food_11)
        imageList.add(R.drawable.food_12)
        imageList.add(R.drawable.food_13)
        imageList.add(R.drawable.food_14)
        imageList.add(R.drawable.food_15)
        imageList.add(R.drawable.food_16)

        adapter = ImageAdapter(imageList)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        dateTil.setStartIconTintList(null)
        amountTil.setStartIconTintList(null)
        descriptionTil.setStartIconTintList(null)
        val calendar = Calendar.getInstance()
        val year1 = calendar[Calendar.YEAR]
        val month1 = calendar[Calendar.MONTH]
        val date1 = calendar[Calendar.DAY_OF_MONTH]

        date.requestFocus()
        date.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _, year, month, day ->
                    val monthVar = month + 1
                    val date2 = String.format("%02d/%02d/%04d", day, monthVar, year)
                    date.error = null
                    date.setText(date2)
                }, year1, month1, date1
            )
            datePickerDialog.show()
        }

        uploadData.setOnClickListener {
            simplifiedDescription = description.text.toString().trim { it <= ' ' }
            if (date.text.toString().trim { it <= ' ' }.length < 10 || date.text.toString()
                    .trim { it <= ' ' }.length > 10
            ) {
                date.error = "Date wrongly formatted"
            } else if (!date.text.toString().trim { it <= ' ' }.contains("/")) {
                date.error = "Date format DD/MM/YYYY"
            } else if (amount.text.toString().trim { it <= ' ' }.isEmpty()) {
                amount.error = "Amount should not be empty"
            } else if (simplifiedDescription.isEmpty()) {
                description.error = "Description should not be empty"
            } else {
                upload()
            }
        }

        clearData.setOnClickListener {
            date.setText("")
            amount.setText("")
            description.setText("")
        }

        setupTransformer()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                foodId = position + 1
            }
        })
    }

    private fun setupTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
        viewPager2.setPageTransformer(transformer)
    }

    private fun dateFormat(dateString: String): String {
        // Split the date string into day, month, and year components
        val dateComponents = dateString.split("/".toRegex())
        val day = dateComponents[0]
        val month = dateComponents[1]
        val year = dateComponents[2]
        // Return the date string in "MM/DD/YYYY" format
        return "$month/$day/$year"
    }


    private fun upload() {
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        val cancel = view1.findViewById<Button>(R.id.cancel_confirm_id)
        val upload = view1.findViewById<Button>(R.id.confirm_confirm_id)
        val ll1 = view1.findViewById<LinearLayout>(R.id.ll1)
        val animationView = view1.findViewById<LottieAnimationView>(R.id.lottie_animation_1)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        val dateVal = date.text.toString().trim { it <= ' ' }
        val amountVal = amount.text.toString().trim { it <= ' ' }
        val descriptionVal = description.text.toString()

        dateD.text = dateVal
        amountD.text = getString(R.string.amount_entered_AD, amountVal)
        descriptionD.text = getString(R.string.description_entered_AD, descriptionVal)
        dialog1.setCanceledOnTouchOutside(false)
        upload.setOnClickListener {
            val url = resources.getString(R.string.spreadsheet_url)
            ll1.visibility = View.GONE
            animationView.visibility = View.VISIBLE
            animationView.setAnimation(R.raw.meditation_wait_please)
            animationView.playAnimation()
            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        LOGGING.INFO(contextTAG, "Upload Data, Got Response - $response")
                        animationView.setAnimation(R.raw.done)
                        animationView.playAnimation()
                        Toast.makeText(
                            requireActivity().applicationContext,
                            response,
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog1.dismiss()
                            amount.setText("")
                            description.setText("")
                            date.setText("")
                            GlobalAccess.isUserAddedNewData = true
                        }, 2000)
                    },
                    Response.ErrorListener { error ->
                        LOGGING.DEBUG(contextTAG, "Upload Data, Got Error - $error")
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return hashMapOf(
                            "action" to "addItem",
                            "userId" to id,
                            "roomId" to roomId,
                            "userName" to userName,
                            "date" to dateFormat(dateVal),
                            "amount" to amountVal,
                            "description" to descriptionVal,
                            "foodId" to foodId.toString(),
                            "profileId" to userDataViewModel.profileId
                        )
                    }
                }
            val socketTimeOut = 50000 // u can change this .. here it is 50 seconds
            val retryPolicy: RetryPolicy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            stringRequest.setRetryPolicy(retryPolicy)
            val queue = Volley.newRequestQueue(requireActivity().applicationContext)
            queue.add(stringRequest)
            date.setText("")
            amount.setText("")
            description.setText("")

        }
        cancel.setOnClickListener { dialog1.dismiss() }
        dialog1.show()
    }

}