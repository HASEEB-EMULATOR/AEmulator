package app.aemu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.aemu.core.Gaming

/**
 * Pick the phone the guest should pretend to be. Choosing one also fits the
 * guest screen to the host display, so nothing is letterboxed.
 */
@Composable
fun GamingDialog(onPick: (Gaming.Device) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gaming mode") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Pick a phone. The screen is fitted to yours, so it fills the display.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Gaming.DEVICES.forEach { d ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(d) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(d.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            d.model + " · " + d.gpu.uppercase() + " · " + d.platform,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
