package de.moqo.challenge.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.moqo.challenge.R
import de.moqo.challenge.ui.theme.AppTheme

/**
 * Base class for Compose bottom sheets. Handles the ComposeView, the rounded background, the theme
 * wrapper and the expanded, non-draggable behaviour, so a sheet only has to provide its content.
 */
abstract class ComposeBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Composable
    abstract fun SheetContent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setBackgroundResource(R.drawable.bg_bottom_sheet_rounded_top)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            AppTheme {
                SheetContent()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isDraggable = false
        }
    }
}
