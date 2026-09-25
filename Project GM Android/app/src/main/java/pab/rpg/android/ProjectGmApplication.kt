package pab.rpg.android

import android.app.Application
import pab.rpg.android.data.local.AppDatabase
import pab.rpg.android.network.ApiClient

class ProjectGmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        AppDatabase.init(this)
    }
}
