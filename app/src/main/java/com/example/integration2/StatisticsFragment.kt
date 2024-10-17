package com.example.integration2

import ActivityUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class StatisticsFragment : Fragment() {

    private lateinit var aaChartView: AAChartView
    private val contextTAG: String = "StatisticsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)


        // Inflate the layout for this fragment
        /*************************************
         * Implement following Charts, Also try Material Dat Visualization
         * Pie Chart, Bar Chart, Radar Chart
         * -----------Specific -------------
         * Specific Person - Specific Month
         * Specific Person - Overall
         * -----------Overall --------------
         * Everyone - Specific Month
         * Everyone - Overall
         **************************************/

        aaChartView = view.findViewById(R.id.aaChartView)

        setupChart()


        return view
    }

    private fun setupChart1() {
        // Create and configure the AAChartModel
        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column) // Specify the chart type as column (bar chart)
            .title("Monthly Expenses Comparison")
            .subtitle("Comparing Expenses of Two People (June to December 2024)")
            .categories(
                arrayOf(
                    "Jun 2024", "Jul 2024", "Aug 2024",
                    "Sep 2024", "Oct 2024", "Nov 2024", "Dec 2024" // More months added
                )
            )
            .dataLabelsEnabled(true)
            .tooltipEnabled(true) // Enable tooltips
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Person A") // Name of the first person
                        .data(arrayOf(200, 300, 400, 250, 320, 450, 500)), // Expenses for Person A
                    AASeriesElement()
                        .name("Person B") // Name of the second person
                        .data(arrayOf(600, 500, 200, 400, 350, 600, 700))  // Expenses for Person B
                )
            )

        // Assign the chart model to the AAChartView
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun readJsonFromFile(file: File): JSONObject? {
        return try {
            val inputStream = FileInputStream(file)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            JSONObject(jsonString) // Parse the JSON string into a JSONObject
        } catch (e: Exception) {
            e.printStackTrace()
            null // Handle any errors while reading or parsing
        }
    }

    private fun setupChart() {
        // Reading the JSON data from the file (assuming file is already created)
        val jsonObject = readJsonFromFile(ActivityUtils.roomMontlyExpensesFile)

        Log.d(contextTAG, jsonObject.toString())

        // Extract expenses for Person A and Person B from the JSON
        val categories = mutableListOf<String>()
        val personAExpenses = mutableListOf<Int>()
        val personBExpenses = mutableListOf<Int>()

        jsonObject?.let {
            // Iterate through the months (keys) in the JSON object
            val keys = it.keys()
            while (keys.hasNext()) {
                val month = keys.next()
                categories.add(month) // Add month to the categories list

                val monthData = it.getJSONArray(month)

                // Extract Person A and Person B's expenses for that month
                val personAData = monthData.getJSONArray(0)
                val personBData = monthData.getJSONArray(1)

                Log.d(contextTAG, "personAData : $personAData")
                Log.d(contextTAG, "personBData : $personBData")

                // Sum the expenses for each person in the current month for Person A
                var personASum = 0
                for (k in 0 until personAData.length()) {
                    personASum += personAData.getInt(k) // Get each expense and add to sum
                }
                personAExpenses.add(personASum)

                // Sum the expenses for Person B
                var personBSum = 0
                for (k in 0 until personBData.length()) {
                    personBSum += personBData.getInt(k) // Get each expense and add to sum
                }
                personBExpenses.add(personBSum)


            }
        }

        // Create and configure the AAChartModel with dynamic data
        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column) // Specify the chart type as column (bar chart)
            .title("Monthly Expenses Comparison")
            .subtitle("Comparing Expenses of Two People")
            .categories(categories.toTypedArray()) // Use the months from the JSON
            .dataLabelsEnabled(true)
            .tooltipEnabled(true) // Enable tooltips
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Person A") // Name of the first person
                        .data(personAExpenses.toTypedArray()), // Expenses for Person A
                    AASeriesElement()
                        .name("Person B") // Name of the second person
                        .data(personBExpenses.toTypedArray())  // Expenses for Person B
                )
            )

        // Assign the chart model to the AAChartView
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }


}