package de.moqo.challenge.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    @SerialName("active")
    ACTIVE,

    @SerialName("upcoming")
    UPCOMING,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("completed")
    COMPLETED,
}

@Serializable
data class Vehicle(
    @SerialName("name") val name: String,
    @SerialName("plate") val plate: String,
)

@Serializable
data class Provider(
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String? = null,
)

@Serializable
data class Booking(
    @SerialName("id") val id: String,
    @SerialName("status") val status: BookingStatus,
    @SerialName("vehicle") val vehicle: Vehicle,
    @SerialName("provider") val provider: Provider? = null,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("flag") val flag: String? = null,
    @SerialName("stalls_on_start") val stallsOnStart: Boolean = false,
)

@Serializable
data class Bookings(
    @SerialName("bookings") val items: List<Booking>,
)

data class TripTicket(
    val bookingId: String,
    val unlockCode: String,
)

data class FleetError(
    val code: Int,
    val message: String,
)
