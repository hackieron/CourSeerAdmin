package com.courseer.courseeradmin

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactionList: List<String>) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemTransaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("TransactionAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transactionInfo = transactionList[position]
        holder.textView.text = "${transactionInfo.split("\n")[0]}\n${transactionInfo.split("\n")[1]}"
        // Display only the name

        holder.itemView.setOnClickListener {
            // Handle item click, e.g., start a new activity
            val context = holder.itemView.context
            val intent = Intent(context, TransactionInfo::class.java)
            intent.putExtra("transactionInfo", transactionInfo)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
