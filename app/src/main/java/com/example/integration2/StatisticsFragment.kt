package com.example.integration2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }
}