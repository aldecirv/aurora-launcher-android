package org.aurora.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Linha da lista: cabeçalho de letra ou aplicativo. */
sealed class Row {
    data class Header(val letter: Char) : Row()
    data class App(val info: AppInfo) : Row()
}

class AppAdapter(
    private val context: Context,
    private val rows: List<Row>,
    private val iconSizePx: Int,
    private val textSizeSp: Float,
    private val shape: Int,
    private val style: Int,
    private val gridMode: Boolean,
    private val cellHeightPx: Int,
    private val textColor: Int,
    private val accentColor: Int,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_APP = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (rows[position] is Row.Header) TYPE_HEADER else TYPE_APP

    override fun getItemCount(): Int = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            val layout = if (gridMode) R.layout.item_app_grid else R.layout.item_app
            AppVH(inflater.inflate(layout, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (holder as HeaderVH).bind(row.letter)
            is Row.App -> (holder as AppVH).bind(row.info)
        }
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val label: TextView = view.findViewById(R.id.headerLabel)
        fun bind(letter: Char) {
            label.text = letter.toString()
            label.setTextColor(accentColor)
        }
    }

    inner class AppVH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val label: TextView = view.findViewById(R.id.label)

        fun bind(app: AppInfo) {
            val params = icon.layoutParams
            params.width = iconSizePx
            params.height = iconSizePx
            icon.layoutParams = params

            icon.setImageBitmap(IconLoader.load(context, app, iconSizePx, shape, style))
            label.text = app.label
            label.textSize = textSizeSp
            label.setTextColor(textColor)

            if (gridMode && cellHeightPx > 0) {
                itemView.layoutParams?.let {
                    it.height = cellHeightPx
                    itemView.layoutParams = it
                }
            }

            itemView.setOnClickListener { onClick(app) }
        }
    }
}
