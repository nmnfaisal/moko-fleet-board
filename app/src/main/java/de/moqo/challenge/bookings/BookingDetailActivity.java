package de.moqo.challenge.bookings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import de.moqo.challenge.R;
import de.moqo.challenge.core.BaseActivity;
import de.moqo.challenge.databinding.ActivityBookingDetailBinding;
import de.moqo.challenge.feature.cancellation.CancelBookingSheet;
import de.moqo.challenge.sdk.BasicCallback;
import de.moqo.challenge.sdk.Booking;
import de.moqo.challenge.sdk.FleetApi;
import de.moqo.challenge.sdk.FleetError;
import de.moqo.challenge.sdk.TripTicket;

public class BookingDetailActivity extends BaseActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";

    private ActivityBookingDetailBinding binding;
    @Nullable
    private Booking booking;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable startTripTimeout;

    @Override
    protected boolean isFullscreenContent() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_booking_detail);

        binding.startTrip.setOnClickListener(view -> startTrip());
        binding.cancelBooking.setOnClickListener(view -> showCancelSheet());
        binding.close.setOnClickListener(view -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(binding.actions, (view, insets) -> {
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    navBottom);
            return insets;
        });

        String bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        loadBooking(bookingId);
    }

    private void loadBooking(String bookingId) {
        showGlobalProgress();
        FleetApi.with(this).booking(this, bookingId, new BasicCallback<Booking>() {
            @Override
            public void onSuccess(@NonNull Booking result) {
                dismissGlobalProgress();
                booking = result;
                render(result);
            }

            @Override
            public void onFailure(@NonNull FleetError error) {
                dismissGlobalProgress();
                Toast.makeText(BookingDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void render(Booking booking) {
        binding.vehicleName.setText(booking.getVehicle().getName());
        binding.plate.setText(booking.getVehicle().getPlate());
        binding.timeRange.setText(BookingFormatter.timeRange(booking));
        String providerText = BookingFormatter.providerLine(this, booking);
        binding.provider.setText(providerText);
        binding.provider.setVisibility(providerText.isEmpty() ? View.GONE : View.VISIBLE);
        binding.startTrip.setEnabled(true);
    }

    private void startTrip() {
        if (booking == null) {
            return;
        }
        binding.startTrip.setEnabled(false);
        showGlobalProgress();
        setGlobalProgressCancelable(d -> cancelStartTrip());
        startTripTimeout = () -> {
            cancelStartTrip();
            Toast.makeText(this, R.string.detail_start_trip_timeout, Toast.LENGTH_LONG).show();
        };
        handler.postDelayed(startTripTimeout, 10_000);
        FleetApi.with(this).startTrip(this, booking.getId(), new BasicCallback<TripTicket>() {
            @Override
            public void onSuccess(@NonNull TripTicket result) {
                handler.removeCallbacks(startTripTimeout);
                startTripTimeout = null;
                dismissGlobalProgress();
                binding.unlockCode.setText(getString(R.string.detail_unlock_code, result.getUnlockCode()));
                binding.unlockCode.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull FleetError error) {
                cancelStartTrip();
                Toast.makeText(BookingDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cancelStartTrip() {
        handler.removeCallbacks(startTripTimeout);
        startTripTimeout = null;
        dismissGlobalProgress();
        binding.startTrip.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void showCancelSheet() {
        if (booking == null) {
            return;
        }
        CancelBookingSheet.newInstance(booking.getVehicle().getName())
                .show(getSupportFragmentManager(), "cancel_booking");
    }
}
