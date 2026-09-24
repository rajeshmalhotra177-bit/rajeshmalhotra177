package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DetectionLog
import com.example.data.model.TriggerKeyword
import com.example.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HearClear", appName)
    }

    @Test
    fun `default user settings model properties`() {
        val settings = UserSettings()
        assertTrue(settings.autoDuckMedia)
        assertTrue(settings.audioPassthroughEnabled)
        assertTrue(settings.playAlertChime)
        assertTrue(settings.vibrateOnAlert)
        assertEquals(58f, settings.sensitivityDb, 0.01f)
    }

    @Test
    fun `trigger keyword model validation`() {
        val kw = TriggerKeyword(keyword = "Rajesh", isPreset = false)
        assertEquals("Rajesh", kw.keyword)
        assertTrue(kw.isEnabled)
    }

    @Test
    fun `detection log model creation`() {
        val log = DetectionLog(
            triggerType = "VOICE_DETECTED",
            peakDecibels = 68.5f,
            detailText = "Speech detected"
        )
        assertEquals("VOICE_DETECTED", log.triggerType)
        assertEquals(68.5f, log.peakDecibels, 0.01f)
    }
}
