package com.b0npu.calendarapp

import android.net.Uri

/**
  * 予定表のデータベース情報を定義するオブジェクト
  *
  * 予定表のSQLiteデータベースでテーブル名やカラム名といった
  * アプリ内でグローバルに扱いたい一意の値を定義する
  */
object ScheduleDB {

  /* SQLiteデータベースのテーブル名とカラム名  */
  val TABLE = "schedule_table"
  val ID = "_id"
  val CONTENT = "schedule_content"
  val DATE = "schedule_date"
  val TIME = "schedule_time"

  /* AndroidManifestに定義したContentProviderのURI */
  val CONTENT_URI: Uri = Uri.parse("content://com.b0npu.calendarapp.ScheduleContentProvider")
}