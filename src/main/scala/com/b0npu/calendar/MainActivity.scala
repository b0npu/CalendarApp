package com.b0npu.calendar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity extends AppCompatActivity with TypedFindView {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }
}
