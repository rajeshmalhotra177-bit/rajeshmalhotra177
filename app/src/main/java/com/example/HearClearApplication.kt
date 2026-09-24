package com.example

import android.app.Application
import com.example.audio.AlertChimeHelper
import com.example.audio.AudioFocusHelper
import com.example.audio.AudioPassthroughEngine
import com.example.audio.BluetoothDeviceHelper
import com.example.audio.HapticAlertHelper
import com.example.audio.SpeechTriggerDetector
import com.example.data.AppDatabase
import com.example.data.HearClearRepository
import com.example.data.model.DetectionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HearClearApplication : Application() {

    lateinit var database: AppDatabase private set
    lateinit var repository: HearClearRepository private set

    lateinit var audioFocusHelper: AudioFocusHelper private set
    lateinit var alertChimeHelper: AlertChimeHelper private set
    lateinit var hapticAlertHelper: HapticAlertHelper private set
    lateinit var bluetoothHelper: BluetoothDeviceHelper private set
    lateinit var audioEngine: AudioPassthroughEngine private set
    lateinit var speechDetector: SpeechTriggerDetector private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        repository = HearClearRepository(
            detectionLogDao = database.detectionLogDao(),
            triggerKeywordDao = database.triggerKeywordDao(),
            userSettingsDao = database.userSettingsDao()
        )

        audioFocusHelper = AudioFocusHelper(this)
        alertChimeHelper = AlertChimeHelper()
        hapticAlertHelper = HapticAlertHelper(this)
        bluetoothHelper = BluetoothDeviceHelper(this)

        audioEngine = AudioPassthroughEngine(
            context = this,
            audioFocusHelper = audioFocusHelper,
            alertChimeHelper = alertChimeHelper,
            hapticAlertHelper = hapticAlertHelper
        )

        speechDetector = SpeechTriggerDetector(this) { matchedKeyword, fullText ->
            // When custom name or keyword was recognized
            audioEngine.simulateCallOut(64f, matchedKeyword)
            CoroutineScope(Dispatchers.IO).launch {
                repository.addLog(
                    DetectionLog(
                        triggerType = "NAME_CALLED",
                        peakDecibels = 65f,
                        detailText = "Name/Keyword '$matchedKeyword' heard: \"$fullText\""
                    )
                )
            }
        }
    }
}
