package ui.compose.city_creator.widgets.side_panel.buildings

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import model.FlyMap
import model.landscape.Building


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BuildingList(
    modifier: Modifier = Modifier,
    city: FlyMap,
    onFocusChange: (focused: Boolean, buildingId: Long) -> Unit,
    onBuildingChanged: (Building) -> Unit,
    onBuildingFinished: () -> Unit
) {

    LazyColumn(
        modifier = modifier.background(Color.White),
    ) {

        city.buildings.forEachIndexed { i, building ->
            item {

                BuildingItem(
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Enter) { event ->
                            onFocusChange(true, building.id)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            onFocusChange(false, building.id)
                        },
                    building = building,
                    onChanged = {
                        onBuildingChanged(it)
                    },
                    onFinished = { onBuildingFinished() }
                )
            }
        }

    }

}
