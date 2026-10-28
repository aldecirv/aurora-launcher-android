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

    /** Luminância relativa (WCAG). */
    fun luminance(color: Int): Double {
        fun lin(v: Double) = if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        val r = lin(Color.red(color) / 255.0)
        val g = lin(Color.green(color) / 255.0)
        val b = lin(Color.blue(color) / 255.0)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /** Preto ou branco, o que tiver melhor contraste sobre a cor informada. */
    fun onColor(background: Int): Int =
        if (luminance(background) > 0.45) Color.BLACK else Color.WHITE

    /** Razão de contraste entre duas cores (1.0 a 21.0). */
    fun contrastRatio(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        val lighter = Math.max(la, lb)
        val darker = Math.min(la, lb)
        return (lighter + 0.05) / (darker + 0.05)
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
