package com.courseer.courseeradmin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.courseer.courseeradmin.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
// Firebase Analytics initialization
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)

        val intent = Intent(this, MyBroadcastReceiver::class.java)
// Replace PendingIntent.FLAG_MUTABLE with PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.loginButton.setOnClickListener {
            // Replace these lines with user input or a secure way to obtain credentials
            val username = binding.usernameTextInputLayout.editText?.text.toString()
            val password = binding.passwordTextInputLayout.editText?.text.toString()



            if (validateInput(binding.usernameTextInputLayout, binding.passwordTextInputLayout)) {
                if (dbHelper.getUser(username, password)) {
                    // Login successful, start CombinedActivity
                    val intent = Intent(this, CombinedActivity::class.java)
                    startActivity(intent)
                } else {
                    // Invalid credentials, show a Toast
                    Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun validateInput(usernameLayout: TextInputLayout, passwordLayout: TextInputLayout): Boolean {
        val username = usernameLayout.editText?.text.toString()
        val password = passwordLayout.editText?.text.toString()

        if (username.isEmpty()) {
            usernameLayout.error = "Username is required"
            return false
        } else {
            usernameLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            return false
        } else {
            passwordLayout.error = null
        }

        return true
    }
}





