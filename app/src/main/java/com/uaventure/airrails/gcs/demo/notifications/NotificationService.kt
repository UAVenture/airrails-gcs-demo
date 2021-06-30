package com.uaventure.airrails.gcs.demo.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import timber.log.Timber
import java.util.*

/** Service commands  */

class NotificationService: Service() {

    companion object {
        public const val MSG_RESERVED = 0
        public const val MSG_ALLOW_TAKEOFF = 1

        public const val MSG_EVENT_ON_GROUND = 100
        public const val MSG_EVENT_TAKEOFF = 101
        public const val MSG_EVENT_FLYING = 102
        public const val MSG_EVENT_LANDING = 103
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private lateinit var mMessenger: Messenger

    /**
     * Handler of incoming messages from clients.
     */
    internal class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext) : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {

                MSG_EVENT_ON_GROUND, MSG_EVENT_TAKEOFF, MSG_EVENT_FLYING, MSG_EVENT_LANDING -> {
                    setAction(msg.what)
                }

                MSG_ALLOW_TAKEOFF -> {
                    //
                    // uid - the vehicle UID.
                    // expire - the expiry time, in seconds.
                    //
                    if (msg.obj != null) {
                        val bundle: Bundle = msg.obj as Bundle

                        setTakeoffWindow(
                            bundle.getString("uid"),
                            bundle.getLong("expire"));
                    }
                }

                else -> {
                    Timber.e("Unknown what value: %d", msg.what)
                    super.handleMessage(msg)
                }
            }
        }

        private fun setTakeoffWindow(uid: String?, expire: Long) {
            showMsg(String.format(Locale.US,
                "Takeoff permission. UID: %s expires: %d", uid, expire))
        }

        private fun setAction(id: Int) {
            showMsg(String.format(Locale.US,"Action: %d", id))
        }

        private fun showMsg(msg: String) {
            Timber.i(msg)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }
}