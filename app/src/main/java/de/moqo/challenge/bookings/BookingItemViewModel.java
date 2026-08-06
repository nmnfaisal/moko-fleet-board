package de.moqo.challenge.bookings;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.core.content.ContextCompat;

import de.moqo.challenge.R;
import de.moqo.challenge.databinding.ItemBookingBinding;
import de.moqo.challenge.sdk.Booking;
import de.moqo.challenge.sdk.BookingStatus;

/**
 * Row presenter for the bookings list. Mirrors the item view model pattern used across the
 * production app: the view model holds the model and writes it into the binding.
 */
public class BookingItemViewModel {

    private final Booking booking;

    public BookingItemViewModel(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setDataBinding(ItemBookingBinding binding) {
        binding.title.setText(booking.getVehicle().getName());
        binding.subtitle.setText(BookingFormatter.timeRange(booking));
        binding.plate.setText(booking.getVehicle().getPlate());

        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.ACTIVE || status == BookingStatus.UPCOMING) {
            int color = status == BookingStatus.ACTIVE ? R.color.status_active : R.color.status_upcoming;
            binding.statusDot.setImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(binding.getRoot().getContext(), color)));
        }

        String flag = booking.getFlag();
        if (flag != null) {
            binding.flagChip.setText(BookingFormatter.flagLabel(binding.getRoot().getContext(), flag));
            binding.flagChip.setVisibility(View.VISIBLE);
        }
    }
}
