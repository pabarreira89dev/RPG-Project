package pab.rpg.android.network

import android.content.Context

object AuthSessionManagerProvider {
    fun create(context: Context): AuthSessionManager = LocalAuthSessionManager()
}
