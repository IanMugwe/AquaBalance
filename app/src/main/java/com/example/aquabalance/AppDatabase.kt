package com.example.AquaBalance.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_STORAGE_FACILITY_TABLE)
        db.execSQL(CREATE_PUMP_STATION_TABLE)
        db.execSQL(CREATE_FLOW_RATE_TABLE)
        db.execSQL(CREATE_CONSUMER_DATA_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STORAGE_FACILITY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PUMP_STATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLOW_RATE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONSUMER_DATA")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "water_distribution.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_STORAGE_FACILITY = "storage_facilities"
        private const val TABLE_PUMP_STATION = "pump_stations"
        private const val TABLE_FLOW_RATE = "flow_rates"
        private const val TABLE_CONSUMER_DATA = "consumer_data"

        // Create table statements
        private const val CREATE_STORAGE_FACILITY_TABLE = "CREATE TABLE $TABLE_STORAGE_FACILITY (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "capacity TEXT)"

        private const val CREATE_PUMP_STATION_TABLE = "CREATE TABLE $TABLE_PUMP_STATION (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "status TEXT)"

        private const val CREATE_FLOW_RATE_TABLE = "CREATE TABLE $TABLE_FLOW_RATE (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "location TEXT," +
                "rate REAL)"


        private const val CREATE_CONSUMER_DATA_TABLE = "CREATE TABLE $TABLE_CONSUMER_DATA (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "consumerId TEXT," +
                "usage REAL)"
    }
}
