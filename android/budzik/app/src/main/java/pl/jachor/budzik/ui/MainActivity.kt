package pl.jachor.budzik.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.jachor.budzik.R
import pl.jachor.budzik.inject.ApplicationLevelInjectors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationLevelInjectors.get(this).inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        select_device_button.setOnClickListener {
            SelectDeviceFragment().show(supportFragmentManager, "select-device-fragment")
        }
    }
}
