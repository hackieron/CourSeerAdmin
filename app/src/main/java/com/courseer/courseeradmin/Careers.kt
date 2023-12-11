package com.courseer.courseeradmin

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class Careers : Fragment() {

    private var careers: String? = null // Assuming this variable holds the filtered programs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_keywordranking, container, false)

        // Call the function to process Firestore data and update the graph with the legend
        val legendLayout = view.findViewById<LinearLayout>(R.id.legendLayout)
        processFirestoreData(view.findViewById(R.id.graphView), legendLayout)

        return view
    }

    private fun processFirestoreData(graphView: GraphView, legendLayout: LinearLayout) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val collection = db.collection("transaction_reports")

            try {
                val querySnapshot = collection.get().await()

                val programCountMap = mutableMapOf<String, Int>()

                for (document in querySnapshot.documents) {
                    val careersString = document["careers"] as? String

                    careersString?.split(",")?.forEach { program ->
                        val trimmedProgram = program.trim()
                        programCountMap[trimmedProgram] = programCountMap.getOrDefault(trimmedProgram, 0) + 1
                    }
                }

                val sortedProgramCountList = programCountMap.entries.sortedByDescending { it.value }.take(25)

                requireActivity().runOnUiThread {
                    val series = BarGraphSeries<DataPoint>()
                    val colors = mutableListOf<Int>()

                    sortedProgramCountList.forEachIndexed { index, entry ->
                        val dataPoint = DataPoint(index.toDouble(), entry.value.toDouble())
                        series.appendData(dataPoint, true, sortedProgramCountList.size)

                        // Assign color based on program dynamically
                        val color = getColorForProgram(entry.key, requireContext())  // Use the same color logic
                        colors.add(color)
                    }

                    // Enable drawing values on top of each bar
                    series.setDrawValuesOnTop(true)
                    series.setDataWidth(0.9) // Adjust the width as needed
                    series.setSpacing(20) // Adjust the spacing as needed
                    series.valuesOnTopColor = android.R.color.black

                    // Set viewport bounds
                    graphView.viewport.isXAxisBoundsManual = true
                    graphView.viewport.isYAxisBoundsManual = true
                    graphView.viewport.setMinY(0.0)
                    graphView.viewport.setMaxY(sortedProgramCountList.maxByOrNull { it.value }?.value?.toDouble() ?: 0.0)
                    graphView.viewport.setMinX(-0.5)
                    graphView.viewport.setMaxX(sortedProgramCountList.size.toDouble())

                    // Set color for each bar
                    series.setValueDependentColor { dataPoint ->
                        val index = dataPoint.x.toInt()
                        colors.getOrNull(index)?.takeIf { it is Int } as? Int ?: getRandomColor()
                    }

                    // Add series to the graph
                    graphView.addSeries(series)

                    // Add legend



                    // Update the legend layout
                    updateLegendLayout(legendLayout, sortedProgramCountList, colors)
                }

            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    private fun updateLegendLayout(
        legendLayout: LinearLayout,
        sortedProgramCountList: List<Map.Entry<String, Int>>,
        colors: List<Int>
    ) {
        // Clear previous views from the legend layout
        legendLayout.removeAllViews()

        // Sort the legend entries by occurrences
        val sortedLegendList = sortedProgramCountList.sortedByDescending { it.value }

        // Add TextView elements for each program in the legend layout
        sortedLegendList.forEachIndexed { index, entry ->
            val legendTextView = TextView(requireContext())
            legendTextView.text = entry.key
            legendTextView.setBackgroundColor(colors[index])
            legendTextView.setTextColor(Color.WHITE) // You can adjust text color as needed

            // You can customize the layout parameters as needed
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = 16 // Add margin between legend items

            legendLayout.addView(legendTextView, layoutParams)
        }
    }

    private fun getColorForProgram(program: String, context: Context): Int {
        // Sample color assignments for specific program names
        val programColorMap = mutableMapOf<String, Int>()

        // Assuming careers is a comma-separated string of program names
        val distinctPrograms = careers?.split(",")?.map { it.trim() }?.distinct()

        // Assign colors dynamically based on distinct program names
        distinctPrograms?.forEachIndexed { index, distinctProgram ->
            // You can assign colors based on a predefined logic or use a getRandomColor function
            val color = when (index % 3) {
                0 -> ContextCompat.getColor(context, android.R.color.holo_blue_light)
                1 -> ContextCompat.getColor(context, android.R.color.holo_green_light)
                else -> ContextCompat.getColor(context, android.R.color.holo_red_light)
            }

            programColorMap[distinctProgram] = color
        }

        // If the program name is in the map, return the corresponding color; otherwise, return a default color
        return programColorMap[program] ?: getRandomColor()
    }

    private fun getRandomColor(): Int {
        // Generate a random color as a fallback
        return Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    }
}
