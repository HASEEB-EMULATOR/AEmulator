package app.aemu.core

import android.content.Context

/**
 * Gaming mode: pick a phone to imitate, then the guest runs at that phone's
 * aspect ratio (so the picture fills the screen) and reports its identity.
 *
 * Hardware properties (ro.hardware.egl, ro.opengles.version, ro.kernel.qemu)
 * are deliberately left alone: the engine serves graphics with the phone's real
 * GPU, and lying about it makes SurfaceFlinger abort.
 */
object Gaming {
    private const val PREFS = "aemu_gaming"

    data class Device(
        val id: String,
        val label: String,
        val model: String,
        val brand: String,
        val manufacturer: String,
        val device: String,
        val fingerprint: String,
        val gpu: String,          // shown in the picker only
        val platform: String,
        val gles: String,
    )

    val DEVICES = listOf(
        Device("s25u", "Galaxy S25 Ultra", "SM-S938B", "samsung", "samsung", "e3q",
            "samsung/e3qxxx/e3q:15/AP3A.240905.015/S938BXXU1AXK1:user/release-keys",
            "adreno", "sun", "196610"),
        Device("s24u", "Galaxy S24 Ultra", "SM-S928B", "samsung", "samsung", "e3q",
            "samsung/e3qxxx/e3q:14/UP1A.231005.007/S928BXXS3AXK1:user/release-keys",
            "adreno", "pineapple", "196610"),
        Device("rm10", "RedMagic 10 Pro", "NX789J", "nubia", "ZTE", "NX789J",
            "nubia/NX789J/NX789J:15/AQ3A.240829.003/20241218:user/release-keys",
            "adreno", "sun", "196610"),
        Device("rm9", "RedMagic 9 Pro", "NX769J", "nubia", "ZTE", "NX769J",
            "nubia/NX769J/NX769J:14/UKQ1.231003.002/20240520:user/release-keys",
            "adreno", "pineapple", "196610"),
        Device("rog9", "ROG Phone 9", "ASUS_AI2501", "asus", "asus", "AI2501",
            "asus/WW_AI2501/AI2501:15/AQ3A.240829.003/20241210:user/release-keys",
            "adreno", "sun", "196610"),
        Device("rog8", "ROG Phone 8", "ASUS_AI2401", "asus", "asus", "AI2401",
            "asus/WW_AI2401/AI2401:14/UKQ1.231003.002/20240710:user/release-keys",
            "adreno", "pineapple", "196610"),
        Device("iqoo13", "iQOO 13", "V2408A", "vivo", "vivo", "V2408A",
            "vivo/V2408A/V2408A:15/AP3A.240905.015/20241220:user/release-keys",
            "adreno", "sun", "196610"),
        Device("op13", "OnePlus 13", "PJZ110", "OnePlus", "OnePlus", "PJZ110",
            "OnePlus/PJZ110/PJZ110:15/AP3A.240905.015/20241215:user/release-keys",
            "adreno", "sun", "196610"),
        Device("pocoF7", "Poco F7 Pro", "24117RK2C", "Xiaomi", "Xiaomi", "redwood",
            "Xiaomi/redwood/redwood:15/AQ3A.240829.003/OS2.0.10.0:user/release-keys",
            "adreno", "pineapple", "196610"),
        Device("x15", "Xiaomi 15", "24129PN74C", "Xiaomi", "Xiaomi", "dada",
            "Xiaomi/dada/dada:15/AQ3A.240829.003/OS2.0.12.0:user/release-keys",
            "adreno", "sun", "196610"),
    )

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun gpuEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean("gpu", false)

    fun setGpu(ctx: Context, on: Boolean) { prefs(ctx).edit().putBoolean("gpu", on).apply() }

    /** refresh rates the guest can be asked to run at */
    val FPS = listOf(30, 45, 60, 90, 120)

    fun withFps(s: VmSettings, fps: Int): VmSettings =
        s.copy(fbHz = fps, touchHz = fps)

    /** guest screen widths: 360 = fastest, 720 = as sharp as the phone itself */
    val QUALITY = listOf(360, 540, 720)

    /** identity only — never hardware or qemu properties */
    fun props(ctx: Context): Map<String, String> {
        val id = prefs(ctx).getString("device", null) ?: return emptyMap()
        val d = DEVICES.firstOrNull { it.id == id } ?: return emptyMap()
        return mapOf(
            "ro.product.model" to d.model,
            "ro.product.brand" to d.brand,
            "ro.product.manufacturer" to d.manufacturer,
            "ro.product.device" to d.device,
            "ro.product.name" to d.device,
            "ro.product.marketname" to d.label,
            "ro.build.fingerprint" to d.fingerprint,
            "ro.build.product" to d.device,
            "ro.build.device" to d.device,
            "ro.build.tags" to "release-keys",
            "ro.debuggable" to "0",
            "ro.secure" to "1",
        )
    }

    /** the guest runs at the host's aspect ratio, so the picture fills the screen */
    fun fit(ctx: Context, s: VmSettings, quality: Int = 360): VmSettings {
        val dm = ctx.resources.displayMetrics
        val w = quality
        val h = (w * dm.heightPixels.toFloat() / dm.widthPixels).toInt().coerceAtLeast(640)
        val dpi = (w * 160 / 360).coerceIn(120, 640)
        return s.copy(
            width = w, height = h, density = dpi,
            gpu = true, hwui = true, jit = true,
            ramMb = 2048, lowRam = false,
            fbHz = 60, touchHz = 60,
            showNavBar = false,
            keepScreenOn = true,
        )
    }

    fun apply(ctx: Context, imageId: String, device: Device, fps: Int = 60, quality: Int = 360) {
        prefs(ctx).edit().putString("device", device.id).putInt("fps", fps)
            .putInt("quality", quality).putBoolean("gpu", true).apply()
        ImageStore.update(ctx, imageId) { img ->
            img.copy(settings = withFps(fit(ctx, img.settings, quality), fps))
        }
    }

    /** settings screen: remember the phone and fit the default settings */
    fun applyDefaults(ctx: Context, s: VmSettings, device: Device, fps: Int = 60, quality: Int = 360): VmSettings {
        prefs(ctx).edit().putString("device", device.id).putInt("fps", fps)
            .putInt("quality", quality).putBoolean("gpu", true).apply()
        return withFps(fit(ctx, s, quality), fps)
    }
}
