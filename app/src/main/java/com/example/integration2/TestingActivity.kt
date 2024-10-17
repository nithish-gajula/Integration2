package com.example.integration2

import ActivityUtils
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.github.imcloudfloating.markdown.MarkdownIt

class TestingActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var markdownView: MarkdownIt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        markdownView = findViewById(R.id.markdown_view)
        markdownView.markdownString = loadFileFromInternalStorage()


    }

    // Optionally handle back button to close drawer if opened
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFileFromInternalStorage(): String {
        val file = ActivityUtils.reportedReadmeLogsFile
        return file.readText()
    }
}
