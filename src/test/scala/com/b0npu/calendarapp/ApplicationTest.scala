package com.b0npu.calendarapp

import android.os.Build.VERSION_CODES.LOLLIPOP
import com.b0npu.calendarapp.R
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.scalatest.{ FlatSpec, Matchers, RobolectricSuite }

@Config( sdk = Array( LOLLIPOP ) )
class ApplicationTest extends FlatSpec with Matchers with RobolectricSuite {
  "Resources" should "be accessible via R" in {
    RuntimeEnvironment.application.getString( R.string.app_name ) shouldBe "Hello World!"
  }
}
