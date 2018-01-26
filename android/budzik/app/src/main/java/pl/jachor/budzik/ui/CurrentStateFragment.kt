package pl.jachor.budzik.ui

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_current_state.*
import kotlinx.android.synthetic.main.fragment_select_device.*
import pl.jachor.budzik.LightIntensityPolicy
import pl.jachor.budzik.R
import pl.jachor.budzik.inject.ApplicationLevelInjectors
import pl.jachor.budzik.utils.LightClient
import pl.jachor.budzik.utils.LightUpdater
import pl.jachor.budzik.utils.NetworkScanner
import pl.jachor.budzik.utils.Settings
import javax.inject.Inject

/**
 * Dialog allowing user to choose device from one of detected on local network.
 */
class CurrentStateFragment : Fragment() {
    @Inject
    internal lateinit var lightIntensityPolicy: LightIntensityPolicy
    @Inject
    lateinit var lightUpdaterFactory: LightUpdater.Factory
    lateinit var lightUpdater: LightUpdater
    lateinit var updateHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationLevelInjectors.get(this).inject(this)
        super.onCreate(savedInstanceState)
        lightUpdater = lightUpdaterFactory.create("-i", LIGHT_WRITE_VALIDITY_MS)
        updateHandler = Handler(context.mainLooper)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_current_state, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        brightness_slider.max = LightClient.MAX_BRIGHTNESS
        brightness_slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lightUpdater.update(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        updateHandler.post(this::updateUiTask)
    }

    override fun onPause() {
        lightUpdater.stopTask()
        updateHandler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    private fun updateUiTask() {
        val result = lightIntensityPolicy.run()
        state_text.text = result.toString()
        updateHandler.postDelayed(this::updateUiTask, 500)
    }

    companion object {
        val LIGHT_WRITE_VALIDITY_MS = 10 * 1000
    }
}