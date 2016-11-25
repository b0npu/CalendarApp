package com.b0npu.calendarapp

import android.content.{ContentResolver, DialogInterface, Intent}
import android.database.Cursor
import android.os.Bundle
import android.support.v7.app.{AlertDialog, AppCompatActivity}
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget._

/**
  * 予定表の画面を表示するクラス
  *
  * アプリの画面を生成するonCreateメソッドでCalendarActivityからIntentを受取り
  * CalendarViewで選択された日付の予定を表示する
  */
class ScheduleActivity extends AppCompatActivity with TypedFindView {

  /**
    * アプリの画面を生成
    *
    * アプリを起動するとonCreateが呼ばれてActivityが初期化される
    * 選択された日付のIntentから取得しviewScheduleListメソッドで
    * 予定をListViewに表示する
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

    /* 選択された日付のSQLiteデータベースに保存された予定をListViewに表示する */
    viewScheduleList(selectedDate)

    /* addScheduleButtonを押して予定表を追加する */
    val addScheduleButton: Button = findView(TR.addScheduleButton)
    addScheduleButton.setOnClickListener(new View.OnClickListener {

      override def onClick(view: View): Unit = {
        /* インテントにEditScheduleActivityクラスと予定表の日付を指定して予定を編集する画面を開く */
        val scheduleIntent = new Intent(ScheduleActivity.this, classOf[EditScheduleActivity])
        scheduleIntent.putExtra("schedule_date", selectedDate)
        startActivity(scheduleIntent)
      }
    })

  }

  /**
    * getScheduleListメソッドの定義
    *
    * SQLiteデータベースからselectedDateの日付の予定を
    * getScheduleListメソッドで取得しListViewに表示する
    * ListViewに表示された予定を選択すると予定の編集画面を開く
    * ListViewに表示された予定を長押しすると削除する
    */
  private def viewScheduleList(selectedDate: String): Unit = {
    /* SQLiteデータベースに保存された予定のデータベースIDと内容を取得する */
    val (scheduleItemIdArray, scheduleListArray) = getScheduleList(selectedDate)

    val scheduleListView = findView(TR.scheduleListView)
    val listViewAdapter = new ArrayAdapter[String](
      ScheduleActivity.this,
      android.R.layout.simple_list_item_1,
      scheduleListArray
    )
    scheduleListView.setAdapter(listViewAdapter)

    /* scheduleListViewを押して予定表を編集する */
    scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener {

      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {

        val selectedScheduleItemId = scheduleItemIdArray(id.toInt)
        /* インテントにEditScheduleActivityクラスと予定のデータベースIDを指定して予定を編集する画面を開く */
        val scheduleIntent = new Intent(ScheduleActivity.this, classOf[EditScheduleActivity])
        scheduleIntent.putExtra("schedule_id", selectedScheduleItemId)
        startActivity(scheduleIntent)
      }
    })

    /* scheduleListViewを長押して予定を削除する */
    scheduleListView.setOnItemLongClickListener(new OnItemLongClickListener {
      override def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long): Boolean = {

        val selectedScheduleItemId = scheduleItemIdArray(id.toInt)
        val scheduleItemSelection = s"${ScheduleDB.ID} = $selectedScheduleItemId"

        /* アラートダイアログを表示して予定を削除するか確認する */
        new AlertDialog.Builder(ScheduleActivity.this)
          .setMessage("この予定を削除しますか？")
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener {
            /* ダイアログのOKボタンを押すと予定を削除する */
            override def onClick(dialogInterface: DialogInterface, i: Int): Unit = {
              /* 予定をデータベースから削除する */
              val scheduleContentResolver: ContentResolver = getContentResolver
              scheduleContentResolver.delete(ScheduleDB.CONTENT_URI, scheduleItemSelection, null)
              /* TODO: 削除後のリストが自動で更新されないのでとりあえず画面を閉じとく */
              finish
            }
          })
          .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener {
            /* Cancelボタンを押したら何もしない */
            override def onClick(dialogInterface: DialogInterface, i: Int): Unit = {
            }
          })
          .create
          .show

        true
      }
    })
  }

  /**
    * getScheduleListメソッドの定義
    *
    * SQLiteデータベースからselectedDateの日付の予定を取得して
    * データベースIDの配列と内容の配列に格納し返す
    */
  private def getScheduleList(selectedDate: String): (Array[String], Array[String]) = {

    var scheduleItemIdArray: Array[String] = Array.empty
    var scheduleListArray: Array[String] = Array.empty

    val scheduleContentResolver: ContentResolver = getContentResolver
    val dateSelection = s"${ScheduleDB.DATE} LIKE ?"
    val dateSelectionArgs = Array(s"$selectedDate%")
    val sortOrderByTime = ScheduleDB.TIME

    val scheduleContentCursor: Cursor = scheduleContentResolver.query(ScheduleDB.CONTENT_URI, null, dateSelection, dateSelectionArgs, sortOrderByTime)
    if (scheduleContentCursor.moveToFirst) {
      do {
        val scheduleItemId = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.ID))
        scheduleItemIdArray :+= scheduleItemId

        val scheduleTime = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.TIME))
        val scheduleContent = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.CONTENT))

        val scheduleItem = if (scheduleTime.isEmpty) s"$scheduleContent" else s"$scheduleTime\n$scheduleContent"
        scheduleListArray :+= scheduleItem

      } while (scheduleContentCursor.moveToNext)
    }
    scheduleContentCursor.close

    (scheduleItemIdArray, scheduleListArray)
  }

}