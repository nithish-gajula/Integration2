package com.example.integration2

import LOGGING
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class ContactUsActivity : AppCompatActivity() {

    private lateinit var selectImageTV: TextView
    private lateinit var send: Button
    private lateinit var emailBody: EditText
    private lateinit var uri: Uri
    private lateinit var previewImageIV: ImageView
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var dropdownLayout: TextInputLayout
    private var category: String = " "
    private val contextTAG: String = "ContactUsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        selectImageTV = findViewById(R.id.add_attachment_tv_id)
        previewImageIV = findViewById(R.id.IVPreviewImage)
        send = findViewById(R.id.Bsend)
        emailBody = findViewById(R.id.emailbodyid)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        dropdownLayout = findViewById(R.id.dropdown_layout)
        val description = findViewById<TextInputLayout>(R.id.txtInputLayout)
        description.setStartIconTintList(null)

        selectImageTV.setOnClickListener { imageChooser() }

        val options = arrayOf("Feedback", "Help", "Report Bugs")

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        autoCompleteTextView.setAdapter(adapter)

        // Set a listener to capture the selected value
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            category = parent.getItemAtPosition(position) as String
            dropdownLayout.error = null // Clear error if there was any
        }

        send.setOnClickListener {
            if (emailBody.text.toString().trim().isEmpty()) {
                emailBody.error = "Comments should not be empty"
            } else if (previewImageIV.drawable == null) {
                emailBody.error = "Image not chosen"
            } else if (category.trim().isEmpty()) {
                autoCompleteTextView.error = "Select Category"
            } else {
                mailTo(emailBody.text.toString(), uri)
            }
        }
    }

    private val imageChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val selectedImageUri = intent.data
                    if (selectedImageUri != null) {
                        previewImageIV.setImageURI(selectedImageUri)
                        selectImageTV.visibility = View.GONE
                        previewImageIV.visibility = View.VISIBLE
                        uri = selectedImageUri
                    }
                }
            }
        }

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        imageChooser.launch(Intent.createChooser(i, "Select Picture"))
    }


    private fun mailTo(emailBody: String?, bm: Uri?) {
        val intent = Intent(Intent.ACTION_SEND)
        val recipients = arrayOf("gajulanithish000@gmail.com")
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
        intent.putExtra(Intent.EXTRA_SUBJECT, category)
        intent.putExtra(Intent.EXTRA_TEXT, emailBody)
        intent.putExtra(Intent.EXTRA_CC, "mailcc@gmail.com")
        if (bm != null) {
            intent.putExtra(Intent.EXTRA_STREAM, bm)
        }
        intent.setType("text/html")
        intent.setPackage("com.google.android.gm")
        LOGGING.INFO(contextTAG, "Requested to send a mail")
        startActivity(Intent.createChooser(intent, "Send mail"))
    }
}