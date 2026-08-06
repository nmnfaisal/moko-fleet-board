package de.moqo.challenge.feature.cancellation

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import de.moqo.challenge.R
import de.moqo.challenge.core.ui.ComposeBottomSheetDialogFragment
import de.moqo.challenge.ui.components.SheetActionButtons
import de.moqo.challenge.ui.theme.AppTheme

/**
 * The one screen that has already been migrated to Compose. New UI is meant to look like this.
 */
class CancelBookingSheet : ComposeBottomSheetDialogFragment() {

    private val vehicleName: String
        get() = arguments?.getString(ARG_VEHICLE_NAME).orEmpty()

    @Composable
    override fun SheetContent() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(dimensionResource(R.dimen.sheet_content_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        ) {
            Text(
                text = stringResource(R.string.cancel_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.cancel_sheet_body, vehicleName),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textMuted,
            )
            SheetActionButtons(
                primaryLabel = stringResource(R.string.cancel_sheet_confirm),
                secondaryLabel = stringResource(R.string.cancel_sheet_keep),
                onPrimary = {
                    Toast.makeText(requireContext(), R.string.cancel_sheet_done, Toast.LENGTH_SHORT).show()
                    dismiss()
                },
                onSecondary = { dismiss() },
            )
        }
    }

    companion object {
        private const val ARG_VEHICLE_NAME = "vehicle_name"

        @JvmStatic
        fun newInstance(vehicleName: String): CancelBookingSheet = CancelBookingSheet().apply {
            arguments = Bundle().apply { putString(ARG_VEHICLE_NAME, vehicleName) }
        }
    }
}
