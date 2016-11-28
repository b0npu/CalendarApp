package com.b0npu.calendarapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener

/**
  * アプリ起動時の画面を表示するクラス
  *
  * CalendarViewを表示する
  */
class CalendarActivity extends AppCompatActivity with TypedFindView {

  /**
    * アプリの画面を生成
    *
    * アプリを起動するとonCreateが呼ばれてActivityが初期化される
    * レイアウトに配置したCalendarViewを表示し選択した日の予定表を表示する
    */
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    /* CalendarViewから選択した日の予定表を開く */
    val calendarView: CalendarView = findView(TR.calendarView)
    calendarView.setOnDateChangeListener(new OnDateChangeListener {

      override def onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int): Unit = {
        /* 選択された日付をyy/mm/ddの形に整える */
        val selectedDate: String = s"$year/${month + 1}/$dayOfMonth"

        /* インテントにScheduleActivityクラスと選択された日を指定して予定表の画面を開く */
        val scheduleIntent = new Intent(CalendarActivity.this, classOf[ScheduleActivity])
        scheduleIntent.putExtra("select_date", selectedDate)
        startActivity(scheduleIntent)
      }
    })
  }
}