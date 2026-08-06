package de.moqo.challenge.sdk

import android.content.Context
import android.os.Handler
import android.util.Log
import java.lang.ref.WeakReference

abstract class BasicCallback<T> {

    abstract fun onSuccess(result: T)

    open fun onFailure(error: FleetError) = Unit
}

/**
 * Marshals SDK results back onto the main thread, so a callback can touch UI directly.
 *
 * Holds the caller's context weakly. If it has been collected by the time the response arrives the
 * callback is dropped, which is why callers that may outlive their screen should hand in the
 * application context.
 */
internal class CallbackWrapper<T>(context: Context, private val delegate: BasicCallback<T>) {

    private val contextRef = WeakReference(context)

    fun success(result: T) = runOnMainThread { delegate.onSuccess(result) }

    fun failure(error: FleetError) = runOnMainThread { delegate.onFailure(error) }

    private fun runOnMainThread(block: () -> Unit) {
        val context = contextRef.get()
        if (context == null) {
            Log.w(TAG, "Lost context, skip callback")
            return
        }
        Handler(context.mainLooper).post(block)
    }

    private companion object {
        const val TAG = "CallbackWrapper"
    }
}
