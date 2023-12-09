package com.courseer.courseeradmin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserCredentials.db"
        private const val TABLE_NAME = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Add admin credentials
        private const val ADMIN_USERNAME = "admin"
        private const val ADMIN_PASSWORD = "admin123"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT)")
        db.execSQL(CREATE_TABLE)

        // Insert admin user when the database is created
        insertAdminUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertAdminUser(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(COLUMN_USERNAME, ADMIN_USERNAME)
        values.put(COLUMN_PASSWORD, ADMIN_PASSWORD)
        db.insert(TABLE_NAME, null, values)
    }
    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val result = cursor.count > 0
        cursor.close()
        return result
    }
}

