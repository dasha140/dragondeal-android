package com.elvsf.dragondeal

import android.app.Application
import com.elvsf.dragondeal.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DragonDealApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@DragonDealApp)
            modules(appModule)
        }
    }
}
