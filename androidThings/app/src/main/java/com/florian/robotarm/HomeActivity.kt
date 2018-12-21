package com.florian.robotarm

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException


// UART Device Name
private val UART_DEVICE_NAME: String = "USB1-1:1.0"


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class HomeActivity : Activity() {
    private var mDevice: UartDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        title = "RobotArm"

        seekBarServo1.setOnSeekBarChangeListener(onSeekBarChangeListener(textView1, R.string.s1Text, "s1"))
        seekBarServo2.setOnSeekBarChangeListener(onSeekBarChangeListener(textView2, R.string.s2Text, "s2"))
        seekBarServo3.setOnSeekBarChangeListener(onSeekBarChangeListener(textView3, R.string.s3Text, "s3"))
        seekBarServo4.setOnSeekBarChangeListener(onSeekBarChangeListener(textView1, R.string.s4Text, "s4"))
        seekBarServo5.setOnSeekBarChangeListener(onSeekBarChangeListener(textView2, R.string.s5Text, "s5"))
        seekBarServo6.setOnSeekBarChangeListener(onSeekBarChangeListener(textView3, R.string.s6Text, "s6"))

        initUsb()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeUsb()
    }

    override fun onStop() {
        super.onStop()
        // Interrupt events no longer necessary
        mDevice?.unregisterUartDeviceCallback(mUartCallback)
    }

    private val mUartCallback = object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Read available data from the UART device
            try {
                readUartBuffer(uart)
            } catch (e: IOException) {
                Log.w(TAG, "Unable to access UART device", e)
            }

            // Continue listening for more interrupts
            return true
        }

        override fun onUartDeviceError(uart: UartDevice?, error: Int) {
            Log.w(TAG, "$uart: Error event $error")
        }
    }

    fun writeUart(uart: UartDevice, value: String) {
        uart.apply {
            write(value.toByteArray(), value.length)
        }
    }

    @Throws(IOException::class)
    fun readUartBuffer(uart: UartDevice) {
        // Maximum amount of data to read at one time
        val maxCount = 10
        var string = ""
        uart.apply {
            ByteArray(maxCount).also { buffer ->
                var count: Int = read(buffer, buffer.size)
                string = string.plus(String(buffer, 0, count, Charsets.UTF_8))
                while (count > 0) {
                    Log.d(TAG, "Read $count bytes from peripheral")
                    count = read(buffer, buffer.size)
                    string = string.plus(String(buffer, 0, count, Charsets.UTF_8))
                }
                Log.d(TAG, "Read $string from peripheral")
            }
        }
    }

    private fun initUsb() {
        // Attempt to access the UART device
        mDevice = try {
            PeripheralManager.getInstance().openUartDevice(UART_DEVICE_NAME).apply {
                // Configure the UART port
                setBaudrate(9600)
                setDataSize(8)
                setParity(UartDevice.PARITY_NONE)
                setStopBits(1)

                registerUartDeviceCallback(mUartCallback)
            }
        } catch (e: IOException) {
            Log.w(TAG, "Unable to access UART device", e)
            null
        }
    }

    private fun closeUsb() {
        try {
            mDevice?.close()
            mDevice = null
        } catch (e: IOException) {
            Log.w(TAG, "Unable to close UART device", e)
        }
    }

    private fun onSeekBarChangeListener(myTextView1: TextView, servo1Text: Int, servoIdentifier: String): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            var progress = 0

            override fun onProgressChanged(seekBar: SeekBar?, progressValue: Int, fromUser: Boolean) {
                progress = progressValue
                Toast.makeText(applicationContext, "Changing seekbar's progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(applicationContext, "Started tracking seekbar", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                myTextView1.text = getString(servo1Text, progress, seekBar?.max)
                Toast.makeText(applicationContext, "Stopped tracking seekbar", Toast.LENGTH_SHORT).show()
                mDevice?.let { writeUart(it, "$servoIdentifier$progress") }
            }
        }
    }
}
