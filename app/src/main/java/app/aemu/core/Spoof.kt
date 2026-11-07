package app.aemu.core

import android.content.Context

/**
 * Device identity written into the guest's property area at every boot.
 * An empty map leaves the firmware's own values untouched.
 */
object Spoof {
    private const val FILE = "aemu_spoof"

    val KEYS = listOf(
        "ro.product.model", "ro.product.brand", "ro.product.manufacturer",
        "ro.product.device", "ro.product.name", "ro.product.marketname",
        "ro.build.fingerprint", "ro.build.product", "ro.build.device",
        "ro.build.tags", "ro.debuggable", "ro.secure"
    )

    fun map(ctx: Context): Map<String, String> {
        val sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val out = LinkedHashMap<String, String>()
        for (k in KEYS) sp.getString(k, null)?.takeIf { it.isNotBlank() }?.let { out[k] = it }
        return out
    }

    fun set(ctx: Context, values: Map<String, String>) {
        val e = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        for (k in KEYS) {
            val v = values[k]?.trim()
            if (v.isNullOrEmpty()) e.remove(k) else e.putString(k, v)
        }
        e.apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /** ready-made identities: one tap and the guest stops looking like this phone */
    fun preset(name: String): Map<String, String> = when (name) {
        "pixel7" -> mapOf(
            "ro.product.model" to "Pixel 7",
            "ro.product.brand" to "google",
            "ro.product.manufacturer" to "Google",
            "ro.product.device" to "panther",
            "ro.product.name" to "panther",
            "ro.product.marketname" to "Pixel 7",
            "ro.build.fingerprint" to "google/panther/panther:14/UQ1A.240205.004/11269751:user/release-keys",
            "ro.build.product" to "panther",
            "ro.build.device" to "panther",
            "ro.build.tags" to "release-keys",
            "ro.debuggable" to "0",
            "ro.secure" to "1"
        )
        "s23" -> mapOf(
            "ro.product.model" to "SM-S911B",
            "ro.product.brand" to "samsung",
            "ro.product.manufacturer" to "samsung",
            "ro.product.device" to "dm1q",
            "ro.product.name" to "dm1qxx",
            "ro.product.marketname" to "Galaxy S23",
            "ro.build.fingerprint" to "samsung/dm1qxx/dm1q:14/UP1A.231005.007/S911BXXS3AWLM:user/release-keys",
            "ro.build.product" to "dm1q",
            "ro.build.device" to "dm1q",
            "ro.build.tags" to "release-keys",
            "ro.debuggable" to "0",
            "ro.secure" to "1"
        )
        else -> emptyMap()
    }
}
