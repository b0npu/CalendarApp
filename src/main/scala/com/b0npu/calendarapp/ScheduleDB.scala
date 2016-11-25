package com.b0npu.calendarapp

import android.net.Uri

/**
 * 予定表のデータベース情報を定義するオブジェクト
 *
 */
object ScheduleDB {

    /**
     * フィールドの定義
     *
     */
    val TABLE = "schedule_table"
    val ID = "_id"
    val CONTENT = "schedule_content"
    val DATE = "schedule_date"
    val TIME = "schedule_time"

    val CONTENT_URI: Uri = Uri.parse( "content://com.b0npu.calendarapp.ScheduleContentProvider" )

}