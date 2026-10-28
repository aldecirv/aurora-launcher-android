package org.aurora.launcher

import android.app.Activity
import android.app.WallpaperManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max

/**
 * Aplica o fundo escolhido (tema, cor sólida ou papel de parede desfocado).
 * O papel de parede é reduzido, desfocado em Kotlin puro e cacheado: nada de
 * RenderScript (obsoleto) e nenhuma permissão extra.
 */
object Background {

    private var cacheKey: String? = null
    private var cacheDrawable: Drawable? = null

    fun apply(activity: Activity) {
        val root = activity.findViewById<View>(R.id.root) ?: return

        when (Prefs.bgMode(activity)) {
            Prefs.BG_COLOR -> {
                // Sem cor escolhida ainda: usa o fundo do tema como base.
                val color = Prefs.bgColor(activity).takeIf { it != 0 }
                    ?: ContextCompat.getColor(activity, R.color.aurora_bg)
                root.background = ColorDrawable(color)
                setBars(activity, color)
            }

            Prefs.BG_WALLPAPER -> {
                val wallpaper = wallpaperDrawable(activity)
                if (wallpaper != null) {
                    // Papel de parede estático: usa a cópia desfocada.
                    root.background = wallpaper
                } else {
                    // Papel de parede animado: o sistema desenha atrás da janela
                    // translúcida; aplicamos só a sobreposição de legibilidade.
                    root.background = ColorDrawable(scrimColor(activity))
                }
                setBars(activity, Color.TRANSPARENT)
            }

            else -> {
                val color = ContextCompat.getColor(activity, R.color.aurora_bg)
                root.background = ColorDrawable(color)
                setBars(activity, color)
            }
        }
    }

    fun clear() {
        cacheKey = null
        cacheDrawable = null
    }

    @Suppress("DEPRECATION")
    private fun setBars(activity: Activity, color: Int) {
        activity.window.statusBarColor = color
        activity.window.navigationBarColor = color
    }

    private fun isNight(activity: Activity): Boolean =
        (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun wallpaperDrawable(activity: Activity): Drawable? {
        val dm = activity.resources.displayMetrics
        val blur = Prefs.bgBlur(activity)
        val scrim = Prefs.bgScrim(activity)
        val night = isNight(activity)
        val wallpaperId = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                WallpaperManager.getInstance(activity).getWallpaperId(WallpaperManager.FLAG_SYSTEM)
            } else 0
        } catch (_: Throwable) {
            0
        }
        val key = "w|${dm.widthPixels}x${dm.heightPixels}|$blur|$scrim|$night|$wallpaperId"

        if (cacheKey == key) cacheDrawable?.let { return it }

        val bitmap = loadWallpaper(activity, dm.widthPixels, dm.heightPixels) ?: return null
        val blurred = if (blur > 0) blur(bitmap, blur) else bitmap

        val overlay = scrimColor(activity)

        val drawable = LayerDrawable(
            arrayOf(
                BitmapDrawable(activity.resources, blurred),
                ColorDrawable(overlay)
            )
        )
        cacheKey = key
        cacheDrawable = drawable
        return drawable
    }

    /** Sobreposição que garante contraste do texto sobre o papel de parede. */
    private fun scrimColor(activity: Activity): Int {
        val alpha = (Prefs.bgScrim(activity).coerceIn(0, Prefs.BG_SCRIM_MAX) * 255) / 100
        return if (isNight(activity)) {
            Color.argb(alpha, 0, 0, 0)
        } else {
            Color.argb(alpha, 255, 255, 255)
        }
    }

    /** Lê o papel de parede do sistema e recorta no centro no tamanho da tela. */
    private fun loadWallpaper(activity: Activity, width: Int, height: Int): Bitmap? {
        val manager = WallpaperManager.getInstance(activity)
        val drawable = try {
            @Suppress("DEPRECATION")
            manager.drawable
        } catch (_: Throwable) {
            null
        } ?: return null

        val source: Bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            val rendered = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(Canvas(rendered))
            rendered
        }

        if (source.width <= 0 || source.height <= 0) return null

        val scale = max(width.toFloat() / source.width, height.toFloat() / source.height)
        val scaledW = (source.width * scale).toInt().coerceAtLeast(width)
        val scaledH = (source.height * scale).toInt().coerceAtLeast(height)
        val scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)
        val x = ((scaledW - width) / 2).coerceAtLeast(0)
        val y = ((scaledH - height) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(scaled, x, y, width, height)
        if (scaled !== cropped && scaled !== source) scaled.recycle()
        return cropped
    }

    /**
     * Desfoque por média de caixa (duas passadas) sobre uma versão reduzida
     * da imagem. Barato o bastante para rodar na thread de UI ao abrir o app.
     */
    private fun blur(source: Bitmap, radius: Int): Bitmap {
        val factor = 4
        val w = max(1, source.width / factor)
        val h = max(1, source.height / factor)
        val small = Bitmap.createScaledBitmap(source, w, h, true)

        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)

        val r = radius.coerceIn(1, 25)
        var buffer = boxBlur(pixels, w, h, r)
        buffer = boxBlur(buffer, w, h, r)

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(buffer, 0, w, 0, 0, w, h)
        val result = Bitmap.createScaledBitmap(out, source.width, source.height, true)

        if (small !== result) small.recycle()
        out.recycle()
        return result
    }

    private fun boxBlur(src: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val tmp = IntArray(w * h)
        val out = IntArray(w * h)
        val div = radius * 2 + 1

        // Horizontal
        for (y in 0 until h) {
            val row = y * w
            var r = 0
            var g = 0
            var b = 0
            for (i in -radius..radius) {
                val c = src[row + i.coerceIn(0, w - 1)]
                r += Color.red(c); g += Color.green(c); b += Color.blue(c)
            }
            for (x in 0 until w) {
                tmp[row + x] = Color.rgb(r / div, g / div, b / div)
                val add = src[row + (x + radius + 1).coerceAtMost(w - 1)]
                val rem = src[row + (x - radius).coerceAtLeast(0)]
                r += Color.red(add) - Color.red(rem)
                g += Color.green(add) - Color.green(rem)
                b += Color.blue(add) - Color.blue(rem)
            }
        }

        // Vertical
        for (x in 0 until w) {
            var r = 0
            var g = 0
            var b = 0
            for (i in -radius..radius) {
                val c = tmp[i.coerceIn(0, h - 1) * w + x]
                r += Color.red(c); g += Color.green(c); b += Color.blue(c)
            }
            for (y in 0 until h) {
                out[y * w + x] = Color.rgb(r / div, g / div, b / div)
                val add = tmp[(y + radius + 1).coerceAtMost(h - 1) * w + x]
                val rem = tmp[(y - radius).coerceAtLeast(0) * w + x]
                r += Color.red(add) - Color.red(rem)
                g += Color.green(add) - Color.green(rem)
                b += Color.blue(add) - Color.blue(rem)
            }
        }
        return out
    }
}
