package com.courseer.courseeradmin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
class Transaction : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        // Assuming you have a RecyclerView in your layout with the id "transactionRecyclerView"
        val recyclerView = view.findViewById<RecyclerView>(R.id.transactionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Assuming you have a SwipeRefreshLayout in your layout with the id "swipeRefreshLayout"
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            // Handle the refresh action here
            fetchData(recyclerView, swipeRefreshLayout)
        }

        fetchData(recyclerView, swipeRefreshLayout) // Initial data load

        return view
    }

    // Inside your Transaction class
    private fun fetchData(recyclerView: RecyclerView, swipeRefreshLayout: SwipeRefreshLayout) {
        // Set up Firestore query
        val collectionReference = firestore.collection("transaction_reports")

        // Create a query to get all documents, ordered by the "timestamp" field in descending order
        collectionReference.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val transactionList = mutableListOf<String>()

                for (document in documents) {
                    // Access fields using document.data
                    val name = document.getString("name") ?: ""
                    val strand = document.getString("strand") ?: ""

                    val interests = parseMultipleValues(document.getString("interests"))
                    val careers = parseMultipleValues(document.getString("careers"))
                    val filteredPrograms = parseMultipleValues(document.getString("filteredPrograms"))
                    val timestamp = document.getTimestamp("timestamp")
                    val formattedTimestamp = timestamp?.let { formatTimestamp(it) } ?: ""
                    // Do something with the data (e.g., add to the list)
                    val transactionInfo = "$name\nStrand: $strand\nInterests: $interests\nCareers: $careers\n\nRecommended Programs: $filteredPrograms\n\n\nDate Created: $formattedTimestamp"
                    transactionList.add(transactionInfo)
                    Log.d("TransactionFragment", "Transaction list size: ${transactionList.size}")

                }

                // Set up RecyclerView adapter with your data
                val adapter = TransactionAdapter(transactionList)
                recyclerView.adapter = adapter

                // Stop the refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e("TransactionFragment", "Error getting documents: ", exception)

                // Stop the refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
    }
    private fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
    private fun parseMultipleValues(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }
}
