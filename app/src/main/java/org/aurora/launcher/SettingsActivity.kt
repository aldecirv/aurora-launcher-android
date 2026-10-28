package org.aurora.launcher

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private lateinit var accentRow: LinearLayout

    private val presetColors = listOf(
        0,                      // padrão do tema
        0xFF22D3EE.toInt(),     // ciano
        0xFF4ADE80.toInt(),     // verde
        0xFF60A5FA.toInt(),     // azul
        0xFFA78BFA.toInt(),     // roxo
        0xFFF472B6.toInt(),     // rosa
        0xFFFB923C.toInt(),     // laranja
        0xFFF87171.toInt(),     // vermelho
        0xFFE5E7EB.toInt()      // cinza claro
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        accentRow = findViewById(R.id.accentRow)
        buildAccentRow()

        findViewById<MaterialButton>(R.id.btnCustomColor).setOnClickListener {
            ColorPickerDialog(this, Accent.color(this)) { color ->
                applyAccentChoice(color)
            }.show()
        }

        setupTheme()
        setupSliders()
        setupSpinners()
        setupReset()
        applyAccent()
    }

    private fun applyAccentChoice(color: Int) {
        Prefs.setAccent(this, color)
        IconLoader.clear()
        buildAccentRow()
        recreate()
    }

    private fun buildAccentRow() {
        accentRow.removeAllViews()
        val current = Prefs.accent(this)
        val density = resources.displayMetrics.density
        val size = (40 * density).toInt()
        val margin = (10 * density).toInt()

        presetColors.forEach { value ->
            val color = if (value == 0) ContextCompat.getColor(this, R.color.aurora_accent) else value
            val selected = current == value

            val swatch = android.view.View(this)
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = margin
            swatch.layoutParams = lp
            swatch.background = Accent.circle(
                color,
                size,
                if (selected) (3 * density).toInt() else (1 * density).toInt(),
                if (selected) Color.WHITE else ContextCompat.getColor(this, R.color.aurora_divider)
            )
            swatch.contentDescription = if (value == 0) getString(R.string.accent_default) else String.format("#%06X", 0xFFFFFF and value)
            swatch.setOnClickListener { applyAccentChoice(value) }
            accentRow.addView(swatch)
        }
    }

    private fun applyAccent() {
        val accent = Accent.color(this)

        listOf(R.id.iconSizeSlider, R.id.zoomSlider, R.id.columnsSlider, R.id.rowsSlider).forEach { id ->
            findViewById<Slider>(id).apply {
                trackTintList = Accent.stateList(accent)
                thumbTintList = Accent.stateList(accent)
                haloTintList = Accent.stateList(accent)
            }
        }

        listOf(R.id.themeSystem, R.id.themeLight, R.id.themeDark).forEach { id ->
            findViewById<RadioButton>(id).buttonTintList = Accent.stateList(accent)
        }

        findViewById<MaterialButton>(R.id.btnReset).apply {
            backgroundTintList = Accent.stateList(accent)
            setTextColor(Accent.onColor(accent))
        }

        findViewById<MaterialButton>(R.id.btnCustomColor).apply {
            strokeColor = Accent.stateList(accent)
            setTextColor(accent)
            iconTint = Accent.stateList(accent)
        }

        findViewById<MaterialToolbar>(R.id.toolbar).navigationIcon?.setTint(accent)
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
