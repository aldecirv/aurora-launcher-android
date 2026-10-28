package org.aurora.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var indexBar: LinearLayout
    private lateinit var emptyView: TextView
    private lateinit var searchInput: TextInputEditText

    private var allApps: List<AppInfo> = emptyList()
    private var query: String = ""

    // Preferências aplicadas
    private var columns = Prefs.COLUMNS_DEFAULT
    private var rows = Prefs.ROWS_DEFAULT
    private var iconSizePx = 0
    private var textSizeSp = 17f
    private var shape = Prefs.SHAPE_ORIGINAL
    private var style = Prefs.STYLE_ORIGINAL
    private var sort = Prefs.SORT_ASC

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            reloadApps()
        }
    }
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Prefs.bgMode(this) == Prefs.BG_WALLPAPER) {
            setTheme(R.style.Theme_Aurora_Wallpaper)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Background.apply(this)

        recycler = findViewById(R.id.recycler)
        indexBar = findViewById(R.id.indexBar)
        emptyView = findViewById(R.id.emptyView)
        searchInput = findViewById(R.id.searchInput)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { showSortMenu(it) }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString()?.trim().orEmpty()
                render()
            }
        })

        readPreferences()
        applyAccent()
        reloadApps()
    }

    override fun onResume() {
        super.onResume()
        Background.apply(this)
        readPreferences()
        applyAccent()
        reloadApps()
        registerPackageReceiver()
    }

    override fun onPause() {
        super.onPause()
        if (receiverRegistered) {
            try {
                unregisterReceiver(packageReceiver)
            } catch (_: IllegalArgumentException) {
                // já removido
            }
            receiverRegistered = false
        }
    }

    override fun onBackPressed() {
        // Launcher: não sai com o botão voltar.
    }

    private fun registerPackageReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(packageReceiver, filter)
        }
        receiverRegistered = true
    }

    /** Lê as preferências e calcula os tamanhos em pixels. */
    private fun readPreferences() {
        val dm = resources.displayMetrics
        val zoom = Prefs.zoom(this) / 100f

        columns = Prefs.columns(this).coerceIn(Prefs.COLUMNS_MIN, Prefs.COLUMNS_MAX)
        rows = Prefs.rows(this).coerceIn(Prefs.ROWS_MIN, Prefs.ROWS_MAX)
        iconSizePx = (Prefs.iconSize(this) * dm.density * zoom).toInt().coerceAtLeast(24)
        textSizeSp = 17f * zoom
        shape = Prefs.shape(this)
        style = Prefs.style(this)
        sort = Prefs.sort(this)
    }

    /** Aplica a cor de destaque escolhida ao campo de busca e aos botões. */
    private fun applyAccent() {
        val accent = Accent.color(this)

        findViewById<TextInputLayout>(R.id.searchLayout).apply {
            setBoxStrokeColorStateList(
                Accent.stateListFocused(accent, getColor(R.color.aurora_divider))
            )
            setStartIconTintList(Accent.stateList(accent))
            setEndIconTintList(Accent.stateList(accent))
        }

        listOf(R.id.btnSort, R.id.btnSettings).forEach { id ->
            findViewById<ImageButton>(id).imageTintList = Accent.stateList(accent)
        }
    }

    /** Recarrega a lista de apps instalados. */
    private fun reloadApps() {
        allApps = loadApps()
        render()
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg == packageName) return@mapNotNull null
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun filteredApps(): List<AppInfo> {
        val base = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
        return when (sort) {
            Prefs.SORT_DESC -> base.sortedByDescending { it.label.lowercase() }
            else -> base.sortedBy { it.label.lowercase() }
        }
    }

    private fun letterOf(app: AppInfo): Char =
        app.label.firstOrNull()?.uppercaseChar() ?: '#'

    /** Monta a lista com cabeçalhos de letra e atualiza a tela. */
    private fun render() {
        val apps = filteredApps()

        val rowsList = mutableListOf<Row>()
        var lastLetter: Char? = null
        apps.forEach { app ->
            val letter = letterOf(app)
            if (letter != lastLetter) {
                rowsList.add(Row.Header(letter))
                lastLetter = letter
            }
            rowsList.add(Row.App(app))
        }

        val gridMode = columns > 1
        val cellHeight = if (gridMode) {
            (resources.displayMetrics.heightPixels / rows).coerceAtLeast(96)
        } else {
            0
        }

        val adapter = AppAdapter(
            context = this,
            rows = rowsList,
            iconSizePx = iconSizePx,
            textSizeSp = textSizeSp,
            shape = shape,
            style = style,
            gridMode = gridMode,
            cellHeightPx = cellHeight
        ) { app -> openApp(app) }

        val span = columns.coerceIn(1, 5)
        val layoutManager = GridLayoutManager(this, span)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (rowsList.getOrNull(position) is Row.Header) span else 1
        }

        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        emptyView.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
        emptyView.text = if (allApps.isEmpty()) {
            getString(R.string.no_apps_installed)
        } else {
            getString(R.string.no_apps_found)
        }

        buildIndex(rowsList)
    }

    private fun buildIndex(rowsList: List<Row>) {
        indexBar.removeAllViews()
        indexBar.visibility = if (rowsList.isEmpty()) View.GONE else View.VISIBLE

        val letters = rowsList.mapNotNull { (it as? Row.Header)?.letter }
        letters.forEach { letter ->
            val tv = TextView(this).apply {
                text = letter.toString()
                setTextColor(Accent.color(this@MainActivity))
                textSize = 10f * (Prefs.zoom(this@MainActivity) / 100f)
                gravity = Gravity.CENTER
                setPadding(10, 6, 10, 6)
            }
            tv.setOnClickListener {
                val pos = rowsList.indexOfFirst { it is Row.Header && it.letter == letter }
                if (pos >= 0) recycler.scrollToPosition(pos)
            }
            indexBar.addView(tv)
        }
    }

    private fun showSortMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, Prefs.SORT_ASC, 0, getString(R.string.sort_name_asc))
            menu.add(0, Prefs.SORT_DESC, 1, getString(R.string.sort_name_desc))
            setOnMenuItemClickListener { item ->
                Prefs.setSort(this@MainActivity, item.itemId)
                readPreferences()
                render()
                true
            }
            show()
        }
    }

    private fun openApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // app indisponível no momento — ignora
        }
    }
}
