package app.aemu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.aemu.core.GuestVm

private const val SWITCH = "/data/local/tmp/aemu-su-on"

/**
 * One switch: on = every app in this system gets root, off = none does.
 * The guest's /system/xbin/su (installed at import) looks for this file before it hands out root.
 */
@Composable
fun RootAccessDialog(vm: GuestVm, onDismiss: () -> Unit) {
    var on by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val out = runCatching { vm.guestShell("test -f $SWITCH && echo 1 || echo 0", 15_000) }.getOrDefault("0")
        on = out.trim().contains("1")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Root access") },
        text = {
            Column {
                Text(
                    "On: every app in this system gets root. Off: no app does.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = on == true, onCheckedChange = { on = it }, enabled = on != null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        when (on) {
                            null -> "Reading…"
                            true -> "Root ON"
                            false -> "Root OFF"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val want = on == true
                vm.guestShell(
                    if (want) "touch $SWITCH; chmod 644 $SWITCH" else "rm -f $SWITCH",
                    15_000
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
