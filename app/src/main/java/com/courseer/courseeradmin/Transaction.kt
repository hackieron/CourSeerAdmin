package com.courseer.courseeradmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


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

        // Set up Firestore query
        val collectionReference = firestore.collection("transaction_reports")

        // Create a query to get all documents
        collectionReference.get()
            .addOnSuccessListener { documents ->
                val transactionList = mutableListOf<String>()

                for (document in documents) {
                    // Access fields using document.data
                    val name = document.getString("name") ?: ""
                    val strand = document.getString("strand") ?: ""
                    val interests = parseMultipleValues(document.getString("interests"))
                    val careers = parseMultipleValues(document.getString("careers"))
                    val filteredPrograms = parseMultipleValues(document.getString("filteredPrograms"))

                    // Do something with the data (e.g., add to the list)
                    val transactionInfo = "$name\nStrand: $strand\nInterests: $interests\nCareers: $careers\n\nRecommended Programs: $filteredPrograms"
                    transactionList.add(transactionInfo)
                    Log.d("TransactionFragment", "Transaction list size: ${transactionList.size}")

                }
                Log.d("TransactionFragment", "Transaction list size: ${transactionList.size}")

                // Set up RecyclerView adapter with your data
                val adapter = TransactionAdapter(transactionList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e("TransactionFragment", "Error getting documents: ", exception)
            }

        return view
    }

    private fun parseMultipleValues(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }
}


