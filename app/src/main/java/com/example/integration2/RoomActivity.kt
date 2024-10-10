package com.example.integration2

import ActivityUtils
import LOGGING
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
    private val contextTAG: String = "RoomActivity"
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Display application icon in the toolbar
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setLogo(R.drawable.android_os)
        supportActionBar!!.setDisplayUseLogoEnabled(true)

        // Find the custom overflow icon ImageView
        customOverflowIcon = toolbar.findViewById(R.id.custom_overflow_icon)
        customOverflowIcon.setOnClickListener {
            // Handle custom overflow icon click event
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
                LOGGING.INFO(contextTAG, "onBackPressed clicked")
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
        // Handle custom overflow menu action
        // You can integrate your existing menu item handling here
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
                R.id.menu_about -> {
                    ActivityUtils.showAboutDialog(this)
                    true
                }
                R.id.menu_logout -> {
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


/*

This is RoomActivity Containing the Navigation drawer view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView


class RoomActivity : AppCompatActivity() {

    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var navigationView: NavigationView
    private lateinit var drawer: DrawerLayout
    private var pressedTime: Long = 0
    lateinit var animationView: LottieAnimationView
    lateinit var alertDialog: AlertDialog
    private val contextTAG: String = "RoomActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.itemIconTintList = null

        val header = navigationView.getHeaderView(0)
        val userProfilePic = header.findViewById<ImageView>(R.id.user_profile_pic)

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.loading_box, null)
        animationView = dialogView.findViewById(R.id.lottie_animation)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_data -> {
                    replaceFragment(AddDataFragment())
                    toolbar.title = "Add Data"
                }
                R.id.nav_individual_data -> {
                    replaceFragment(GetDataFragment())
                    toolbar.title = "Individual Data"
                }
                R.id.nav_all_data -> {
                    replaceFragment(GetAllDataFragment())
                    toolbar.title = "All Data"
                }
                R.id.nav_delete_data -> {
                    replaceFragment(DeleteDataFragment())
                    toolbar.title = "Delete Data"
                }
                R.id.nav_contact_us -> {
                    replaceFragment(ContactUsFragment())
                    toolbar.title = "Contact Us"
                }
                R.id.nav_relaunch -> relaunch()
                R.id.nav_about -> showAboutDialog()
            }
            drawer.closeDrawers()
            true
        }


        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DefaultFragment()).commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LOGGING.INFO(contextTAG, "onBackPressed clicked")
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    if (pressedTime + 2000 > System.currentTimeMillis()) {
                        finishAffinity()
                    } else {
                        Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                    pressedTime = System.currentTimeMillis()
                }
            }
        })

        userProfilePic.setOnClickListener { ActivityUtils.navigateToActivity(this, Intent(this, EditDetailsActivity::class.java)) }
    }


    private fun replaceFragment(fragment: Fragment) {
        LOGGING.INFO(contextTAG, "Entered in replaceFragment Function - $fragment")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commit()
    }

    private fun relaunch() {
        LOGGING.INFO(contextTAG, "Entered in relaunch Function")
        //finish()
        finishAffinity()
        startActivity(intent)
    }

    private fun showAboutDialog() {
        LOGGING.INFO(contextTAG, "Entered in showAboutDialog Function")
        val mView: View = layoutInflater.inflate(R.layout.about, null)
        val tv = mView.findViewById<TextView>(R.id.app_version_id)
        val gmailIMG = mView.findViewById<ImageView>(R.id.gmail_img_id)
        val githubIMG = mView.findViewById<ImageView>(R.id.github_img_id)
        val instagramIMG = mView.findViewById<ImageView>(R.id.instagram_img_id)
        val linkedinIMG = mView.findViewById<ImageView>(R.id.linkedin_img_id)
        tv.text = resources.getString(R.string.version)

        AlertDialog.Builder(this)
            .setView(mView)
            .setCancelable(true)
            .show()

        gmailIMG.setOnClickListener {
            val uri = Uri.parse(getString(R.string.email_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        githubIMG.setOnClickListener {
            val uri = Uri.parse(getString(R.string.github_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        instagramIMG.setOnClickListener {
            val uri = Uri.parse(getString(R.string.instagram_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        linkedinIMG.setOnClickListener {
            val uri = Uri.parse(getString(R.string.linkedin_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

    }

}

 */

