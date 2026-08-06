package de.moqo.challenge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import de.moqo.challenge.R
import de.moqo.challenge.ui.theme.AppTheme

/** Primary plus secondary action pair, the standard bottom sheet footer. */
@Composable
fun SheetActionButtons(
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
    ) {
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.row_height_standard)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        ) {
            Text(primaryLabel, style = MaterialTheme.typography.labelLarge)
        }
        TextButton(
            onClick = onSecondary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = secondaryLabel,
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.textDark,
            )
        }
    }
}
