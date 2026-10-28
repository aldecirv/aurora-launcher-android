package org.aurora.launcher

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        setupTheme()
        setupSliders()
        setupSpinners()
        setupReset()
    }

    private fun setupTheme() {
        val group = findViewById<RadioGroup>(R.id.themeGroup)
        when (Prefs.theme(this)) {
            Prefs.THEME_LIGHT -> group.check(R.id.themeLight)
            Prefs.THEME_DARK -> group.check(R.id.themeDark)
            else -> group.check(R.id.themeSystem)
        }
        group.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.themeLight -> Prefs.THEME_LIGHT
                R.id.themeDark -> Prefs.THEME_DARK
                else -> Prefs.THEME_SYSTEM
            }
            if (Prefs.theme(this) != value) {
                Prefs.setTheme(this, value)
                (application as AuroraApp).applyTheme()
            }
        }
    }

    private fun setupSliders() {
        bindSlider(
            R.id.iconSizeSlider, R.id.iconSizeValue, " dp",
            Prefs.iconSize(this), Prefs.ICON_MIN, Prefs.ICON_MAX
        ) { Prefs.setIconSize(this, it) }

        bindSlider(
            R.id.zoomSlider, R.id.zoomValue, "%",
            Prefs.zoom(this), Prefs.ZOOM_MIN, Prefs.ZOOM_MAX
        ) { Prefs.setZoom(this, it) }

        bindSlider(
            R.id.columnsSlider, R.id.columnsValue, "",
            Prefs.columns(this), Prefs.COLUMNS_MIN, Prefs.COLUMNS_MAX
        ) { Prefs.setColumns(this, it) }

        bindSlider(
            R.id.rowsSlider, R.id.rowsValue, "",
            Prefs.rows(this), Prefs.ROWS_MIN, Prefs.ROWS_MAX
        ) { Prefs.setRows(this, it) }
    }

    private fun bindSlider(
        sliderId: Int,
        valueId: Int,
        suffix: String,
        initial: Int,
        min: Int,
        max: Int,
        onChange: (Int) -> Unit
    ) {
        val slider = findViewById<Slider>(sliderId)
        val valueView = findViewById<TextView>(valueId)

        slider.valueFrom = min.toFloat()
        slider.valueTo = max.toFloat()
        slider.value = initial.coerceIn(min, max).toFloat()
        valueView.text = "${slider.value.toInt()}$suffix"

        slider.addOnChangeListener { _, value, fromUser ->
            val v = value.toInt()
            valueView.text = "$v$suffix"
            if (fromUser) onChange(v)
        }
    }

    private fun setupSpinners() {
        bindSpinner(
            R.id.styleSpinner,
            arrayOf(
                getString(R.string.style_original),
                getString(R.string.style_mono),
                getString(R.string.style_gray)
            ),
            Prefs.style(this)
        ) { Prefs.setStyle(this, it) }

        bindSpinner(
            R.id.shapeSpinner,
            arrayOf(
                getString(R.string.shape_original),
                getString(R.string.shape_rounded),
                getString(R.string.shape_circle)
            ),
            Prefs.shape(this)
        ) { Prefs.setShape(this, it) }

        bindSpinner(
            R.id.sortSpinner,
            arrayOf(getString(R.string.sort_name_asc), getString(R.string.sort_name_desc)),
            Prefs.sort(this)
        ) { Prefs.setSort(this, it) }
    }

    private fun bindSpinner(spinnerId: Int, options: Array<String>, selected: Int, onPick: (Int) -> Unit) {
        val spinner = findViewById<Spinner>(spinnerId)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.setSelection(selected.coerceIn(0, options.lastIndex), false)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onPick(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }

    private fun setupReset() {
        findViewById<android.view.View>(R.id.btnReset).setOnClickListener {
            Prefs.reset(this)
            IconLoader.clear()
            (application as AuroraApp).applyTheme()
            recreate()
        }
    }
}
