package ui.compose.city_creator.widgets.side_panel.delivery_panel.drones

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.drone.Drone


@Composable
fun CreateDroneDialog(
    newId: Long,
    onConfirm: (Drone) -> Unit,
    onDismiss: () -> Unit,
    startPoint: DroneStartPoint.StartPointSelected
) {
    var batteryLevel by remember { mutableStateOf(100.0) }
    var maxCargoCapacity by remember { mutableStateOf("") }
    var cargoError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать БПЛА") },
        text = {
            Column {
                Text("Уровень заряда: ${batteryLevel.toInt()}%")
                Slider(
                    value = batteryLevel.toFloat(),
                    onValueChange = { batteryLevel = it.toDouble() },
                    valueRange = 0f..100f,
                    steps = 100,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = Color(0, 0, 0, 0x0000009b),
                        inactiveTrackColor = Color(0, 0, 0, 0x00000044)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = maxCargoCapacity,
                    onValueChange = {
                        maxCargoCapacity = it.filter { ch -> ch.isDigit() || ch == '.' }
                        cargoError = maxCargoCapacity.toDoubleOrNull()?.let { value -> value <= 0f } ?: true
                    },
                    label = { Text("Макс. грузоподъемность (кг)") },
                    isError = cargoError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
//                        unfocusedBorderColor = Color.Black,
                        textColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
//                        unfocusedLabelColor = Color.Black
                    )
                )
                if (cargoError) {
                    Text(
                        "Введите положительное целое число",
                        color = MaterialTheme.colors.error,
                        fontSize = MaterialTheme.typography.caption.fontSize
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cargo = maxCargoCapacity.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        Drone(
                            id = newId,
                            batteryLevel = batteryLevel,
                            maxCargoCapacityMass = cargo,
                            currentPosition = startPoint.start
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White,
                    backgroundColor = if (!cargoError && maxCargoCapacity.isNotBlank()) Color.Black else Color.Transparent
                ),
                enabled = !cargoError && maxCargoCapacity.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Text("Отмена")
            }
        }
    )
}