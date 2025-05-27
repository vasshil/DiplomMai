package ui.compose.city_creator.widgets.side_panel.landscape

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
import model.landscape.NoFlyZone
import ui.compose.city_creator.widgets.side_panel.landscape.building.BuildingItem
import ui.compose.city_creator.widgets.side_panel.landscape.nfz.NoFlyZoneItem


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LandscapeList(
    modifier: Modifier = Modifier,
    flyMap: FlyMap,
    onBuildingFocusChange: (focused: Boolean, buildingId: Long) -> Unit,
    onNFZFocusChange: (focused: Boolean, nfzId: Long) -> Unit,
    onBuildingChanged: (Building) -> Unit,
    onBuildingFinished: () -> Unit,
    onNFZChanged: (NoFlyZone) -> Unit,
    onNFZFinished: () -> Unit
) {

    LazyColumn(
        modifier = modifier.background(Color.White),
    ) {

        flyMap.buildings.forEachIndexed { i, building ->
            item {

                BuildingItem(
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Enter) { event ->
                            onBuildingFocusChange(true, building.id)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            onBuildingFocusChange(false, building.id)
                        },
                    building = building,
                    onChanged = {
                        onBuildingChanged(it)
                    },
                    onFinished = { onBuildingFinished() }
                )
            }
        }

        flyMap.noFlyZones.forEachIndexed { i, nfz ->

            item {

                NoFlyZoneItem(
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Enter) { event ->
                            onNFZFocusChange(true, nfz.id)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            onNFZFocusChange(false, nfz.id)
                        },
                    nfz = nfz,
                    onChanged = {
                        onNFZChanged(it)
                    },
                    onFinished = { onNFZFinished() }
                )
            }

        }

    }

}
