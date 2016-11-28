package com.b0npu.calendarapp

import android.content.{ContentProvider, ContentUris, ContentValues, Context}
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.net.Uri

/**
  * ContentProviderのクラス
  */
class ScheduleContentProvider extends ContentProvider {

  /**
    * フィールドの定義
    *
    * (自クラスで使うだけのフィールドはprivateにして明示的に非公開にしてます)
    */
  private var scheduleDBOpenHelper: ScheduleDBOpenHelper = _

  /**
    * onCreateメソッドをオーバーライド
    *
    * このメソッドはContentProviderが読み込まれた際に初期化するメソッドで
    * ContentProviderが正常に読み込まれればTrueを失敗すればFalseを返す
    */
  override def onCreate(): Boolean = {
    /* SQLiteOpenHelperを  */
    scheduleDBOpenHelper = new ScheduleDBOpenHelper(getContext, ScheduleDB.TABLE, null, 1)
    true
  }

  /**
    * queryメソッドをオーバーライド
    *
    * このメソッドはSQLiteデータベースへの問い合わせのためのメソッドで
    * データベースを検索した結果をCursorに格納して返す
    */
  override def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {
    /* データベースを読み出し専用で開いて問い合わせの内容を検索する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getReadableDatabase
    val scheduleContentCursor: Cursor = scheduleSQLiteDB.query(ScheduleDB.TABLE, projection, selection, selectionArgs, null, null, sortOrder)

    /* データベースの変更を通知するためCursor */
    scheduleContentCursor.setNotificationUri(getContext.getContentResolver, uri)

    /* 検索結果を格納したCursorを返す */
    scheduleContentCursor
  }

  /**
    * insertメソッドをオーバーライド
    *
    * このメソッドはContentProviderにデータを追加するメソッドで
    * 追加されたデータのURIを返す
    */
  override def insert(uri: Uri, contentValues: ContentValues): Uri = {
    /* データベースを書き込み可能な状態で開いてデータを追加する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val newContentId: Long = scheduleSQLiteDB.insert(ScheduleDB.TABLE, null, contentValues)
    //    val newContentUri: Uri = Uri.parse(s"$uri/$newContentId")
    val newContentUri: Uri = ContentUris.withAppendedId(uri, newContentId)

    /* データベースの変更を通知するため */
    getContext.getContentResolver.notifyChange(newContentUri, null)

    /* 追加されたデータのURIを返す */
    newContentUri
  }

  /**
    * updateメソッドをオーバーライド
    *
    */
  override def update(uri: Uri, contentValues: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    /* データベースを書き込み可能な状態で開いてデータを更新する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val updatedRowNum: Int = scheduleSQLiteDB.update(ScheduleDB.TABLE, contentValues, selection, selectionArgs)

    /* データベースの変更を通知するため */
    getContext.getContentResolver.notifyChange(uri, null)

    /* 更新された列の数を返す */
    updatedRowNum
  }

  /**
    * deleteメソッドをオーバーライド
    *
    */
  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    /* データベースを書き込み可能な状態で開いてデータを削除する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val deletedRowNum: Int = scheduleSQLiteDB.delete(ScheduleDB.TABLE, selection, selectionArgs)

    /* データベースの変更を通知するため */
    getContext.getContentResolver.notifyChange(uri, null)

    /* 削除された列の数を返す */
    deletedRowNum
  }

  /**
    * getTypeメソッドをオーバーライド
    *
    */
  override def getType(uri: Uri): String = {
    null
  }

  /**
    * SQLiteデータベースを管理するヘルパークラス
    *
    */
  class ScheduleDBOpenHelper(context: Context, name: String, factory: CursorFactory, version: Int)
    extends SQLiteOpenHelper(context, name, factory, version) {

    /**
      * SQLiteOpenHelperのonCreateメソッドをオーバーライド
      *
      * このメソッドはSQLiteデータベースの作成時に呼ばれるメソッドで
      * データベースにテーブルが存在しない場合にテーブルを作成する
      */
    override def onCreate(sqLiteDatabase: SQLiteDatabase): Unit = {
      /* SQLiteデータベースにテーブルを作成するSQLステートメントを実行する */
      sqLiteDatabase.execSQL(
        s""" CREATE TABLE ${ScheduleDB.TABLE} (
               ${ScheduleDB.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
               ${ScheduleDB.CONTENT} TEXT,
               ${ScheduleDB.DATE} TEXT,
               ${ScheduleDB.TIME} TEXT);
         """
      )
    }

    /**
      * SQLiteOpenHelperのonUpgradeメソッドをオーバーライド
      *
      */
    override def onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
      /* SQLiteデータベースに存在するテーブルを削除して新しくテーブルを作る */
      sqLiteDatabase.execSQL(s"DROP TABLE IF EXISTS ${ScheduleDB.TABLE}")
      onCreate(sqLiteDatabase)
    }
  }

}