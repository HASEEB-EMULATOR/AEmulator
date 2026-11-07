package app.aemu.core

import android.content.Context

/**
 * One-tap gaming setup: a small screen, no guest navigation bar, and the GPU
 * identity games expect from a Snapdragon phone.
 */
object Gaming {
    private const val PREFS = "aemu_gaming"

    fun preset(s: VmSettings): VmSettings = s.copy(
        width = 360, height = 800, density = 160,
        gpu = true, hwui = true, jit = true,
        ramMb = 2048, lowRam = false,
        fbHz = 60, touchHz = 60,
        showNavBar = false,
        keepScreenOn = true,
    )

    fun gpuEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("gpu", false)

    fun setGpu(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("gpu", on).apply()
    }

    /** what a Snapdragon 835 / Adreno 540 phone reports */
    fun gpuProps(): Map<String, String> = mapOf(
        "ro.hardware.egl" to "adreno",
        "ro.board.platform" to "msm8998",
        "ro.opengles.version" to "196610",
        "ro.hardware.vulkan" to "adreno",
    )

    fun apply(ctx: Context, id: String) {
        ImageStore.update(ctx, id) { img -> img.copy(settings = preset(img.settings)) }
    }
}
