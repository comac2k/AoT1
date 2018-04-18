package com.example.comac.aot1

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Switch
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.io.IOException

private val TAG = MainActivity::class.java.simpleName
private val gpioButton1PinName = "BCM14"
private val gpioButton2PinName = "BCM15"
private val gpioForLED1_R = "BCM3"
private val gpioForLED1_G = "BCM4"
private val gpioForLED1_B = "BCM2"
private val gpioForLED2_R = "BCM27"
private val gpioForLED2_G = "BCM22"
private val gpioForLED2_B = "BCM17"

class MainActivity : Activity() {
    private val handler = Handler()
    private lateinit var button1Gpio: Gpio
    private lateinit var button2Gpio: Gpio
    private var ledState = false
    private lateinit var switch1View: Switch
    private lateinit var switch2View: Switch
    private val gpiosToClose = mutableListOf<Gpio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButton()
        setupLEDs()

        switch1View = findViewById(R.id.switch1)
        switch2View = findViewById(R.id.switch2)
    }

    private fun setupLEDs() {
        setupLED(R.id.led1_r, gpioForLED1_R)
        setupLED(R.id.led1_g, gpioForLED1_G)
        setupLED(R.id.led1_b, gpioForLED1_B)

        setupLED(R.id.led2_r, gpioForLED2_R)
        setupLED(R.id.led2_g, gpioForLED2_G)
        setupLED(R.id.led2_b, gpioForLED2_B)
    }

    private fun setupLED(switchViewId: Int, ledGpioName: String) {
        val ledGpio = openLED(ledGpioName)
        gpiosToClose.add(ledGpio)
        findViewById<Switch>(switchViewId).setOnCheckedChangeListener { button, checked -> ledGpio.value = checked }
    }

    private fun openLED(gpioName : String): Gpio {
        val gpio = PeripheralManager.getInstance().openGpio(gpioName)
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)
        gpio.setActiveType(Gpio.ACTIVE_LOW)
        return gpio
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyButton()
        gpiosToClose.forEach {
            it.value = false
            it.close()
        }
        gpiosToClose.clear()
    }

    private fun setupButton() {
        try {
            button1Gpio = PeripheralManager.getInstance().openGpio(gpioButton1PinName)
            button1Gpio.setDirection(Gpio.DIRECTION_IN)
            button1Gpio.setActiveType(Gpio.ACTIVE_HIGH)
            button1Gpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
            button1Gpio.registerGpioCallback {
                Log.i(TAG, "GPIO changed, button pressed")
                switch1View.isChecked = it.value
                // Return true to continue listening to events
                true
            }

            button2Gpio = PeripheralManager.getInstance().openGpio(gpioButton2PinName)
            button2Gpio.setDirection(Gpio.DIRECTION_IN)
            button2Gpio.setActiveType(Gpio.ACTIVE_HIGH)
            button2Gpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
            button2Gpio.registerGpioCallback {
                Log.i(TAG, "GPIO changed, button pressed")
                switch2View.isChecked = it.value
                // Return true to continue listening to events
                true
            }
        } catch (e: IOException) {
            // couldn't configure the button...
        }

    }

    private fun destroyButton() {
        Log.i(TAG, "Closing button")
        try {
            button1Gpio.close()
            button2Gpio.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing button", e)
        }
    }

}
