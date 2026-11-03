package app.aemu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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

private const val ALLOW = "/data/local/tmp/aemu-su-allow"

private data class RootApp(val pkg: String, val uid: Int)

@Composable
fun RootAccessDialog(vm: GuestVm, onDismiss: () -> Unit) {
    var apps by remember { mutableStateOf<List<RootApp>>(emptyList()) }
    var allowed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val out = runCatching { vm.guestShell("pm list packages -3 -U 2>/dev/null", 40_000) }.getOrDefault("")
        val re = Regex("package:(\\S+)\\s+uid:(\\d+)")
        apps = out.lineSequence().mapNotNull { line ->
            val m = re.find(line) ?: return@mapNotNull null
            RootApp(m.groupValues[1], m.groupValues[2].toIntOrNull() ?: return@mapNotNull null)
        }.sortedBy { it.pkg }.toList()
        val cur = runCatching { vm.guestShell("cat $ALLOW 2>/dev/null", 10_000) }.getOrDefault("")
        allowed = cur.lineSequence().mapNotNull { it.trim().toIntOrNull() }.toSet()
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Root access") },
        text = {
            if (loading) {
                Text("Reading the app list from the system…")
            } else {
                Column(Modifier.heightIn(max = 420.dp)) {
                    Text("Tick the apps that may use su.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    if (apps.isEmpty()) {
                        Text("No user apps installed.", style = MaterialTheme.typography.bodyMedium)
                    }
                    LazyColumn {
                        items(apps) { a ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { allowed = if (a.uid in allowed) allowed - a.uid else allowed + a.uid }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = a.uid in allowed,
                                    onCheckedChange = { on -> allowed = if (on) allowed + a.uid else allowed - a.uid }
                                )
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(a.pkg, style = MaterialTheme.typography.bodyMedium)
                                    Text("uid ${a.uid}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val body = allowed.joinToString("\n")
                vm.guestShell("mkdir -p /data/local/tmp; printf '%s\\n' '" + body + "' > " + ALLOW + "; chmod 644 " + ALLOW, 20_000)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
