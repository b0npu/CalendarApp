package com.b0npu.calendarapp

import android.content.{ContentProvider, ContentUris, ContentValues, Context}
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.net.Uri

/**
  * 予定表のデータベースを扱うためのContentProviderのクラス
  *
  * 予定表のSQLiteデータベースに問い合わせ・追加・更新・削除を行う
  * SQLiteデータベースのテーブルはSQLiteOpenHelperクラスを継承した
  * インナークラスのScheduleDBOpenHelperで作成する
  */
class ScheduleContentProvider extends ContentProvider {

  /**
    * フィールドの定義
    *
    * インナークラスのScheduleDBOpenHelperを扱うための変数を定義する
    * (自クラスで使うだけのフィールドはprivateにして明示的に非公開にしてます)
    */
  private var scheduleDBOpenHelper: ScheduleDBOpenHelper = _

  /**
    * onCreateメソッドをオーバーライド
    *
    * このメソッドはContentProviderが読み込まれた際に初期化するメソッドで
    * ContentProviderが正常に読み込まれればTrueを失敗すればFalseを返す
    * インナークラスのScheduleDBOpenHelperにSQLiteデータベースのファイル名を
    * 渡してSQLiteデータベースを作成する
    * 既に同じファイル名のSQLiteデータベースがある場合は開いて使用する
    */
  override def onCreate(): Boolean = {
    /* SQLiteデータベースとしてschedule_table.sqliteファイルを作成する(既にあれば開く)  */
    scheduleDBOpenHelper = new ScheduleDBOpenHelper(getContext, s"${ScheduleDB.TABLE}.sqlite", null, 1)
    true
  }

  /**
    * queryメソッドをオーバーライド
    *
    * このメソッドはSQLiteデータベースへの問い合わせのためのメソッドで
    * データベースを検索した結果をCursorに格納して返す
    * selectionで指定されたschedule_tableのカラムをsortOrderの順にCursorに格納する
    * CursorLoaderでデータベースの変更をアプリに即時反映させるため
    * setNotificationUriでデータベースのURIを監視対象として登録する
    */
  override def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {
    /* データベースを読み出し専用で開いて問い合わせの内容を検索する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getReadableDatabase
    val scheduleContentCursor: Cursor = scheduleSQLiteDB.query(ScheduleDB.TABLE, projection, selection, selectionArgs, null, null, sortOrder)

    /* データベースの変更をCursorLoaderに通知するためURIを登録する */
    scheduleContentCursor.setNotificationUri(getContext.getContentResolver, uri)

    /* 検索結果を格納したCursorを返す */
    scheduleContentCursor
  }

  /**
    * insertメソッドをオーバーライド
    *
    * このメソッドはContentProviderにデータを追加するメソッドで
    * 追加されたデータのURIを返す
    * CursorLoaderでデータベースの変更をアプリに即時反映させるため
    * notifyChangeでデータベースへの追加をCursorLoaderに通知する
    */
  override def insert(uri: Uri, contentValues: ContentValues): Uri = {
    /* データベースを書き込み可能な状態で開いてデータを追加する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val newContentId: Long = scheduleSQLiteDB.insert(ScheduleDB.TABLE, null, contentValues)
    val newContentUri: Uri = ContentUris.withAppendedId(uri, newContentId)

    /* データベースへの追加をCursorLoaderに通知する */
    getContext.getContentResolver.notifyChange(newContentUri, null)

    /* 追加されたデータのURIを返す */
    newContentUri
  }

  /**
    * updateメソッドをオーバーライド
    *
    * このメソッドはContentProviderにデータを更新するメソッドで
    * データが更新された列の数を返す
    * CursorLoaderでデータベースの変更をアプリに即時反映させるため
    * notifyChangeでデータベースへの更新をCursorLoaderに通知する
    */
  override def update(uri: Uri, contentValues: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    /* データベースを書き込み可能な状態で開いてデータを更新する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val updatedRowNum: Int = scheduleSQLiteDB.update(ScheduleDB.TABLE, contentValues, selection, selectionArgs)

    /* データベースの更新をCursorLoaderに通知する */
    getContext.getContentResolver.notifyChange(uri, null)

    /* 更新された列の数を返す */
    updatedRowNum
  }

  /**
    * deleteメソッドをオーバーライド
    *
    * このメソッドはContentProviderからデータを削除するメソッドで
    * 削除された列の数を返す
    * CursorLoaderでデータベースの変更をアプリに即時反映させるため
    * notifyChangeでデータベースからの削除をCursorLoaderに通知する
    */
  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    /* データベースを書き込み可能な状態で開いてデータを削除する */
    val scheduleSQLiteDB: SQLiteDatabase = scheduleDBOpenHelper.getWritableDatabase
    val deletedRowNum: Int = scheduleSQLiteDB.delete(ScheduleDB.TABLE, selection, selectionArgs)

    /* データベースからの削除をCursorLoaderに通知する */
    getContext.getContentResolver.notifyChange(uri, null)

    /* 削除された列の数を返す */
    deletedRowNum
  }

  /**
    * getTypeメソッドをオーバーライド
    *
    * このメソッドはContentProviderからデータのタイプを取得するメソッドで
    * 与えられたURIに格納されているデータのMIMEタイプを返す
    * 予定表のSQLiteデータベースでは使用しないのでnullを返しておく
    */
  override def getType(uri: Uri): String = {
    null
  }

  /**
    * SQLiteデータベースを管理するヘルパークラス
    *
    * 予定表を保存するSQLiteデータベースの作成とバージョンを管理する
    * SQLiteデータベースの作成と操作はContentProviderを経由するので
    * ContentProviderのインナークラスとして定義する
    */
  class ScheduleDBOpenHelper(context: Context, name: String, factory: CursorFactory, version: Int)
    extends SQLiteOpenHelper(context, name, factory, version) {

    /**
      * SQLiteOpenHelperのonCreateメソッドをオーバーライド
      *
      * このメソッドはSQLiteデータベースの作成時に呼ばれるメソッドで
      * データベースにテーブルが存在しない場合にテーブルを作成する
      * 予定の内容・日付・時間を保存する予定表テーブルを作成する
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
      * このメソッドはSQLiteデータベースをアップグレードする際に呼ばれるメソッドで
      * データベースの構造を新しくする必要がある際に使用する
      * とりあえず古いテーブルを削除してテーブルを作り直す
      */
    override def onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
      /* SQLiteデータベースに存在するテーブルを削除して新しくテーブルを作る */
      sqLiteDatabase.execSQL(s"DROP TABLE IF EXISTS ${ScheduleDB.TABLE}")
      onCreate(sqLiteDatabase)
    }
  }

}