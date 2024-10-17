import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.integration2.LoginActivity
import com.example.integration2.R
import java.io.File

object ActivityUtils {

    val userDataFileName = "userdata.json"
    val userExpensesFileName = "user_expenses.json"
    val roomMonthlyExpensesFileName = "room_monthly_expenses.json"
    val reportedLogsFileName = "logs.txt"
    val reportedReadmeLogsFileName = "logs.md"
    val directoryName = "RoomBudget"
    val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        directoryName
    )
    val userDataFile = File(directory, userDataFileName)
    val roomMontlyExpensesFile = File(directory, roomMonthlyExpensesFileName)
    val reportedLogsFile = File(directory, reportedLogsFileName)
    val reportedReadmeLogsFile = File(directory, reportedReadmeLogsFileName)
    val userExpensesFile = File(directory, userExpensesFileName)
    private val contextTAG: String = "GetDataFragment"


    val avatars = intArrayOf(
        R.mipmap.avatar1,
        R.mipmap.avatar2,
        R.mipmap.avatar3,
        R.mipmap.avatar4,
        R.mipmap.avatar5,
        R.mipmap.avatar6,
        R.mipmap.avatar7,
        R.mipmap.avatar8,
        R.mipmap.avatar9,
        R.mipmap.avatar10,
        R.mipmap.avatar11,
        R.mipmap.avatar12,
    )

    fun navigateToActivity(activity: Activity, intent: Intent) {
        if (intent.component?.className == LoginActivity::class.java.name) {
            LOGGING.INFO(contextTAG, "Deleting userData.json and user_expenses.json files and navigating to LoginActivity")
            userDataFile.delete()
            userExpensesFile.delete()
            roomMontlyExpensesFile.delete()
        }
        activity.startActivity(intent)
    }

    fun showAboutDialog(activity: Activity) {
        val mView: View = activity.layoutInflater.inflate(R.layout.about, null)
        val tv = mView.findViewById<TextView>(R.id.app_version_id)
        val gmailIMG = mView.findViewById<ImageView>(R.id.gmail_img_id)
        val githubIMG = mView.findViewById<ImageView>(R.id.github_img_id)
        val instagramIMG = mView.findViewById<ImageView>(R.id.instagram_img_id)
        val linkedinIMG = mView.findViewById<ImageView>(R.id.linkedin_img_id)
        tv.text = activity.resources.getString(R.string.version)

        AlertDialog.Builder(activity)
            .setView(mView)
            .setCancelable(true)
            .show()

        gmailIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.email_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        githubIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.github_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        instagramIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.instagram_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        linkedinIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.linkedin_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

    }

    fun relaunch(activity: Activity) {
        activity.finishAffinity()
        activity.startActivity(activity.intent)
    }


}