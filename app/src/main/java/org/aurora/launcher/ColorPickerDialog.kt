package org.aurora.launcher

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/**
 * Seletor de cor simples (R, G, B) com prévia e código hexadecimal.
 */
class ColorPickerDialog(
    context: Context,
    initialColor: Int,
    private val onPicked: (Int) -> Unit
) : Dialog(context) {

    private var red = Color.red(initialColor)
    private var green = Color.green(initialColor)
    private var blue = Color.blue(initialColor)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)

        val preview = findViewById<View>(R.id.colorPreview)
        val hexLabel = findViewById<TextView>(R.id.colorHex)
        val seekR = findViewById<SeekBar>(R.id.seekR)
        val seekG = findViewById<SeekBar>(R.id.seekG)
        val seekB = findViewById<SeekBar>(R.id.seekB)

        fun current() = Color.rgb(red, green, blue)

        fun refresh() {
            val color = current()
            preview.background = GradientDrawable().apply {
                cornerRadius = 18f * context.resources.displayMetrics.density
                setColor(color)
            }
            hexLabel.text = String.format("#%06X", 0xFFFFFF and color)
        }

        fun bind(seek: SeekBar, value: Int, onChange: (Int) -> Unit) {
            seek.max = 255
            seek.progress = value
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    onChange(progress)
                    refresh()
                }
                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
        }

        bind(seekR, red) { red = it }
        bind(seekG, green) { green = it }
        bind(seekB, blue) { blue = it }
        refresh()

        findViewById<MaterialButton>(R.id.btnOk).setOnClickListener {
            onPicked(current())
            dismiss()
        }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { dismiss() }
    }
}
