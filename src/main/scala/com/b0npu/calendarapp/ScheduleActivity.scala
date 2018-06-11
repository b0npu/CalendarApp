package com.b0npu.calendarapp

import android.content.{ContentResolver, DialogInterface, Intent}
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v4.content.{CursorLoader, Loader}
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget._

/**
  * 予定表の画面を表示するクラス
  *
  * アプリの画面を生成するonCreateメソッドでCalendarActivityからIntentを受取り
  * CalendarViewで選択された日の予定表を表示する
  * 予定の追加・更新・削除を予定表に即時反映させるためにCursorLoaderを使うので
  * LoaderCallbacksインターフェースを実装する
  */
class ScheduleActivity extends FragmentActivity with TypedFindView with LoaderCallbacks[Cursor] {

  /**
    * フィールドの定義
    *
    * SQLiteデータベースから取得した予定のデータベースIDを格納する配列を定義する
    * 取得した予定の内容をCursorからListViewに渡すために使うCursorAdapterを格納する変数も定義する
    * (自クラスで使うだけのフィールドはprivateにして明示的に非公開にしてます)
    */
  private var scheduleItemIdArray: Array[String] = Array.empty
  private var scheduleContentCursorAdapter: SimpleCursorAdapter = _

  /**
    * アプリの画面を生成
    *
    * アプリを起動するとonCreateが呼ばれてActivityが初期化される
    * 選択された日付をIntentから取得しviewScheduleListメソッドで予定をListViewに表示する
    * addScheduleButtonを押すと予定の編集・登録画面を開く
    */
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_schedule)

    /* Intentから選択された日付を取得する */
    val calendarIntent: Intent = getIntent
    val selectedDate = calendarIntent.getStringExtra("select_date")

    /* レイアウトに設置したscheduleDateTextViewに選択された日付を表示する */
    val selectedDateTextView: TextView = findView(TR.selectedDateTextView)
    selectedDateTextView.setText(selectedDate)

    /* 選択された日付の予定をListViewに表示する */
    viewScheduleList(selectedDate)

    /* addScheduleButtonを押して予定を追加する */
    val addScheduleButton: Button = findView(TR.addScheduleButton)
    addScheduleButton.setOnClickListener(new View.OnClickListener {

      override def onClick(view: View): Unit = {
        /* インテントにEditScheduleActivityクラスと予定表の日付を指定して予定の編集・登録画面を開く */
        val editScheduleIntent = new Intent(ScheduleActivity.this, classOf[EditScheduleActivity])
        editScheduleIntent.putExtra("schedule_date", selectedDate)
        startActivity(editScheduleIntent)
      }
    })
  }

  /**
    * viewScheduleListメソッドの定義
    *
    * SQLiteデータベースからselectedDateの日付の予定をCursorLoaderで取得しListViewに表示する
    * ListViewに表示された予定を選択すると予定の編集画面を開く
    * ListViewに表示された予定を長押しすると削除する
    */
  private def viewScheduleList(selectedDate: String): Unit = {

    /* SQLiteデータベースへの問い合わせに使うので選択された日付をCursorLoaderに渡す */
    val scheduleDateBundle = new Bundle
    scheduleDateBundle.putString("selectedDate", selectedDate)
    getSupportLoaderManager.initLoader(0, scheduleDateBundle, ScheduleActivity.this)

    /* CursorLoaderで取得する予定表の項目と項目の表示先のViewを指定してCursorAdapterを作成する */
    val fromScheduleDBColumn = Array(ScheduleDB.TIME, ScheduleDB.CONTENT)
    val toTextViewInnerListView = Array(R.id.scheduleTimeInnerListView, R.id.scheduleContentInnerListView)
    scheduleContentCursorAdapter = new SimpleCursorAdapter(ScheduleActivity.this, R.layout.listview_schedulelistitem, null, fromScheduleDBColumn, toTextViewInnerListView, 0)

    /* scheduleListViewにCursorAdapterを渡して予定を表示する */
    val scheduleListView = findView(TR.scheduleListView)
    scheduleListView.setAdapter(scheduleContentCursorAdapter)

    /* scheduleListViewに表示した予定表から予定を選択して編集する */
    scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {

        /* 選択された予定のデータベースIDを取得する */
        val selectedScheduleItemId = scheduleItemIdArray(position)
        /* インテントにEditScheduleActivityクラスと予定のデータベースIDを指定して予定を編集する画面を開く */
        val editScheduleIntent = new Intent(ScheduleActivity.this, classOf[EditScheduleActivity])
        editScheduleIntent.putExtra("schedule_id", selectedScheduleItemId)
        startActivity(editScheduleIntent)
      }
    })

    /* scheduleListViewに表示した予定表から長押しした予定を削除する */
    scheduleListView.setOnItemLongClickListener(new OnItemLongClickListener {
      override def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long): Boolean = {

        /* 長押した予定のデータベースIDを取得してSQLステートメントを作成する */
        val selectedScheduleItemId = scheduleItemIdArray(position)
        val scheduleItemSelection = s"${ScheduleDB.ID} = $selectedScheduleItemId"

        /* アラートダイアログを表示して予定を削除するか確認する */
        new AlertDialog.Builder(ScheduleActivity.this)
          .setMessage("この予定を削除しますか？")
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener {
            /* ダイアログのOKボタンが押されたら予定を削除する */
            override def onClick(dialogInterface: DialogInterface, i: Int): Unit = {
              /* 予定をSQLiteデータベースから削除する */
              val scheduleContentResolver: ContentResolver = getContentResolver
              scheduleContentResolver.delete(ScheduleDB.CONTENT_URI, scheduleItemSelection, null)
            }
          })
          .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener {
            /* Cancelボタンが押されたら予定を削除せずにダイアログを閉じる */
            override def onClick(dialogInterface: DialogInterface, i: Int): Unit = {
            }
          })
          .create
          .show
        /* LongClickの完了を通知する */
        true
      }
    })
  }

  /**
    * onCreateLoaderメソッドをオーバーライド
    *
    * このメソッドはCursorLoaderを生成するメソッドで
    * 生成されたCursorLoaderは指定された条件によるデータベースへの問い合わせを
    * 非同期的に実施し検索結果をCursorに格納する
    */
  override def onCreateLoader(id: Int, bundle: Bundle): Loader[Cursor] = {

    /* 選択された日付をBundleから取得してSQLステートメントを作成する */
    val selectedDate = bundle.getString("selectedDate")
    val dateSelection = s"${ScheduleDB.DATE} LIKE ?"
    val dateSelectionArgs = Array(s"$selectedDate%")
    val sortOrderByTime = ScheduleDB.TIME

    /* SQLiteデータベースから選択された日付の予定を予定時間の順にCursorに格納するCursorLoaderを生成する */
    new CursorLoader(ScheduleActivity.this, ScheduleDB.CONTENT_URI, null, dateSelection, dateSelectionArgs, sortOrderByTime)
  }

  /**
    * onLoadFinishedメソッドをオーバーライド
    *
    * このメソッドはCursorLoaderが読み込みを終了する際に呼ばれるメソッドで
    * 以前にCursorAdapterに渡したCursorと新しく検索結果を格納したCursorを取り替える
    * ListViewに表示された予定の編集や削除でデータベースIDを使用するので
    * 新しくCursorに格納された予定表の検索結果から予定のデータベースIDを取得し
    * Cursorに格納された順にデータベースIDを配列に格納する
    */
  override def onLoadFinished(loader: Loader[Cursor], cursor: Cursor): Unit = {

    /* 以前のCursorと新しいCursorを取り替える */
    scheduleContentCursorAdapter.swapCursor(cursor)
    /* Cursorを取り替えたらデータベースIDの配列を作り直す */
    scheduleItemIdArray = Array.empty

    /* Cursorに格納された予定表の検索結果の先頭から順に予定のデータベースIDを取得し配列に格納する */
    if (cursor.moveToFirst) {
      do {
        val scheduleItemId = cursor.getString(cursor.getColumnIndex(ScheduleDB.ID))
        scheduleItemIdArray :+= scheduleItemId
      } while (cursor.moveToNext)
    }
  }

  /**
    * onLoaderResetメソッドをオーバーライド
    *
    * このメソッドはCursorLoaderがリセットされる際に呼ばれるメソッドで
    * 以前にCursorAdapterに渡したCursorを取り除き利用できなくする
    */
  override def onLoaderReset(loader: Loader[Cursor]): Unit = {
    scheduleContentCursorAdapter.swapCursor(null)
  }
}
