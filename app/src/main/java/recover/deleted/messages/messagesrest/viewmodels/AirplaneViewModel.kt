package recover.deleted.messages.messagesrest.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import recover.deleted.messages.messagesrest.models.states.AirplaneState
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@HiltViewModel
class AirplaneViewModel @Inject constructor() : ViewModel() {

    private val _state: MutableStateFlow<AirplaneState> = MutableStateFlow(AirplaneState.FlyEnd)
    val state: StateFlow<AirplaneState> = _state.asStateFlow()

    private val _coefficient: MutableStateFlow<Float> = MutableStateFlow(1f)
    val coefficient: StateFlow<Float> = _coefficient.asStateFlow()

    private val _coefficientColor: MutableStateFlow<Int> = MutableStateFlow(0)
    val coefficientColor: StateFlow<Int> = _coefficientColor.asStateFlow()

    private val _progress: MutableStateFlow<Int> = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _balance: MutableStateFlow<Int> = MutableStateFlow(1000)
    val balance: StateFlow<Int> = _balance.asStateFlow()

    private val _bet: MutableStateFlow<Int> = MutableStateFlow(10)
    val bet: StateFlow<Int> = _bet.asStateFlow()

    private val _currBet: MutableStateFlow<Int> = MutableStateFlow(-1)
    val currBet: StateFlow<Int> = _currBet.asStateFlow()

    private val random = Random(System.currentTimeMillis())
    private val handler = Handler(Looper.getMainLooper())
    private val cColDefault = Triple(255, 255, 0)

    private var coefficientColorInt = cColDefault
    private var targetCoefficient = -1f

    fun randFloat(): Float =
        random.nextInt(-SHAKE_AMOUNT,SHAKE_AMOUNT + 1).toFloat()

    fun increaseBet(by: Int) {
        val minLimit = max(1, _bet.value + by)
        _bet.value = min(minLimit, _balance.value)
    }

    fun makeBet() {
        _currBet.value = _bet.value
        _balance.value -= _currBet.value
        _bet.value = min(_balance.value, _bet.value)
    }

    fun takeOf() {
        _balance.value += (_currBet.value * _coefficient.value).toInt()
        _bet.value = max(1, _bet.value)
    }

    fun onPause() {
        handler.removeCallbacksAndMessages(null)
    }

    fun onResume() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val resume: Boolean = when (_state.value) {
                    is AirplaneState.FlyStart -> {  onFlyStart(); true }
                    AirplaneState.FlyEnd -> { onFlyEnd(); true }
                    AirplaneState.GameOver -> false
                }
                if (resume) handler.postDelayed(this, TICK_DURATION)
            }
        }, TICK_DURATION)
    }

    fun resetCredits() {
        _coefficient.value = 1f
        _coefficientColor.value = 0
        _progress.value = 0
        _balance.value = 1000
        _bet.value = 10
        _currBet.value = -1
        _state.value = AirplaneState.FlyEnd
        onResume()
    }

    private fun onFlyStart() {
        if (targetCoefficient == -1f) setCoefficient()

        _coefficient.value += 0.01f + _coefficient.value * 0.1f
        _coefficientColor.value = calculateColor()

        // Check fly end
        if (_coefficient.value >= targetCoefficient) {
            _currBet.value = -1
            _state.value =
                if (_balance.value > 0) AirplaneState.FlyEnd
                else AirplaneState.GameOver
            _coefficient.value = 1f
            coefficientColorInt = cColDefault
            targetCoefficient = -1f
        }
    }

    private fun onFlyEnd() {
        _progress.value += 1

        // Check fly start
        if (_progress.value == 100) {
            _state.value = AirplaneState.FlyStart(_currBet.value != -1)
            _progress.value = 0
        }
    }

    private fun setCoefficient() {
        targetCoefficient = when (random.nextInt(100)) {
            in 98..100 -> random.nextDouble(100.0, 200.0)
            in 93..98 -> random.nextDouble(50.0, 100.0)
            in 87..93 -> random.nextDouble(20.0, 50.0)
            in 75..87 -> random.nextDouble(5.0, 20.0)
            in 60..75 -> random.nextDouble(1.5, 5.0)
            else -> random.nextDouble(1.1, 1.5)
        }.toFloat()
    }

    private fun calculateColor(): Int {
        coefficientColorInt = when {
            coefficientColorInt.third == 255 && coefficientColorInt.second > 0 -> {
                coefficientColorInt.copy(
                    first = min(255, coefficientColorInt.first + COEFFICIENT_COLOR_OFFSET),
                    second = max(0, coefficientColorInt.second - COEFFICIENT_COLOR_OFFSET)
                )
            }
            coefficientColorInt.second == 255 -> {
                if (coefficientColorInt.first > 0) {
                    coefficientColorInt.copy(
                        first = max(0, coefficientColorInt.first - COEFFICIENT_COLOR_OFFSET)
                    )
                } else {
                    coefficientColorInt.copy(
                        third = min(255, coefficientColorInt.third + COEFFICIENT_COLOR_OFFSET)
                    )
                }
            }
            else -> {
                coefficientColorInt.copy(
                    second = min(255, coefficientColorInt.second + COEFFICIENT_COLOR_OFFSET),
                    third = max(0, coefficientColorInt.third - COEFFICIENT_COLOR_OFFSET)
                )
            }
        }

        return COEFFICIENT_COLOR_ALPHA and 0xff shl 24 or
                (coefficientColorInt.first and 0xff shl 16) or
                (coefficientColorInt.second and 0xff shl 8) or
                (coefficientColorInt.third and 0xff)
    }

    companion object {
        private const val TICK_DURATION = 100L
        private const val SHAKE_AMOUNT = 10
        private const val COEFFICIENT_COLOR_ALPHA = 70
        private const val COEFFICIENT_COLOR_OFFSET = 19
    }
}