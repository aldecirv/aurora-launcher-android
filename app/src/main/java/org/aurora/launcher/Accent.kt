package org.aurora.launcher

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat

/**
 * Cor de destaque do app.
 * 0 em Prefs significa "usar a cor padrão do tema".
 */
object Accent {

    fun color(ctx: Context): Int {
        val stored = Prefs.accent(ctx)
        return if (stored != 0) stored else ContextCompat.getColor(ctx, R.color.aurora_accent)
    }

    /** Preto ou branco, o que tiver melhor contraste sobre a cor informada. */
    fun onColor(background: Int): Int {
        val r = Color.red(background) / 255.0
        val g = Color.green(background) / 255.0
        val b = Color.blue(background) / 255.0
        fun lin(v: Double) = if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        val luminance = 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b)
        return if (luminance > 0.45) Color.BLACK else Color.WHITE
    }

    fun stateList(color: Int): ColorStateList = ColorStateList.valueOf(color)

    fun stateListFocused(focused: Int, normal: Int): ColorStateList =
        ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(focused, normal)
        )

    fun circle(color: Int, sizePx: Int, strokeWidthPx: Int, strokeColor: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setSize(sizePx, sizePx)
            setStroke(strokeWidthPx, strokeColor)
        }
}
