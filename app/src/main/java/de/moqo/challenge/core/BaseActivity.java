package de.moqo.challenge.core;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.moqo.challenge.R;

/**
 * Shared activity plumbing, trimmed down from the production app.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    private Dialog globalProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
    }

    /**
     * Screens that draw behind the system bars return true and take care of insets themselves.
     */
    protected boolean isFullscreenContent() {
        return false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (!isFullscreenContent()) {
            applySystemBarPaddingToDecor();
        }
    }

    private void applySystemBarPaddingToDecor() {
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }

    /**
     * Reads insets off the root rather than the dispatch chain, so a sibling that consumes them
     * cannot break bottom-anchored UI.
     */
    protected void adjustBottomPadding(View view, int extraDp) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(getWindow().getDecorView());
        int bottom = insets == null
                ? 0
                : insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
        int extraPx = Math.round(extraDp * getResources().getDisplayMetrics().density);
        view.setPadding(
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                bottom + extraPx);
    }

    /**
     * Blocking, non-cancellable progress overlay. Dims nothing, so the screen still looks live while
     * it swallows every touch.
     */
    public void showGlobalProgress() {
        if (isFinishing() || globalProgress != null) {
            return;
        }
        Dialog dialog = new Dialog(this, R.style.GlobalProgress);
        dialog.setContentView(buildProgressView());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setDimAmount(0.0f);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        dialog.show();
        globalProgress = dialog;
    }

    private View buildProgressView() {
        int size = Math.round(48 * getResources().getDisplayMetrics().density);
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminateTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent)));

        FrameLayout root = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        root.addView(spinner, params);
        return root;
    }

    protected void setGlobalProgressCancelable(DialogInterface.OnCancelListener listener) {
        if (globalProgress != null) {
            globalProgress.setCancelable(true);
            globalProgress.setOnCancelListener(listener);
        }
    }

    public void dismissGlobalProgress() {
        if (globalProgress == null) {
            return;
        }
        globalProgress.dismiss();
        globalProgress = null;
    }

    @Override
    protected void onDestroy() {
        dismissGlobalProgress();
        super.onDestroy();
    }
}
