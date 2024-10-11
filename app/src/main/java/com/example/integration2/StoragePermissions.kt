package com.example.integration2

import ActivityUtils
import LOGGING
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class StoragePermissions : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 101
    private val contextTAG: String = "StoragePermissions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_permissions)

        checkStoragePermissions()

    }

    private fun checkStoragePermissions() {
        // Check if both READ and WRITE permissions are granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            // Permissions are already granted, navigate to MainActivity
            setupFilesAndDirectories() // Call to set up files and directories
            navigateToMainActivity()
        } else {
            // Request the permissions
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), STORAGE_PERMISSION_CODE
            )
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            // Check if both permissions were granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, navigate to MainActivity
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
                setupFilesAndDirectories() // Call to set up files and directories
                navigateToMainActivity()
            } else {
                // Permissions denied, show a message and close the app
                Toast.makeText(this, "Permissions Denied. Closing the app.", Toast.LENGTH_SHORT)
                    .show()
                finish() // Close the app
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close StoragePermissions activity
    }

    private fun setupFilesAndDirectories() {

        // Get the directory path
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            ActivityUtils.directoryName
        )

        // Check if directory exists, if not create it
        if (!directory.exists()) {
            val dirCreated = directory.mkdirs()
            if (dirCreated) {
                Log.d("StoragePermissions", "Directory created: ${directory.absolutePath}")
            } else {
                Log.e("StoragePermissions", "Failed to create directory: ${directory.absolutePath}")
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            Log.d("StoragePermissions", "Directory already exists: ${directory.absolutePath}")
        }

        // List of files to create
        val reportedLogsFile = File(directory, ActivityUtils.reportedLogsFileName)
        val reportedReadmeLogsFile = File(directory, ActivityUtils.reportedReadmeLogsFileName)
        val userDataFile = File(directory, ActivityUtils.userDataFileName)

        // Create files if they don't exist
        createFileIfNotExists(reportedLogsFile)
        createFileIfNotExists(reportedReadmeLogsFile)
        createFileIfNotExists(userDataFile)
    }

    private fun createFileIfNotExists(file: File) {
        if (!file.exists()) {
            try {
                val fileCreated = file.createNewFile()
                if (fileCreated) {
                    Log.i("StoragePermissions", "File created: ${file.absolutePath}")
                    LOGGING.INFO(contextTAG, "File created: ${file.absolutePath}")
                } else {
                    Log.e("StoragePermissions", "Failed to create file: ${file.absolutePath}")
                }
            } catch (e: IOException) {
                Log.e("StoragePermissions", "Error creating file: ${file.absolutePath}", e)
            }
        } else {
            Log.d("StoragePermissions", "File already exists: ${file.absolutePath}")
        }
    }
}
