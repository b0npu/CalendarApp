package com.b0npu.calendarapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener

/**
  * アプリ起動時の画面を表示するクラス
  *
  * CalendarViewを表示しカレンダーの中から選択した日の予定表に移動する
  */
class CalendarActivity extends AppCompatActivity with TypedFindView {

  /**
    * アプリの画面を生成
    *
    * アプリを起動するとonCreateが呼ばれてActivityが初期化される
    * レイアウトに配置したCalendarViewを表示し
    */
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    /* レイアウトに設置したcalendarViewのidを取得し選択された日付を把握する */
    val calendarView: CalendarView = findView(TR.calendarView)
    calendarView.setOnDateChangeListener(new OnDateChangeListener {

      override def onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int): Unit = {
        /* 選択された日付をyy/mm/ddの形に整える */
        val selectScheduleDate: String = s"$year/${month + 1}/$dayOfMonth"

        /* インテントにScheduleActivityクラスと選択された日付を指定して予定表の画面を開く */
        val calendarIntent = new Intent(CalendarActivity.this, classOf[ScheduleActivity])
        calendarIntent.putExtra("select_date", selectScheduleDate)
        startActivity(calendarIntent)
      }
    })
  }
}