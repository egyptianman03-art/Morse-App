package com.ahmed.morseapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmed.morseapp.converter.MorseConverter
import com.ahmed.morseapp.sound.MorseSoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MorseUiState(
    val inputText: String = "",
    val outputText: String = "",
    val lastMorseResult: String = "",      // آخر ناتج مورس (للصوت)
    val selectedLanguage: MorseConverter.Language = MorseConverter.Language.AUTO,
    val isPlaying: Boolean = false,
    val soundSpeed: Float = 15f,
    val soundVolume: Float = 0.8f,
    val vibrateEnabled: Boolean = false,
    val statusMessage: String = "",
    val isError: Boolean = false,
    val conversionMode: ConversionMode = ConversionMode.TEXT_TO_MORSE
)

enum class ConversionMode {
    TEXT_TO_MORSE,
    MORSE_TO_TEXT
}

class MorseViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MorseUiState())
    val uiState: StateFlow<MorseUiState> = _uiState.asStateFlow()

    private val soundPlayer = MorseSoundPlayer(application)

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, statusMessage = "", isError = false) }
    }

    fun onLanguageChanged(language: MorseConverter.Language) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun convertTextToMorse() {
        val state = _uiState.value
        val input = state.inputText.trim()
        if (input.isEmpty()) {
            setError("الرجاء إدخال نص أولاً")
            return
        }

        viewModelScope.launch {
            try {
                val lang = if (state.selectedLanguage == MorseConverter.Language.AUTO)
                    MorseConverter.detectLanguage(input) else state.selectedLanguage
                val result = MorseConverter.textToMorse(input, lang)
                _uiState.update {
                    it.copy(
                        outputText = result,
                        lastMorseResult = result,
                        conversionMode = ConversionMode.TEXT_TO_MORSE,
                        statusMessage = "✅ تم التحويل إلى مورس بنجاح",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                setError("❌ خطأ: ${e.message}")
            }
        }
    }

    fun convertMorseToText() {
        val state = _uiState.value
        val input = state.inputText.trim()
        if (input.isEmpty()) {
            setError("الرجاء إدخال شفرة مورس أولاً")
            return
        }
        if (!MorseConverter.isValidMorse(input)) {
            setError("⚠️ الشفرة تحتوي على رموز غير صالحة (استخدم . - / و مسافات فقط)")
            return
        }

        viewModelScope.launch {
            try {
                val preferArabic = state.selectedLanguage == MorseConverter.Language.ARABIC
                val result = MorseConverter.morseToText(input, preferArabic)
                _uiState.update {
                    it.copy(
                        outputText = result,
                        lastMorseResult = input,   // المدخل هو المورس للتشغيل
                        conversionMode = ConversionMode.MORSE_TO_TEXT,
                        statusMessage = "✅ تم فك التشفير بنجاح",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                setError("❌ خطأ: ${e.message}")
            }
        }
    }

    fun clearAll() {
        stopSound()
        _uiState.update {
            MorseUiState(
                selectedLanguage = it.selectedLanguage,
                soundSpeed = it.soundSpeed,
                soundVolume = it.soundVolume,
                vibrateEnabled = it.vibrateEnabled
            )
        }
    }

    fun playMorseSound() {
        val state = _uiState.value

        if (state.isPlaying) {
            stopSound()
            return
        }

        val morse = state.lastMorseResult
        if (morse.isEmpty()) {
            setError("قم بالتحويل أولاً لتشغيل الصوت")
            return
        }

        soundPlayer.playMorse(
            morseCode = morse,
            speedWpm = state.soundSpeed.toInt(),
            volume = state.soundVolume,
            vibrate = state.vibrateEnabled,
            onStart = {
                _uiState.update { it.copy(isPlaying = true, statusMessage = "🔊 جاري تشغيل شفرة مورس...") }
            },
            onFinish = {
                _uiState.update { it.copy(isPlaying = false, statusMessage = "✅ انتهى التشغيل") }
            }
        )
    }

    private fun stopSound() {
        soundPlayer.stopPlaying()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun onSpeedChanged(speed: Float) = _uiState.update { it.copy(soundSpeed = speed) }
    fun onVolumeChanged(volume: Float) = _uiState.update { it.copy(soundVolume = volume) }
    fun onVibrateToggled(enabled: Boolean) = _uiState.update { it.copy(vibrateEnabled = enabled) }

    private fun setError(msg: String) = _uiState.update { it.copy(statusMessage = msg, isError = true) }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stopPlaying()
    }
}
