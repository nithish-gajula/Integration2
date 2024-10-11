package com.example.integration2

import ActivityUtils
import LOGGING
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class RoomActivity : AppCompatActivity() {

    private lateinit var userDataViewModel: UserDataViewModel
    private var pressedTime: Long = 0
    lateinit var animationView: LottieAnimationView
    lateinit var alertDialog: AlertDialog
    private lateinit var customOverflowIcon: ImageView
    private lateinit var chipNavigationBar: ChipNavigationBar
    private lateinit var toolbar: Toolbar
    private val contextTAG: String = "RoomActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setLogo(R.drawable.android_os)
        supportActionBar!!.setDisplayUseLogoEnabled(true)

        customOverflowIcon = toolbar.findViewById(R.id.custom_overflow_icon)
        customOverflowIcon.setOnClickListener {
            openCustomMenu()
        }

        chipNavigationBar = findViewById(R.id.bottom_menu_id2)
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DefaultFragment()).commit()

        chipNavigationBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.addData -> {
                    replaceFragment(AddDataFragment())
                    toolbar.title = "  Add Data"
                }

                R.id.myData -> {
                    replaceFragment(GetDataFragment())
                    toolbar.title = "  My Expenses"
                }

                R.id.roomData -> {
                    replaceFragment(GetAllDataFragment())
                    toolbar.title = "  Room Expenses"
                }

                R.id.profile -> {
                    replaceFragment(StatisticsFragment())
                    toolbar.title = "  Statistics"
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pressedTime + 2000 > System.currentTimeMillis()) {
                    finishAffinity()
                } else {
                    Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT)
                        .show()
                }
                pressedTime = System.currentTimeMillis()
            }
        })

    }

    private fun openCustomMenu() {
        val popupMenu = PopupMenu(this, customOverflowIcon)
        popupMenu.inflate(R.menu.toolbar_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    ActivityUtils.navigateToActivity(this, Intent(this, EditDetailsActivity::class.java))
                    true
                }

                R.id.menu_relaunch -> {
                    ActivityUtils.relaunch(this)
                    true
                }

                R.id.menu_contact_us -> {
                    ActivityUtils.navigateToActivity(this, Intent(this, ContactUsActivity::class.java))
                    true
                }

                R.id.view_logs -> {
                    ActivityUtils.navigateToActivity(this, Intent(this, TestingActivity::class.java))
                    true
                }

                R.id.menu_about -> {
                    ActivityUtils.showAboutDialog(this)
                    true
                }

                R.id.menu_logout -> {
                    LOGGING.ERROR(contextTAG, "User Logged out from Menu")
                    ActivityUtils.navigateToActivity(this, Intent(this, LoginActivity::class.java))
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()
    }
}
