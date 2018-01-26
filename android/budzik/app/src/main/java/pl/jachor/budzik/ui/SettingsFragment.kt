package pl.jachor.budzik.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_settings.*
import pl.jachor.budzik.R
import pl.jachor.budzik.inject.ApplicationLevelInjectors
import pl.jachor.budzik.service.LightService
import pl.jachor.budzik.utils.Settings
import javax.inject.Inject

/**
 * Fragment with sun-simulation settings.
 */
class SettingsFragment : Fragment() {
    @Inject
    internal lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationLevelInjectors.get(this).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enable.setOnCheckedChangeListener { _, isChecked ->
            settings.isEnabled = isChecked
            updateService()
        }
        bindTextEditChangesToInt(time_before_alarm_minutes, settings::leadInTimeMinutes::set)
        bindTextEditChangesToInt(time_after_alarm_minutes, settings::leadOutTimeMinutes::set)
    }

    override fun onResume() {
        super.onResume()

        enable.isChecked = settings.isEnabled
        time_before_alarm_minutes.setText(settings.leadInTimeMinutes.toString())
        time_after_alarm_minutes.setText(settings.leadOutTimeMinutes.toString())
    }

    private fun bindTextEditChangesToInt(edit: EditText, writeFn: (Int)->Unit) {
        edit.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {
                    writeFn(edit.text.toString().toInt())
                    updateService()
                } catch (e: NumberFormatException) {
                    // ignore
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateService() {
        LightService.start(context)
    }
}