package ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import model.FlyMap
import model.cargo.Cargo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CargosList(
    modifier: Modifier = Modifier,
    flyMap: FlyMap,
    onFocusChange: (focused: Boolean, cargo: Cargo) -> Unit,
    onCargoChanged: (Cargo) -> Unit,
) {

    LazyColumn(
        modifier = modifier.background(Color.White),
    ) {

        flyMap.cargos.forEachIndexed { i, cargo ->

            item {

                CargoItem(
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Enter) { event ->
                            onFocusChange(true, cargo)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            onFocusChange(false, cargo)
                        },
                    cargo = cargo,
                    onChanged = {
                        onCargoChanged(cargo)
                    },
                )

            }

        }

        item { Spacer(modifier = Modifier.height(70.dp)) }

    }

}