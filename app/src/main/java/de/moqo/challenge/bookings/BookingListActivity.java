package de.moqo.challenge.bookings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.moqo.challenge.R;
import de.moqo.challenge.core.BaseActivity;
import de.moqo.challenge.databinding.ActivityBookingListBinding;
import de.moqo.challenge.sdk.BasicCallback;
import de.moqo.challenge.sdk.Booking;
import de.moqo.challenge.sdk.BookingStatus;
import de.moqo.challenge.sdk.Bookings;
import de.moqo.challenge.sdk.FleetApi;
import de.moqo.challenge.sdk.FleetError;

public class BookingListActivity extends BaseActivity implements BookingsAdapter.OnBookingClicked {

    private ActivityBookingListBinding binding;
    private BookingsAdapter adapter;
    private final List<Booking> allBookings = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_booking_list);

        adapter = new BookingsAdapter(this);
        binding.bookings.setLayoutManager(new LinearLayoutManager(this));
        binding.bookings.setAdapter(adapter);

        binding.filterChips.setOnCheckedStateChangeListener((group, checkedIds) ->
                applyFilter(checkedIds.contains(binding.chipAll.getId())));

        loadBookings();
    }

    private void loadBookings() {
        binding.progress.setVisibility(View.VISIBLE);
        FleetApi.with(this).bookings(this, new BasicCallback<Bookings>() {
            @Override
            public void onSuccess(@NonNull Bookings result) {
                binding.progress.setVisibility(View.GONE);
                allBookings.clear();
                allBookings.addAll(result.getItems());
                applyFilter(binding.chipAll.isChecked());
            }

            @Override
            public void onFailure(@NonNull FleetError error) {
                binding.progress.setVisibility(View.GONE);
                binding.footerHint.setText(getString(R.string.list_load_failed, error.getMessage()));
            }
        });
    }

    private void applyFilter(boolean showAll) {
        List<Booking> displayed = showAll
                ? allBookings
                : allBookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.ACTIVE
                                || b.getStatus() == BookingStatus.UPCOMING)
                        .collect(Collectors.toList());
        adapter.setBookings(displayed);
    }

    @Override
    public void onBookingClicked(Booking booking) {
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.getId());
        startActivity(intent);
    }
}
