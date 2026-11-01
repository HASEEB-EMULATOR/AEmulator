package com.haseeb.emulator

import android.app.Application
import com.haseeb.emulator.core.Keeper

class AemuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Keeper.ensureChannel(this)
    }
}
