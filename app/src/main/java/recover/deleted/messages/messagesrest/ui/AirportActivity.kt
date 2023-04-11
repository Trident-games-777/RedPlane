package recover.deleted.messages.messagesrest.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.webkit.ValueCallback
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import recover.deleted.messages.messagesrest.databinding.ActivityAirportBinding
import recover.deleted.messages.messagesrest.gates.GateService
import recover.deleted.messages.messagesrest.models.states.AirportState
import recover.deleted.messages.messagesrest.terminal.TerminalEventListener
import recover.deleted.messages.messagesrest.terminal.TerminalView
import recover.deleted.messages.messagesrest.viewmodels.AirportViewModel

@AndroidEntryPoint
class AirportActivity : AppCompatActivity(), TerminalEventListener {

    private lateinit var binding: ActivityAirportBinding
    private var browserCallback: ValueCallback<Array<Uri>>? = null

    private val viewModel: AirportViewModel by viewModels()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as GateService.GateBinder
            val gateService = binder.getService()
            lifecycleScope.launch {
                gateService.response.collectLatest { state ->
                    viewModel.onGateOpened(gateState = state)
                }
            }
        }
        override fun onServiceDisconnected(className: ComponentName) {

        }
    }
    private val terminalLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        browserCallback?.onReceiveValue(it.toTypedArray())
    }
    private val bpCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            with (binding.terminalView) {
                if (isVisible && canGoBack()) {
                    goBack()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAirportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindUI()
        observeData()
    }

    override fun browseDirectory(browserCallback: ValueCallback<Array<Uri>>?) {
        this.browserCallback = browserCallback
        terminalLauncher.launch(TerminalView.DATA_TYPE)
    }

    override fun executeCommand(command: String?) {
        viewModel.executeCommand(command = command)
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AirportState.Prepared ->
                        Intent(
                            this@AirportActivity,
                            GateService::class.java
                        ).also { intent ->
                            intent.putExtra(GateService.EXT_SOURCE, state.source)
                            bindService(intent, connection, Context.BIND_AUTO_CREATE)
                        }
                    is AirportState.Ready -> loginTerminal(terminal = state.terminal)
                    AirportState.Fly ->
                        Intent(
                            this@AirportActivity,
                            AirplaneActivity::class.java
                        ).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    AirportState.Initial -> Unit
                }
            }
        }
    }

    private fun bindUI() {
        binding.terminalView.setEventListener(eventListener = this)
        onBackPressedDispatcher.addCallback(this, bpCallback)
    }

    private fun loginTerminal(terminal: String) {
        with (binding) {
            container.visibility = View.GONE
            terminalView.visibility = View.VISIBLE
            terminalView.loadUrl(terminal)
        }
    }
}