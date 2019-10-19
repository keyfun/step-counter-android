package keyfun.app.stepcounter.core

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesManager.instance.init(this)
    }
}
