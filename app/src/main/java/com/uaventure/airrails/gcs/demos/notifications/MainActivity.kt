package com.uaventure.airrails.gcs.demos.notifications

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.uaventure.airrails.gcs.demos.R
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    companion object {
        /** Service commands  */
        @JvmStatic
        val MSG_ALLOW_TAKEOFF: Int = 1

        /**
         * Important: This will vary based on the GCS build.
         */
        @JvmStatic
        val PACKAGE_NAME: String = "com.uaventure.airrails.gcs.uaventure"

        @JvmStatic
        val SERVICE_NAME: String = "com.uaventure.airrails.gcs.services.NotificationService"
    }

    /** Messenger for communicating with the service.  */
    private var mService: Messenger? = null

    /** Flag indicating whether we have called bind on the service.  */
    private var bound: Boolean = false

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = Messenger(service)
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.fab)
        btn.setOnClickListener {
            sendTakeoffPermission("a7df876af86af8af", 5 * 60)
        }
    }

    private fun sendTakeoffPermission(uid: String, expireSecs: Long) {
        if (!bound) {
            bindService()
        }

        val bundle = Bundle()

        bundle.putString("uid", uid)
        bundle.putLong("expire", expireSecs)

        val msg: Message = Message.obtain(null, MSG_ALLOW_TAKEOFF, bundle)

        try {
            mService?.send(msg)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        bindService()
    }

    private fun bindService() {
        val serviceIntent = Intent()
        serviceIntent.setClassName(PACKAGE_NAME, SERVICE_NAME)

        if (!bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)) {
            Timber.e("NOT BOUND")
        } else {
            Timber.i("BOUND")
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (bound) {
            unbindService(mConnection)
            bound = false
        }
    }
}