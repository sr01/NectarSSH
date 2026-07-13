package com.rosi.nectarssh

import android.app.Application
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class NectarApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize BouncyCastle security provider for SSH cryptography.
        // Done here so it runs on every process start, regardless of which
        // component (MainActivity, shortcut launch, or service) starts the app.
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
}
