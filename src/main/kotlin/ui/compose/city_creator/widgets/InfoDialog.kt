package ui.compose.city_creator.widgets

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun InfoDialog(
    message: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,           // закрытие кликом вне окна / Esc
        title = { Text("Информация") },
        text  = { Text(message) },
        confirmButton = {
            TextButton(onClick = onClose) { Text("OK") }
        }
    )
}