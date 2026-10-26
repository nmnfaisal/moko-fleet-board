package de.moqo.challenge.bookings

import android.content.Context
import de.moqo.challenge.R
import de.moqo.challenge.sdk.Booking
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object BookingFormatter {

    private val dayFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    @JvmStatic
    fun timeRange(booking: Booking): String {
        val start = OffsetDateTime.parse(booking.startsAt)
        val end = OffsetDateTime.parse(booking.endsAt)
        return "${dayFormat.format(start)}, ${timeFormat.format(start)} to ${timeFormat.format(end)}"
    }

    @JvmStatic
    fun flagLabel(context: Context, flag: String): String = when (flag) {
        "needs_cleaning" -> context.getString(R.string.flag_needs_cleaning)
        "charging" -> context.getString(R.string.flag_charging)
        "damage_reported" -> context.getString(R.string.flag_damage_reported)
        else -> flag
    }

    @JvmStatic
    fun providerLine(context: Context, booking: Booking): String =
        booking.provider?.let { context.getString(R.string.detail_provided_by, it.name) } ?: ""
}
