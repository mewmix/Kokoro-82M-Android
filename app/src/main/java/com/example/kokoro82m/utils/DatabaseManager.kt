package com.example.kokoro82m.utils

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DatabaseManager {
    fun setProject(context: Context, project: Project) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        val gson = Gson()
        val stylesJson = gson.toJson(project.styles)
        val weightsJson = gson.toJson(project.weights)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_URI, project.uri)
            put(DatabaseHelper.COLUMN_STYLES, stylesJson)
            put(DatabaseHelper.COLUMN_WEIGHTS, weightsJson)
            put(DatabaseHelper.COLUMN_MODE, project.mode.name)
            put(DatabaseHelper.COLUMN_SPEED, project.speed)
            project.bookmark?.let {
                put(DatabaseHelper.COLUMN_BOOKMARK_LINE, it.line)
                put(DatabaseHelper.COLUMN_BOOKMARK_POSITION, it.position)
            }
        }

        db.replace(DatabaseHelper.TABLE_PROJECTS, null, values)
        db.close()
    }

    fun getProject(context: Context, uri: String): Project? {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val gson = Gson()
        val cursor = db.query(
            DatabaseHelper.TABLE_PROJECTS,
            null,
            "${DatabaseHelper.COLUMN_URI} = ?",
            arrayOf(uri),
            null,
            null,
            null
        )

        var project: Project? = null
        if (cursor.moveToFirst()) {
            val stylesJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STYLES))
            val weightsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHTS))
            val stylesType = object : TypeToken<List<String>>() {}.type
            val weightsType = object : TypeToken<Map<String, Float>>() {}.type
            val styles = gson.fromJson<List<String>>(stylesJson, stylesType)
            val weights = gson.fromJson<Map<String, Float>>(weightsJson, weightsType)
            val mode = InterpolationMode.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODE)))
            val speed = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SPEED))
            val bookmarkLine = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKMARK_LINE))
            val bookmarkPosition = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKMARK_POSITION))
            val bookmark = if (bookmarkLine != -1) Bookmark(bookmarkLine, bookmarkPosition) else null

            project = Project(uri, styles, weights, mode, speed, bookmark)
        }

        cursor.close()
        db.close()
        return project
    }

    fun setBookmark(context: Context, uri: String, line: Int, position: Int) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_BOOKMARK_LINE, line)
            put(DatabaseHelper.COLUMN_BOOKMARK_POSITION, position)
        }
        db.update(DatabaseHelper.TABLE_PROJECTS, values, "${DatabaseHelper.COLUMN_URI} = ?", arrayOf(uri))
        db.close()
    }

    fun getBookmark(context: Context, uri: String): Bookmark? {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROJECTS,
            arrayOf(DatabaseHelper.COLUMN_BOOKMARK_LINE, DatabaseHelper.COLUMN_BOOKMARK_POSITION),
            "${DatabaseHelper.COLUMN_URI} = ?",
            arrayOf(uri),
            null,
            null,
            null
        )

        var bookmark: Bookmark? = null
        if (cursor.moveToFirst()) {
            val bookmarkLine = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKMARK_LINE))
            val bookmarkPosition = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKMARK_POSITION))
            if (bookmarkLine != -1) {
                bookmark = Bookmark(bookmarkLine, bookmarkPosition)
            }
        }

        cursor.close()
        db.close()
        return bookmark
    }

    fun clearBookmark(context: Context, uri: String) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_BOOKMARK_LINE, -1)
            put(DatabaseHelper.COLUMN_BOOKMARK_POSITION, -1)
        }
        db.update(DatabaseHelper.TABLE_PROJECTS, values, "${DatabaseHelper.COLUMN_URI} = ?", arrayOf(uri))
        db.close()
    }

    fun setSetting(context: Context, key: String, value: String) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_KEY, key)
            put(DatabaseHelper.COLUMN_VALUE, value)
        }
        db.replace(DatabaseHelper.TABLE_SETTINGS, null, values)
        db.close()
    }

    fun getSetting(context: Context, key: String): String? {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SETTINGS,
            arrayOf(DatabaseHelper.COLUMN_VALUE),
            "${DatabaseHelper.COLUMN_KEY} = ?",
            arrayOf(key),
            null,
            null,
            null
        )

        var value: String? = null
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VALUE))
        }

        cursor.close()
        db.close()
        return value
    }
}
