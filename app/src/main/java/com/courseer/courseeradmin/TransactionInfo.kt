package com.courseer.courseeradmin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TransactionInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_info)

        // Retrieve transactionInfo from the intent
        val transactionInfo = intent.getStringExtra("transactionInfo")


        val textViewTransactionInfo: TextView = findViewById(R.id.textViewTransactionInfo)
        textViewTransactionInfo.text = transactionInfo
    }
}
