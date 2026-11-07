package app.aemu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.aemu.core.Spoof

@Composable
fun SpoofDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val cur = remember { Spoof.map(ctx) }
    var model by remember { mutableStateOf(cur["ro.product.model"] ?: "") }
    var brand by remember { mutableStateOf(cur["ro.product.brand"] ?: "") }
    var fp by remember { mutableStateOf(cur["ro.build.fingerprint"] ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Device identity") },
        text = {
            Column {
                Text("What the system reports as its own phone. Restart the system after saving.",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp))
                Row {
                    TextButton(onClick = { Spoof.set(ctx, Spoof.preset("pixel7")); onDismiss() }) { Text("Pixel 7") }
                    TextButton(onClick = { Spoof.set(ctx, Spoof.preset("s23")); onDismiss() }) { Text("Galaxy S23") }
                    TextButton(onClick = { Spoof.clear(ctx); onDismiss() }) { Text("Off") }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = model, onValueChange = { model = it },
                    label = { Text("Model") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = brand, onValueChange = { brand = it },
                    label = { Text("Brand") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = fp, onValueChange = { fp = it },
                    label = { Text("Build fingerprint") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = Spoof.map(ctx).toMutableMap()
                m["ro.product.model"] = model
                m["ro.product.brand"] = brand
                m["ro.build.fingerprint"] = fp
                Spoof.set(ctx, m)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
