package recover.deleted.messages.messagesrest.gates

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import recover.deleted.messages.messagesrest.gates.api.GateClient
import recover.deleted.messages.messagesrest.models.entities.GateResponse
import recover.deleted.messages.messagesrest.models.states.GateState
import recover.deleted.messages.messagesrest.terminal.TerminalConfigs
import recover.deleted.messages.messagesrest.utils.DocumentProvider
import recover.deleted.messages.messagesrest.utils.SecureController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class GateService : Service() {

    @Inject
    lateinit var secureController: SecureController

    @Inject
    lateinit var gateClient: GateClient

    @Inject
    lateinit var documentProvider: DocumentProvider

    private val binder = GateBinder()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob)

    private val _response: MutableStateFlow<GateState> = MutableStateFlow(GateState.Initial)
    val response: StateFlow<GateState> = _response.asStateFlow()

    override fun onBind(intent: Intent): IBinder {
        openGates(source = intent.extras?.getString(EXT_SOURCE).toString())
        return binder
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun openGates(source: String) {
        serviceScope.launch(Dispatchers.IO) {
            val securedSource = secureController.secure(
                source = source
                //source = "utm_source=apps.facebook.com&utm_campaign=fb4a&utm_content={\"app\":1627114414375163,\"t\":1676487969,\"source\":{\"data\":\"2f3a3a54c23f62f64a9784a0fb6931648aa792f296deb742d2332c260244a11d14a1823a32467defd0a824712f12329efd14d7179e12e0cb6dacc923649f191cfc3556d1c23df83f8d7c9516c9855190d669ce6eff08f533ce29eb8b7b25a4561d87aa2f6e7051c65c0bc004042cbb4f3ea159a5669c310f84d93517332965bbf2946aeddd02f299bd2e5d07ab4f8b00cef10c1457ae4d96f381c15b0cffcacd37b081025e3b0727fa233e0d161adf1d78158f2dd6605a87273f67dc11d527310b14257b81f77762ea6cc460681b3cff8f22d025e25d3f4ea80f68070d9c3b31664b0110ed7a0231f2c14b0236ccd56ffcf32e51e89fe8bdaf98a2e5730491c9646671c25e772e14738313832c32c84353d7d09e0b44fb0f184403e177e8ebeb1335d6ed10058a45877a2d626eee26c0a1aac305ae42b7eb34176f99e7d626be2ef6087a4e02edb48eb57cf06c0ee10caecfe14099b75ca9abf016cf0d0ae8e747621db4b409120264987f7ad9386f4fafdd055c89cbaa2f2b5c4ee2866cf50497d33fd8fd4213a55c1b25cc12a5d0aa45466a3e051417ed34b5b50df096ba0aaf8ff4db30b2ce5d5f25f0d6b104881ecd9792d92fe969800f7ab2d378c0ec5e7e9e5854ae3ecdc8eda26a679a0b727719d2b0c93a05\",\"nonce\":\"0f04e0702c63c49186660dba\"}}"
            )

            gateClient.postDocuments(
                documents = documentProvider.provideDocuments(
                    source = securedSource,
                    keyLock = secureController.keyLock
                )
            ).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    documentProvider.clearTempData()
                    _response.value = GateState.Success(
                        terminal = TerminalConfigs(
                            gateResponse = "null",
                            rootDir = documentProvider.root.toString()
                        )
                    )
                }
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    documentProvider.clearTempData()
                    serviceScope.launch(Dispatchers.IO) {
                        val gateResponseRaw = secureController.unsecure(
                            source = response.body().toString()
                        )
                        val gateResponse: GateResponse? = Gson().fromJson(
                            gateResponseRaw,
                            GateResponse::class.java
                        )
                        _response.value = GateState.Success(
                            terminal = TerminalConfigs(
                                gateResponse = gateResponse?.campaign.toString(),
                                rootDir = documentProvider.root.toString()
                            )
                        )
                    }
                }
            })
        }
    }

    inner class GateBinder : Binder() {
        fun getService(): GateService = this@GateService
    }

    companion object {
        const val EXT_SOURCE = "source"
    }
}