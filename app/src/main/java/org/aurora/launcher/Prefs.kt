package org.aurora.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferências do launcher, persistidas em SharedPreferences.
 * Todas as telas leem/gravam por aqui para manter consistência.
 */
object Prefs {

    private const val FILE = "aurora_prefs"

    // Tema
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    // Ordenação
    const val SORT_ASC = 0
    const val SORT_DESC = 1

    // Formato dos ícones
    const val SHAPE_ORIGINAL = 0
    const val SHAPE_ROUNDED = 1
    const val SHAPE_CIRCLE = 2

    // Estilo/filtro dos ícones
    const val STYLE_ORIGINAL = 0
    const val STYLE_MONO = 1
    const val STYLE_GRAY = 2

    // Fundo
    const val BG_THEME = 0
    const val BG_COLOR = 1
    const val BG_WALLPAPER = 2
    const val BG_BLUR_MAX = 25
    const val BG_BLUR_DEFAULT = 12
    const val BG_SCRIM_MAX = 80
    const val BG_SCRIM_DEFAULT = 40

    // Limites usados pela tela de configurações
    const val ICON_MIN = 28
    const val ICON_MAX = 64
    const val ICON_DEFAULT = 42
    const val ZOOM_MIN = 85
    const val ZOOM_MAX = 130
    const val ZOOM_DEFAULT = 100
    const val COLUMNS_MIN = 1
    const val COLUMNS_MAX = 5
    const val COLUMNS_DEFAULT = 1
    const val ROWS_MIN = 3
    const val ROWS_MAX = 8
    const val ROWS_DEFAULT = 6

    private fun sp(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun theme(ctx: Context) = sp(ctx).getInt("theme", THEME_SYSTEM)
    fun setTheme(ctx: Context, value: Int) = sp(ctx).edit().putInt("theme", value).apply()

    fun iconSize(ctx: Context) = sp(ctx).getInt("icon_size", ICON_DEFAULT)
    fun setIconSize(ctx: Context, value: Int) = sp(ctx).edit().putInt("icon_size", value).apply()

    fun zoom(ctx: Context) = sp(ctx).getInt("zoom", ZOOM_DEFAULT)
    fun setZoom(ctx: Context, value: Int) = sp(ctx).edit().putInt("zoom", value).apply()

    fun columns(ctx: Context) = sp(ctx).getInt("columns", COLUMNS_DEFAULT)
    fun setColumns(ctx: Context, value: Int) = sp(ctx).edit().putInt("columns", value).apply()

    fun rows(ctx: Context) = sp(ctx).getInt("rows", ROWS_DEFAULT)
    fun setRows(ctx: Context, value: Int) = sp(ctx).edit().putInt("rows", value).apply()

    fun shape(ctx: Context) = sp(ctx).getInt("shape", SHAPE_ORIGINAL)
    fun setShape(ctx: Context, value: Int) = sp(ctx).edit().putInt("shape", value).apply()

    fun style(ctx: Context) = sp(ctx).getInt("style", STYLE_ORIGINAL)
    fun setStyle(ctx: Context, value: Int) = sp(ctx).edit().putInt("style", value).apply()

    fun sort(ctx: Context) = sp(ctx).getInt("sort", SORT_ASC)
    fun setSort(ctx: Context, value: Int) = sp(ctx).edit().putInt("sort", value).apply()

    /** Cor de destaque (ARGB). 0 = usar a cor padrão do tema. */
    fun accent(ctx: Context) = sp(ctx).getInt("accent", 0)
    fun setAccent(ctx: Context, value: Int) = sp(ctx).edit().putInt("accent", value).apply()

    // ---- Fundo da tela ----

    fun bgMode(ctx: Context) = sp(ctx).getInt("bg_mode", BG_THEME)
    fun setBgMode(ctx: Context, value: Int) = sp(ctx).edit().putInt("bg_mode", value).apply()

    /** Cor sólida do fundo (ARGB). 0 = ainda não escolhida. */
    fun bgColor(ctx: Context) = sp(ctx).getInt("bg_color", 0)
    fun setBgColor(ctx: Context, value: Int) = sp(ctx).edit().putInt("bg_color", value).apply()

    fun bgBlur(ctx: Context) =
        sp(ctx).getInt("bg_blur", BG_BLUR_DEFAULT).coerceIn(0, BG_BLUR_MAX)
    fun setBgBlur(ctx: Context, value: Int) =
        sp(ctx).edit().putInt("bg_blur", value.coerceIn(0, BG_BLUR_MAX)).apply()

    /** Sobreposição sobre o papel de parede, em % (0–80). */
    fun bgScrim(ctx: Context) =
        sp(ctx).getInt("bg_scrim", BG_SCRIM_DEFAULT).coerceIn(0, BG_SCRIM_MAX)
    fun setBgScrim(ctx: Context, value: Int) =
        sp(ctx).edit().putInt("bg_scrim", value.coerceIn(0, BG_SCRIM_MAX)).apply()

    fun reset(ctx: Context) = sp(ctx).edit().clear().apply()
}
