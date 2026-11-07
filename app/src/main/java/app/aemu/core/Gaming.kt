package app.aemu.core

import android.content.Context

/**
 * Gaming mode: pick a phone to imitate, then the guest runs at that phone's
 * aspect ratio (so the picture fills the screen) with its GPU identity.
 */
object Gaming {
    private const val PREFS = "aemu_gaming"

    /** a phone a game can believe in: identity + the GPU it would report */
    data class Device(
        val id: String,
        val label: String,
        val model: String,
        val brand: String,
        val manufacturer: String,
        val device: String,
        val fingerprint: String,
        val gpu: String,          // adreno | mali
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

    fun gpuEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("gpu", false)

    fun setGpu(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("gpu", on).apply()
    }

    /** properties the guest reports once a gaming device has been chosen */
    fun props(ctx: Context): Map<String, String> {
        val id = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("device", null)
            ?: return emptyMap()
        val d = DEVICES.firstOrNull { it.id == id } ?: return emptyMap()
        return mapOf(
            "ro.product.model" to d.model,
            "ro.product.brand" to d.brand,
            "ro.product.manufacturer" to d.manufacturer,
            "ro.product.device" to d.device,
            "ro.product.name" to d.device,
            "ro.build.fingerprint" to d.fingerprint,
            "ro.build.product" to d.device,
            "ro.build.device" to d.device,
            "ro.hardware.egl" to d.gpu,
            "ro.hardware.vulkan" to d.gpu,
            "ro.board.platform" to d.platform,
            "ro.opengles.version" to d.gles,
            "ro.kernel.qemu" to "0",
            "ro.hardware" to "qcom",
            "ro.boot.hardware" to "qcom",
            "ro.build.tags" to "release-keys",
            "ro.debuggable" to "0",
            "ro.secure" to "1",
        )
    }

    /** the guest runs at the host's aspect ratio, so the picture fills the screen */
    fun fit(ctx: Context, s: VmSettings): VmSettings {
        val dm = ctx.resources.displayMetrics
        val w = 360
        val h = (w * dm.heightPixels.toFloat() / dm.widthPixels).toInt().coerceAtLeast(640)
        return s.copy(
            width = w, height = h, density = 160,
            gpu = true, hwui = true, jit = true,
            ramMb = 2048, lowRam = false,
            fbHz = 60, touchHz = 60,
            showNavBar = false,
            keepScreenOn = true,
        )
    }


    /** settings screen: remember the phone and fit the default settings to this display */
    fun applyDefaults(ctx: Context, s: VmSettings, device: Device): VmSettings {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("device", device.id).putBoolean("gpu", true).apply()
        return fit(ctx, s)
    }

    /** apply a chosen device: remember it, fit the screen, restart needed afterwards */
    fun apply(ctx: Context, imageId: String, device: Device) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("device", device.id).putBoolean("gpu", true).apply()
        ImageStore.update(ctx, imageId) { img -> img.copy(settings = fit(ctx, img.settings)) }
    }
}
