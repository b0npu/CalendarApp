package com.b0npu.calendarapp

import android.R.style
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.{ContentResolver, ContentValues, Intent}
import android.database.Cursor
import android.icu.util.Calendar
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget._

/**
  * 予定表を編集する画面を表示するクラス
  *
  */
class EditScheduleActivity extends AppCompatActivity with TypedFindView {

  /**
    * フィールドの定義
    *
    * 複数のメソッドで扱うwidgetのidを格納する変数を定義する
    * (自クラスで使うだけのフィールドはprivateにして明示的に非公開にしてます)
    */
  private var scheduleDateView: TextView = _
  private var scheduleTimeView: TextView = _
  private var scheduleEditText: EditText = _

  /**
    * アプリの画面を生成
    *
    * アプリを起動するとonCreateが呼ばれてActivityが初期化される
    */
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_editschedule)

    /* レイアウトに設置したwidgetのidを変数に格納する */
    scheduleDateView = findView(TR.scheduleDateView)
    scheduleTimeView = findView(TR.scheduleTimeView)
    scheduleEditText = findView(TR.scheduleEditText)
    val saveButton: Button = findView(TR.saveButton)
    val cancelButton: Button = findView(TR.cancelButton)

    /* Intentから予定表のデータベースIDと選択された日付を取得する */
    val scheduleIntent: Intent = getIntent
    val scheduleItemId = scheduleIntent.getStringExtra("schedule_id")
    val scheduleDate = scheduleIntent.getStringExtra("schedule_date")

    if (scheduleItemId == null) {
      /* scheduleItemIdが無ければ新しい予定なのでscheduleDateViewに選択された日付を表示する */
      scheduleDateView.setText(scheduleDate)
    } else {
      /* scheduleItemIdが有れば予定の編集なのでデータベースに保存された予定を表示する */
      viewScheduleItem(scheduleItemId)
    }

    /* scheduleTimeViewに選択した時間を表示する */
    selectScheduleTime

    /* saveButtonが押されたら予定をデータベースに保存する */
    saveButton.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        saveScheduleToDB(scheduleItemId)
      }
    })

    /* cancelButtonが押されたらEditScheduleActivityを閉じる */
    cancelButton.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        finish
      }
    })
  }

  /**
    * viewScheduleItemメソッドの定義
    *
    */
  private def viewScheduleItem(scheduleItemId: String): Unit = {

    val scheduleContentResolver: ContentResolver = getContentResolver
    val scheduleItemSelection = s"${ScheduleDB.ID} = $scheduleItemId"

    val scheduleContentCursor: Cursor = scheduleContentResolver.query(ScheduleDB.CONTENT_URI, null, scheduleItemSelection, null, null)

    scheduleContentCursor.moveToFirst
    val scheduleDate = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.DATE))
    val scheduleTime = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.TIME))
    val scheduleContent = scheduleContentCursor.getString(scheduleContentCursor.getColumnIndex(ScheduleDB.CONTENT))

    scheduleDateView.setText(scheduleDate)
    scheduleTimeView.setText(scheduleTime)
    scheduleEditText.setText(scheduleContent)
  }

  /**
    * selectScheduleTimeメソッドの定義
    *
    */
  private def selectScheduleTime: Unit = {

    /* 時間を選択してscheduleTimeViewに表示する */
    scheduleTimeView.setOnClickListener(new OnClickListener {

      override def onClick(view: View): Unit = {
        /* Calendarユーティリティから現在時刻を取得してTimePickerに現在時刻を表示する */
        val calendarUtility = Calendar.getInstance
        val nowHour = calendarUtility.get(Calendar.HOUR_OF_DAY)
        val nowMinute = calendarUtility.get(Calendar.MINUTE)
        new TimePickerDialog(EditScheduleActivity.this, style.Theme_Holo_Light_Dialog, new OnTimeSetListener {
          override def onTimeSet(timePicker: TimePicker, selectHour: Int, selectMinute: Int): Unit = {
            /* TimePickerで選択した時間を表示する */
            scheduleTimeView.setText(f"$selectHour%02d : $selectMinute%02d")
          }
        }, nowHour, nowMinute, true).show
      }
    })
  }

  /**
    * saveScheduleToDBメソッドの定義
    *
    * 予定をSQLiteデータベースに保存する
    */
  private def saveScheduleToDB(scheduleItemId: String): Unit = {

    /* 予定編集画面に表示された日付と時間と予定を習得する */
    val scheduleDate = scheduleDateView.getText.toString
    val selectedTime = scheduleTimeView.getText.toString
    val writtenSchedule = scheduleEditText.getText.toString

    /* ContentValuesにSQLiteデータベースに保存する値を格納する */
    val scheduleContentValues = new ContentValues
    scheduleContentValues.put(ScheduleDB.DATE, scheduleDate)
    scheduleContentValues.put(ScheduleDB.TIME, selectedTime)
    scheduleContentValues.put(ScheduleDB.CONTENT, writtenSchedule)

    /* ContentProviderを使いSQLiteデータベースに予定を保存する */
    val scheduleContentResolver: ContentResolver = getContentResolver
    if (writtenSchedule.isEmpty) {
      /* scheduleEditTextに記入された予定が無ければ通知する */
      Toast.makeText(
        EditScheduleActivity.this,
        "予定が入っていません\n予定を入力してから保存して下さい",
        Toast.LENGTH_LONG
      ).show

    } else if (scheduleItemId == null) {
      /* scheduleItemIdが無ければデータベースに新しく予定を追加して予定編集画面を閉じる */
      scheduleContentResolver.insert(ScheduleDB.CONTENT_URI, scheduleContentValues)
      finish

    } else {
      /* scheduleItemIdが有れば編集なのでデータベースを更新して予定編集画面を閉じる */
      val scheduleItemSelection = s"${ScheduleDB.ID} = $scheduleItemId"
      scheduleContentResolver.update(ScheduleDB.CONTENT_URI, scheduleContentValues, scheduleItemSelection, null)
      finish
    }
  }
}