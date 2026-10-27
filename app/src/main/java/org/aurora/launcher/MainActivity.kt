package org.aurora.launcher

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var indexBar: LinearLayout
    private var apps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        indexBar = findViewById(R.id.indexBar)

        apps = loadApps()

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = AppAdapter(apps) { app -> openApp(app) }

        buildIndex()
    }

    override fun onBackPressed() {
        // Um launcher não deve "sair" com o botão voltar.
    }

    override fun onResume() {
        super.onResume()
        val atualizados = loadApps()
        if (atualizados.size != apps.size) {
            apps = atualizados
            recycler.adapter = AppAdapter(apps) { app -> openApp(app) }
            buildIndex()
        }
    }

    private fun openApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(intent)
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

    private fun letraDe(app: AppInfo): Char =
        app.label.firstOrNull()?.uppercaseChar() ?: '#'

    private fun buildIndex() {
        indexBar.removeAllViews()
        val letras = apps.map { letraDe(it) }.distinct().sorted()
        letras.forEach { letra ->
            val tv = TextView(this).apply {
                text = letra.toString()
                setTextColor(0xFF8EA0B5.toInt())
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(10, 6, 10, 6)
            }
            tv.setOnClickListener {
                val pos = apps.indexOfFirst { letraDe(it) == letra }
                if (pos >= 0) recycler.scrollToPosition(pos)
            }
            indexBar.addView(tv)
        }
    }
}
