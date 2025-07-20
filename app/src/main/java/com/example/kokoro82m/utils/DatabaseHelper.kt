package com.example.kokoro82m.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kokoro.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_PROJECTS = "projects"
        const val COLUMN_ID = "_id"
        const val COLUMN_URI = "uri"
        const val COLUMN_STYLES = "styles"
        const val COLUMN_WEIGHTS = "weights"
        const val COLUMN_MODE = "mode"
        const val COLUMN_SPEED = "speed"
        const val COLUMN_BOOKMARK_LINE = "bookmark_line"
        const val COLUMN_BOOKMARK_POSITION = "bookmark_position"

        const val TABLE_SETTINGS = "settings"
        const val COLUMN_KEY = "key"
        const val COLUMN_VALUE = "value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProjectsTable = "CREATE TABLE $TABLE_PROJECTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_URI TEXT UNIQUE," +
                "$COLUMN_STYLES TEXT," +
                "$COLUMN_WEIGHTS TEXT," +
                "$COLUMN_MODE TEXT," +
                "$COLUMN_SPEED REAL," +
                "$COLUMN_BOOKMARK_LINE INTEGER," +
                "$COLUMN_BOOKMARK_POSITION INTEGER)"
        db.execSQL(createProjectsTable)

        val createSettingsTable = "CREATE TABLE $TABLE_SETTINGS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_KEY TEXT UNIQUE," +
                "$COLUMN_VALUE TEXT)"
        db.execSQL(createSettingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }
}