package recover.deleted.messages.messagesrest.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import recover.deleted.messages.messagesrest.BuildConfig
import recover.deleted.messages.messagesrest.database.RampRepository
import recover.deleted.messages.messagesrest.models.entities.Ramp
import recover.deleted.messages.messagesrest.utils.SourceClient
import recover.deleted.messages.messagesrest.models.states.AirportState
import recover.deleted.messages.messagesrest.models.states.GateState
import recover.deleted.messages.messagesrest.terminal.TerminalConfigs
import javax.inject.Inject

@HiltViewModel
class AirportViewModel @Inject constructor(
    private val sourceClient: SourceClient,
    private val rampRepository: RampRepository
) : ViewModel() {

    private val _state: MutableStateFlow<AirportState> = MutableStateFlow(AirportState.Initial)
    val state: StateFlow<AirportState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val ramp = rampRepository.getFirst()
            if (ramp != null) {
                _state.value =
                    if (ramp.isLanded) AirportState.Fly
                    else AirportState.Ready(terminal = ramp.terminal)
            } else if (Ramp(landed = sourceClient.sourceCredits())
                    .let {
                        if (it.isLanded) {
                            rampRepository.insert(it)
                            _state.value = AirportState.Fly
                        }
                        !it.isLanded
                    }
            ) {
                val source = sourceClient.connect().first()
                _state.value = AirportState.Prepared(source = source)
            }
        }
    }

    fun onGateOpened(gateState: GateState) {
        when (gateState) {
            is GateState.Success -> passGate(terminal = gateState.terminal)
            is GateState.Error -> Log.e("RequestError", gateState.message)
            GateState.Initial -> Unit
        }
    }

    fun executeCommand(command: String?) {
        when {
            command == null -> Unit
            command == BuildConfig.BASE_URL -> _state.value = AirportState.Fly
            command.contains(BuildConfig.BASE_URL).not() -> saveCommand(command = command)
        }
    }

    private fun passGate(terminal: TerminalConfigs) {
        _state.value = AirportState.Ready(terminal = terminal.toString())
    }

    private fun saveCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            rampRepository.insert(
                Ramp(
                    terminal = command,
                    landed = "0"
                )
            )
        }
    }
}