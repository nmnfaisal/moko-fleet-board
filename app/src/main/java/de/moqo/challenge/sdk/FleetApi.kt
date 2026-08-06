package de.moqo.challenge.sdk

import android.content.Context
import java.util.concurrent.Executors
import kotlinx.serialization.json.Json

/**
 * Stand-in for the real MOQO mobile SDK. Same shape as production: a singleton, callback based,
 * results marshalled to the main thread by [CallbackWrapper]. Data comes from a bundled asset with an
 * artificial delay, so the challenge works offline.
 */
class FleetApi private constructor(private val appContext: Context) {

    private val io = Executors.newSingleThreadExecutor()
    private val json = Json { ignoreUnknownKeys = true }

    fun bookings(context: Context, callback: BasicCallback<Bookings>) {
        val wrapper = CallbackWrapper(context, callback)
        io.execute {
            Thread.sleep(NETWORK_DELAY_MILLIS)
            runCatching { Bookings(loadBookings()) }
                .onSuccess { wrapper.success(it) }
                .onFailure { wrapper.failure(FleetError(500, it.message ?: "unknown")) }
        }
    }

    fun booking(context: Context, bookingId: String, callback: BasicCallback<Booking>) {
        val wrapper = CallbackWrapper(context, callback)
        io.execute {
            Thread.sleep(NETWORK_DELAY_MILLIS)
            val booking = runCatching { loadBookings().firstOrNull { it.id == bookingId } }.getOrNull()
            if (booking == null) {
                wrapper.failure(FleetError(404, "booking not found"))
            } else {
                wrapper.success(booking)
            }
        }
    }

    fun startTrip(context: Context, bookingId: String, callback: BasicCallback<TripTicket>) {
        val wrapper = CallbackWrapper(context, callback)
        io.execute {
            val booking = runCatching { loadBookings().firstOrNull { it.id == bookingId } }.getOrNull()
            if (booking != null && booking.stallsOnStart) {
                // Upstream occasionally accepts the request and never answers. The SDK has no
                // timeout of its own, so nothing further happens on this booking.
                return@execute
            }
            Thread.sleep(NETWORK_DELAY_MILLIS)
            if (booking == null) {
                wrapper.failure(FleetError(404, "booking not found"))
            } else {
                wrapper.success(TripTicket(bookingId, unlockCodeFor(bookingId)))
            }
        }
    }

    private fun loadBookings(): List<Booking> =
        appContext.assets.open(ASSET).bufferedReader().use {
            json.decodeFromString<Bookings>(it.readText()).items
        }

    private fun unlockCodeFor(bookingId: String): String =
        "MQ-" + (bookingId.hashCode().toString().takeLast(4).padStart(4, '0'))

    companion object {
        private const val ASSET = "bookings.json"
        private const val NETWORK_DELAY_MILLIS = 700L

        @Volatile
        private var instance: FleetApi? = null

        @JvmStatic
        fun with(context: Context): FleetApi =
            instance ?: synchronized(this) {
                instance ?: FleetApi(context.applicationContext).also { instance = it }
            }
    }
}
