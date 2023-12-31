package com.courseer.courseeradmin

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class AdminFragment : Fragment() {
    private lateinit var uploadCsvButton: Button
    private lateinit var addRowButton: Button
    private lateinit var updateRowButton: Button
    private lateinit var deleteRowButton: Button
    private var selectedScholarship: String = ""
    private var storage = Firebase.storage
    private val storageRef = storage.reference
    private lateinit var scholarshipNameEditText: EditText
    private lateinit var shortDescriptionEditText: EditText
    private lateinit var longDescriptionEditText: EditText
    private lateinit var linkEditText: EditText
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var categorySpinner: Spinner
    private lateinit var citySpinner: Spinner
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var rootView: View
    private lateinit var cityAdapter: ArrayAdapter<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        FirebaseAnalytics.getInstance(requireContext()).setAnalyticsCollectionEnabled(true)

        rootView = inflater.inflate(R.layout.fragment_admin, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        scholarshipNameEditText = rootView.findViewById(R.id.scholarshipNameEditText)
        shortDescriptionEditText = rootView.findViewById(R.id.shortDescriptionEditText)
        longDescriptionEditText = rootView.findViewById(R.id.longDescriptionEditText)
        linkEditText = rootView.findViewById(R.id.linkEditText)


        categorySpinner = rootView.findViewById(R.id.categorySpinner)
        citySpinner = rootView.findViewById(R.id.citySpinner)

        uploadCsvButton = rootView.findViewById(R.id.uploadCsvButton)
        addRowButton = rootView.findViewById(R.id.addRowButton)
        updateRowButton = rootView.findViewById(R.id.updateRowButton)
        deleteRowButton = rootView.findViewById(R.id.deleteRowButton)

        categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryAdapter.getItem(position)
                citySpinner.isEnabled = selectedCategory != "National Level"
                if(selectedCategory == "National Level"){
                    citySpinner.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }
        fetchCitiesFromStorage()
        fetchCategoriesFromStorage()

        updateRowButton.setOnClickListener {
            logButtonEvent("update_button_click")
            updateRowInCsv()
        }

        // Fetch CSV content from Firebase Storage
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                // Now you have the CSV content, you can use it as needed
                // For example, you can parse it and populate a dropdown with scholarship names
                val scholarshipNames = parseScholarshipNamesFromCSV(csvContent)
                setupScholarshipDropdown(scholarshipNames)
            },
            onFailure = {
                // Handle failure to fetch CSV content
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT)
                    .show()
            }
        )
        deleteRowButton = rootView.findViewById(R.id.deleteRowButton)

        deleteRowButton.setOnClickListener {
            if (selectedScholarship.isNotEmpty()) {
                // Only allow deleting a row if a scholarship is selected

                deleteRowFromCsv()
            } else {
                // No scholarship is selected, do nothing or show a message
                Toast.makeText(
                    requireContext(),
                    "Please select a scholarship to delete",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up click listener for CSV upload button
        uploadCsvButton.setOnClickListener {
            clearInputFields()
            scholarshipNameEditText.isEnabled = true
        }

        // Set up click listener for Add Row button
        // Set up click listener for Add Row button
        addRowButton.setOnClickListener {
            if (selectedScholarship.isEmpty()) {
                // Only allow adding a row if no scholarship is selected
                addRowToCsv()
            } else {
                // Scholarship is selected, do nothing or show a message
                Toast.makeText(
                    requireContext(),
                    "Cannot add row when a scholarship is selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        return rootView
    }
    private fun logButtonEvent(buttonName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName)
        firebaseAnalytics.logEvent("button_click", bundle)

        // Add a log statement for debugging
        Log.d("FirebaseAnalytics", "Event logged: button_click - $buttonName")
    }

    private fun fetchCategoriesFromStorage() {
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                val categories = parseCategoriesFromCSV(csvContent)
                categoryAdapter.clear()
                categoryAdapter.addAll(categories)
                categoryAdapter.notifyDataSetChanged()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun fetchCitiesFromStorage() {
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                val citiesFromCSV = parseCitiesFromCSV(csvContent)
                val citiesInMetroManila = listOf("Makati", "Quezon City", "Manila", "Taguig", "Pasig", "Mandaluyong", "San Juan", "Pasay", "Parañaque", "Muntinlupa", "Las Piñas", "Valenzuela", "Navotas", "Malabon", "Caloocan")

                // Remove duplicates and add cities in Metro Manila to the adapter
                val uniqueCities = (citiesFromCSV + citiesInMetroManila).distinct()
                cityAdapter.clear()
                cityAdapter.addAll(uniqueCities)
                cityAdapter.notifyDataSetChanged()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun parseCategoriesFromCSV(csvContent: String): List<String> {
        val categories = mutableListOf<String>()
        val rows = csvContent.split("|")
        for (row in rows) {
            val columns = row.split(";")
            if (columns.size >= 5) { // Ensure that the category column exists
                categories.add(columns[4]) // Add the category to the list
            }
        }
        return categories.toSet().toList()
    }

    private fun parseCitiesFromCSV(csvContent: String): List<String> {
        val cities = mutableListOf<String>()
        val rows = csvContent.split("|")
        for (row in rows) {
            val columns = row.split(";")
            if (columns.size >= 5) { // Ensure that the category column exists
                cities.add(columns[5]) // Add the category to the list
            }
        }
        return cities.toSet().toList()
    }

    private fun parseScholarshipNamesFromCSV(csvContent: String): List<String> {
        val scholarshipNames = mutableListOf<String>()
        val rows = csvContent.split("|")
        for (row in rows) {
            val columns = row.split(";")
            if (columns.isNotEmpty()) {
                scholarshipNames.add(columns[0])
            }
        }
        return scholarshipNames
    }
    private fun deleteRowFromCsv() {
        // Fetch CSV content from Firebase Storage
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                // Find and delete the selected scholarship's row
                val updatedContent = StringBuilder()
                val rows = csvContent.split("|")
                for (row in rows) {
                    val columns = row.split(";")
                    if (columns.isNotEmpty() && columns[0] != selectedScholarship) {
                        // Keep the rows that are not the selected scholarship
                        updatedContent.append(row).append("|")
                    }
                }

                // Log the updated CSV content
                val updatedCsvContent = updatedContent.toString()
                Log.d("UPDATED_CSV_CONTENT", updatedCsvContent)

                // Upload the updated CSV file back to Firebase Storage
                uploadUpdatedCsvFile(updatedCsvContent)

                // Clear the selected scholarship after deleting
                selectedScholarship = ""
                scholarshipNameEditText.isEnabled = true
            },
            onFailure = {
                // Handle failure to fetch CSV content
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupScholarshipDropdown(scholarshipNames: List<String>) {
        // Assuming you have a dropdown widget in your layout with the ID scholarshipDropdown
        val scholarshipDropdown = rootView.findViewById<Spinner>(R.id.scholarshipDropdown)

        val adapter = ArrayAdapter(context ?: return, android.R.layout.simple_spinner_item, scholarshipNames)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        scholarshipDropdown.adapter = adapter

        // Set up a listener for item selection in the dropdown
// Inside onCreateView

// Set up a listener for item selection in the dropdown
        scholarshipDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedScholarship = scholarshipNames[position] // Update the selected scholarship
                // Now you can fetch and display the details of the selected scholarship
                displayScholarshipDetails(selectedScholarship)

                // Enable or disable scholarshipNameEditText based on selection
                scholarshipNameEditText.isEnabled = false // Disable when an item is selected
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
                scholarshipNameEditText.isEnabled = true // Enable when nothing is selected
            }
        }
    }


    private fun displayScholarshipDetails(selectedScholarship: String) {
        // Fetch CSV content from Firebase Storage
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                // Retrieve scholarship data by name
                val scholarshipData = getScholarshipDataByName(selectedScholarship, csvContent)

                // Display the data in your UI fields (modify this part based on your UI)
                scholarshipNameEditText.setText(scholarshipData.getOrNull(0) ?: "")
                shortDescriptionEditText.setText(scholarshipData.getOrNull(1) ?: "")
                longDescriptionEditText.setText(scholarshipData.getOrNull(2) ?: "")
                linkEditText.setText(scholarshipData.getOrNull(3) ?: "")
                categorySpinner.setSelection(categoryAdapter.getPosition(scholarshipData.getOrNull(4) ?: ""))
                citySpinner.setSelection(cityAdapter.getPosition(scholarshipData.getOrNull(5) ?: ""))

                // Set the selection of the city spinner based on the city value

            },
            onFailure = {
                // Handle failure to fetch CSV content
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun getScholarshipDataByName(
        scholarshipName: String,
        csvContent: String
    ): List<String> {
        val rows = csvContent.split("|")
        for (row in rows) {
            val columns = row.split(";")
            if (columns.isNotEmpty() && columns[0] == scholarshipName) {
                // Ensure the size of the columns matches the expected number of fields
                return if (columns.size >= 6) {
                    columns.subList(0, 6)
                } else {
                    columns + List(6 - columns.size) { "" }
                }
            }
        }
        return List(6) { "" } // Return a list of empty strings if the scholarship is not found
    }
    // Function to retrieve CSV content from Firebase Storage
    private fun getCSVContentFromStorage(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val folderPath = "csv_files/"
        val fileName = "Scholarships.csv"
        val fileRef = storageRef.child(folderPath + fileName)

        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val csvContent = String(bytes)
            onSuccess(csvContent)
        }.addOnFailureListener {
            onFailure()
        }
    }

    private fun addRowToCsv() {
        // Get scholarship details from input fields
        val scholarshipName = scholarshipNameEditText.text.toString()
        val shortDescription = shortDescriptionEditText.text.toString()
        val longDescription = longDescriptionEditText.text.toString()
        val link = linkEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val city = citySpinner.selectedItem.toString()



        // Check if the scholarship name already exists
        if (isScholarshipNameExists(scholarshipName)) {
            Toast.makeText(requireContext(), "Scholarship name already exists", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a CSV-formatted string for the new row
        val csvRow = "|$scholarshipName;$shortDescription;$longDescription;$link;$category;$city"

        // Add the new row to the local CSV file
        appendToCsvFile(csvRow)

        // Upload the updated CSV to Firebase Storage
        uploadCsvFile()

        // Clear input fields
        clearInputFields()

        Toast.makeText(requireContext(), "Row added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun isScholarshipNameExists(scholarshipName: String): Boolean {
        // Fetch CSV content from Firebase Storage
        var isExists = false
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                // Check if scholarship name exists in the CSV content
                isExists = parseScholarshipNamesFromCSV(csvContent).contains(scholarshipName)
            },
            onFailure = {
                // Handle failure to fetch CSV content
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT).show()
            }
        )
        return isExists
    }

    private fun updateRowInCsv() {
        // Fetch CSV content from Firebase Storage
        getCSVContentFromStorage(
            onSuccess = { csvContent ->
                // Find and update the selected scholarship's row
                val updatedContent = StringBuilder()
                val rows = csvContent.split("|")
                for (row in rows) {
                    val columns = row.split(";")
                    if (columns.isNotEmpty() && columns[0] == selectedScholarship) {
                        // Update the existing row with the new data
                        updatedContent.append("$selectedScholarship").append(";")
                        updatedContent.append(shortDescriptionEditText.text.toString()).append(";")
                        updatedContent.append(longDescriptionEditText.text.toString()).append(";")
                        updatedContent.append(linkEditText.text.toString()).append(";")
                        updatedContent.append(categorySpinner.selectedItem.toString()).append(";")
                        updatedContent.append(citySpinner.selectedItem.toString()).append("|")
                    } else {
                        // Keep the other rows unchanged
                        updatedContent.append(row).append("|")
                    }
                }

                // Log the updated CSV content
                val updatedCsvContent = updatedContent.toString()
                Log.d("UPDATED_CSV_CONTENT", updatedCsvContent)

                // Upload the updated CSV file back to Firebase Storage
                uploadUpdatedCsvFile(updatedCsvContent)

                // Clear the selected scholarship after updating
                selectedScholarship = ""

                // Clear input fields after processing all rows
                clearInputFields()
            },
            onFailure = {
                // Handle failure to fetch CSV content
                Toast.makeText(requireContext(), "Failed to fetch CSV file", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun uploadUpdatedCsvFile(updatedCsvContent: String) {
        val folderPath = "csv_files/"
        val fileName = "Scholarships.csv"
        val fileRef = storageRef.child(folderPath + fileName)

        // Log the updated CSV content before uploading
        Log.d("UPDATED_CSV_CONTENT_BEFORE_UPLOAD", updatedCsvContent)

        // Upload the updated CSV content to Firebase Storage
        fileRef.putBytes(updatedCsvContent.toByteArray()).addOnSuccessListener {
            // File uploaded successfully
            clearInputFields()
            selectedScholarship = ""
            Toast.makeText(requireContext(), "Row updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Handle any errors during upload
            Toast.makeText(requireContext(), "Failed to update CSV file", Toast.LENGTH_SHORT).show()
        }
    }



    private fun uploadCsvFile() {
        val folderPath = "csv_files/"
        val fileName = "Scholarships.csv"
        val fileRef = storageRef.child(folderPath + fileName)

        val localCsvFile = Uri.fromFile(File(requireContext().filesDir, fileName))

        val uploadTask = fileRef.putFile(localCsvFile)

        uploadTask.addOnSuccessListener {
            // File uploaded successfully
            Toast.makeText(requireContext(), "Row added and file updated", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener {
            // Handle any errors during upload
            Toast.makeText(requireContext(), "Failed to update CSV file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun appendToCsvFile(csvRow: String) {
        try {
            val folderPath = "csv_files/"
            val fileName = "Scholarships.csv"
            val fileRef = storageRef.child(folderPath + fileName)

            // Download the existing CSV file from Firebase Storage
            fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                // Append the new row
                val updatedContent = StringBuilder(String(bytes))
                updatedContent.append(csvRow).append("")

                // Upload the updated CSV file back to Firebase Storage
                fileRef.putBytes(updatedContent.toString().toByteArray()).addOnSuccessListener {
                    // File uploaded successfully
                    Toast.makeText(
                        requireContext(),
                        "Row added and file updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    // Handle any errors during upload
                    Toast.makeText(
                        requireContext(),
                        "Failed to update CSV file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                // Handle any errors during download
                Toast.makeText(requireContext(), "Failed to download CSV file", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Failed to add row to CSV",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearInputFields() {
        scholarshipNameEditText.text.clear()
        shortDescriptionEditText.text.clear()
        longDescriptionEditText.text.clear()
        linkEditText.text.clear()
        categorySpinner.setSelection(0)
        citySpinner.setSelection(0)

        // Clear the selected scholarship when clearing input fields
        selectedScholarship = ""
        scholarshipNameEditText.isEnabled = true
    }

    companion object {
        private const val FILE_REQUEST_CODE = 1
        private const val CSV_FILE_NAME = "Scholarships.csv"
    }
}
