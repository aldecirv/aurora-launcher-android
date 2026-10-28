package org.aurora.launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.content.ContextCompat

/**
 * Desenha o ícone do app em um Bitmap aplicando:
 *  - tamanho em pixels
 *  - estilo (original / monocromático / escala de cinza)
 *  - formato (original / arredondado / círculo)
 *
 * O resultado é cacheado por pacote+tamanho+estilo+formato, evitando
 * rasterizar o mesmo ícone a cada rolagem.
 */
object IconLoader {

    private data class Key(
        val packageName: String,
        val size: Int,
        val shape: Int,
        val style: Int
    )

    private val cache = LruCache<Key, Bitmap>(240)

    fun load(context: Context, app: AppInfo, sizePx: Int, shape: Int, style: Int): Bitmap {
        val key = Key(app.packageName, sizePx, shape, style)
        cache.get(key)?.let { return it }

        val bitmap = render(context, app.icon, sizePx, shape, style)
        cache.put(key, bitmap)
        return bitmap
    }

    fun clear() = cache.evictAll()

    private fun render(
        context: Context,
        drawable: Drawable,
        size: Int,
        shape: Int,
        style: Int
    ): Bitmap {
        val raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val rawCanvas = Canvas(raw)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(rawCanvas)

        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val outCanvas = Canvas(out)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (style) {
            Prefs.STYLE_MONO -> paint.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(context, R.color.aurora_accent),
                PorterDuff.Mode.SRC_IN
            )
            Prefs.STYLE_GRAY -> paint.colorFilter =
                ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        outCanvas.drawBitmap(raw, 0f, 0f, paint)

        if (shape != Prefs.SHAPE_ORIGINAL) {
            val mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(mask)
            val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            when (shape) {
                Prefs.SHAPE_CIRCLE ->
                    maskCanvas.drawCircle(size / 2f, size / 2f, size / 2f, maskPaint)
                Prefs.SHAPE_ROUNDED -> {
                    val radius = size * 0.22f
                    maskCanvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), radius, radius, maskPaint)
                }
            }
            val cut = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            outCanvas.drawBitmap(mask, 0f, 0f, cut)
            mask.recycle()
        }

        raw.recycle()
        return out
    }
}
