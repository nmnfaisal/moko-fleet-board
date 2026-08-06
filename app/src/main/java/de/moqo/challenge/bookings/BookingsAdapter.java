package de.moqo.challenge.bookings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.moqo.challenge.R;
import de.moqo.challenge.databinding.ItemBookingBinding;
import de.moqo.challenge.sdk.Booking;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    public interface OnBookingClicked {
        void onBookingClicked(Booking booking);
    }

    private final List<BookingItemViewModel> items = new ArrayList<>();
    private final OnBookingClicked listener;

    public BookingsAdapter(OnBookingClicked listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        items.clear();
        for (Booking booking : bookings) {
            items.add(new BookingItemViewModel(booking));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.item_booking, parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItemViewModel item = items.get(position);
        item.setDataBinding(holder.binding);
        holder.binding.getRoot().setOnClickListener(view -> listener.onBookingClicked(item.getBooking()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {

        final ItemBookingBinding binding;

        BookingViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
