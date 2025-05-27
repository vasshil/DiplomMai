package ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jme3.math.Vector3f
import model.cargo.Cargo
import model.drone.Drone


@Composable
fun CreateCargoDialog(
    onConfirm: (Cargo) -> Unit,
    onDismiss: () -> Unit,
    cargoPoints: CargoPoints.TwoPointSelected
) {
    println("cargoPoints ${cargoPoints.start} ${cargoPoints.destination}")
    var weight by remember { mutableStateOf("") }
//    var maxCargoCapacity by remember { mutableStateOf("") }
    var cargoError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать Груз") },
        text = {
            Column(
                modifier = Modifier//.padding(top = 50.dp)
            ) {
                Box(Modifier.height(20.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it.filter { ch -> ch.isDigit() || ch == '.' }
                        cargoError = weight.toFloatOrNull()?.let { value -> value <= 0f } ?: true
                    },
                    label = { Text("Масса (кг)") },
                    isError = cargoError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
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
                    val w = weight.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        Cargo(
                            timeCreation = System.currentTimeMillis(),
                            weight = w,
                            startVertex = cargoPoints.start,
                            destination = cargoPoints.destination,
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White,
                    backgroundColor = if (!cargoError && weight.isNotBlank()) Color.Black else Color.Transparent
                ),
                enabled = !cargoError && weight.isNotBlank()
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