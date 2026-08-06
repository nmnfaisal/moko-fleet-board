package de.moqo.challenge

import de.moqo.challenge.bookings.BookingFormatter
import de.moqo.challenge.sdk.Booking
import de.moqo.challenge.sdk.BookingStatus
import de.moqo.challenge.sdk.Provider
import de.moqo.challenge.sdk.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingFormatterTest {

    private fun booking(
        startsAt: String = "2026-08-03T09:00:00+02:00",
        endsAt: String = "2026-08-03T11:30:00+02:00",
        provider: Provider? = Provider("MOQO Aachen"),
    ) = Booking(
        id = "b-001",
        status = BookingStatus.ACTIVE,
        vehicle = Vehicle("VW ID. Buzz", "AC MQ 421"),
        provider = provider,
        startsAt = startsAt,
        endsAt = endsAt,
    )

    @Test
    fun `formats a same day time range`() {
        assertEquals("3 Aug, 09:00 to 11:30", BookingFormatter.timeRange(booking()))
    }

    @Test
    fun `keeps the start day when the booking runs past midnight`() {
        val range = BookingFormatter.timeRange(
            booking(startsAt = "2026-08-03T23:00:00+02:00", endsAt = "2026-08-04T01:30:00+02:00"),
        )
        assertEquals("3 Aug, 23:00 to 01:30", range)
    }
}
