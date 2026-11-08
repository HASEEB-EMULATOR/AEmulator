package app.aemu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.aemu.core.Gaming

/**
 * Pick the phone the guest should pretend to be and the refresh rate it runs at.
 * Choosing one fits the guest screen to this display, so nothing is letterboxed.
 */
@Composable
fun GamingDialog(onPick: (Gaming.Device, Int, Int) -> Unit, onDismiss: () -> Unit) {
    var fps by remember { mutableIntStateOf(60) }
    var quality by remember { mutableIntStateOf(360) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gaming mode") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Pick a phone. The screen is fitted to yours and your phone's own GPU is used, " +
                        "so it works on every device.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Text("Quality (screen width)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Gaming.QUALITY.forEach { v ->
                        FilterChip(selected = quality == v, onClick = { quality = v },
                            label = { Text(if (v == 720) "720 sharp" else "$v") })
                    }
                }

                Text("Refresh rate", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Gaming.FPS.forEach { v ->
                        FilterChip(selected = fps == v, onClick = { fps = v }, label = { Text("$v") })
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                Gaming.DEVICES.forEach { d ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(d, fps, quality) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(d.label, style = MaterialTheme.typography.titleMedium)
                        Text(d.model + " · " + d.fingerprint.take(28) + "…",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
