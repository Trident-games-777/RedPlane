package recover.deleted.messages.messagesrest.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import recover.deleted.messages.messagesrest.R
import recover.deleted.messages.messagesrest.databinding.ActivityAirplaneBinding
import recover.deleted.messages.messagesrest.models.states.AirplaneState
import recover.deleted.messages.messagesrest.viewmodels.AirplaneViewModel

@AndroidEntryPoint
class AirplaneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAirplaneBinding
    private lateinit var flyStartAnim: ObjectAnimator
    private lateinit var flyEndAnim: ObjectAnimator
    private lateinit var turbulenceAnim: ObjectAnimator

    private val viewModel by viewModels<AirplaneViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirplaneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewAnimations()
        bindUIActions()
        observeData()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onDestroy() {
        flyStartAnim.cancel()
        flyEndAnim.cancel()
        turbulenceAnim.cancel()
        super.onDestroy()
    }

    private fun setViewAnimations() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        flyStartAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.airplane,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -screenWidth, 0f)
        ).apply {
            duration = 500

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    turbulenceAnim.start()
                }
            })
        }

        flyEndAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.airplane,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, screenWidth)
        ).apply {
            duration = 500

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setValues(
                        PropertyValuesHolder.ofFloat(
                            View.TRANSLATION_X,
                            binding.airplane.translationX,
                            screenWidth
                        )
                    )
                    super.onAnimationStart(animation)
                }
            })
        }

        turbulenceAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.airplane,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, viewModel.randFloat(), 0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, viewModel.randFloat(), 0f),
        ).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 500

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    // set new random values for translation on each shake repeat
                    setValues(
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, viewModel.randFloat(), 0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, viewModel.randFloat(), 0f),
                    )
                }
            })
        }
    }

    private fun bindUIActions() {
        with (binding) {
            btnAdd1.setOnClickListener { viewModel.increaseBet(1) }
            btnAdd5.setOnClickListener { viewModel.increaseBet(5) }
            btnAdd10.setOnClickListener { viewModel.increaseBet(10) }
            btnDown1.setOnClickListener { viewModel.increaseBet(-1)  }
            btnDown5.setOnClickListener { viewModel.increaseBet(-5) }
            btnDown10.setOnClickListener { viewModel.increaseBet(-10) }
            btnBet.setOnClickListener { it.isEnabled = false; viewModel.makeBet() }
            btnTakeOff.setOnClickListener { it.isEnabled = false; viewModel.takeOf() }
            btnResetCredits.setOnClickListener { viewModel.resetCredits(); spentMenu.visibility = View.GONE }
            spentMenu.setOnClickListener {  }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AirplaneState.FlyStart -> onFlyStart(state.hasBet)
                    AirplaneState.FlyEnd -> onFlyEnd()
                    AirplaneState.GameOver -> onGameOver()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.coefficient.collectLatest { coefficient ->
                binding.coefficient.text = String.format("%.2f", coefficient)
            }
        }
        lifecycleScope.launch {
            viewModel.coefficientColor.collectLatest { color ->
                binding.coefficient.setBackgroundColor(color)
            }
        }
        lifecycleScope.launch {
            viewModel.progress.collectLatest { progress ->
                binding.progressBar.progress = progress
            }
        }
        lifecycleScope.launch {
            viewModel.bet.collectLatest { bet ->
                binding.txtBet.text = bet.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.balance.collectLatest { balance ->
                binding.txtBalance.text = getString(R.string.balance, balance)
            }
        }
        lifecycleScope.launch {
            viewModel.currBet.collectLatest { currBet ->
                binding.txtCurrBet.visibility =
                    if (currBet > 0) View.VISIBLE
                    else View.GONE
                binding.txtCurrBet.text = getString(R.string.curr_bet, currBet)
            }
        }
    }

    private fun onFlyStart(hasBet: Boolean) {
        with (binding) {
            coefficient.visibility = View.VISIBLE
            progress.visibility = View.GONE
            btnBet.isEnabled = false
            btnTakeOff.isEnabled = hasBet
        }

        flyEndAnim.cancel()
        turbulenceAnim.cancel()
        flyStartAnim.start()
    }

    private fun onFlyEnd() {
        with (binding) {
            coefficient.visibility = View.GONE
            progress.visibility = View.VISIBLE
            btnBet.isEnabled = true
            btnTakeOff.isEnabled = false
        }

        flyStartAnim.cancel()
        turbulenceAnim.cancel()
        flyEndAnim.start()
    }

    private fun onGameOver() {
        with (binding) {
            coefficient.visibility = View.GONE
            btnBet.isEnabled = false
            btnTakeOff.isEnabled = false
            spentMenu.visibility = View.VISIBLE
        }

        flyStartAnim.cancel()
        turbulenceAnim.cancel()
        flyEndAnim.cancel()
    }
}