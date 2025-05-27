package ui.compose.city_creator.widgets.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.compose.city_creator.Scheme2DMode
import ui.compose.common.*


@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    mousePosition: Offset,
    simulationMode: SimulationMode,
    editorMode: CreatorModeEnum,
    saveCity: () -> Unit,
    loadCity: () -> Unit,
    onSimulationModeChange: (SimulationMode) -> Unit,
    onEditorModeChange: (CreatorModeEnum) -> Unit,
    schemeMode: Scheme2DMode,
    onSchemeModeChange: (Scheme2DMode) -> Unit,
) {

    Row(
        modifier = modifier.background(TOP_BAR_BG_COLOR)
    ) {

        if (schemeMode == Scheme2DMode.EDITOR) {

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CreatorModeEnum.ADD_BUILDING,
                selectedMode = editorMode,
                icon = Icons.Filled.AddHome,
                tint = FOCUSED_BUILDING_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CreatorModeEnum.ADD_CHARGE_STATION,
                selectedMode = editorMode,
                icon = Icons.Filled.Bolt,
                tint = CHARGE_STATION_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CreatorModeEnum.ADD_NO_FLY_ZONE,
                selectedMode = editorMode,
                icon = Icons.Filled.Block,
                tint = NO_FLY_ZONE_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CreatorModeEnum.REMOVE,
                selectedMode = editorMode,
                icon = Icons.Filled.Delete,
                onEditorModeChange = onEditorModeChange
            )

        }

        ActionButton(
            icon = Icons.Filled.Save,
        ) {
            saveCity()
        }

        ActionButton(
            icon = Icons.Filled.FileUpload,
        ) {
            loadCity()
        }

        Box(Modifier.weight(1f).align(Alignment.CenterVertically).padding(end = 16.dp)) {

            if (schemeMode != Scheme2DMode.EDITOR) {

                Button(
                    modifier = Modifier.size(width = 56.dp, height = 36.dp).align(Alignment.CenterEnd),
                    onClick = {
                        onSimulationModeChange(if (simulationMode == SimulationMode.PAUSE) SimulationMode.PLAY else SimulationMode.PAUSE)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = SIMULATION_MODE_BG_COLOR),
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = if (simulationMode == SimulationMode.PLAY) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

            }

        }

        Column(
            modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
        ) {

            Text(
                modifier = Modifier.width(60.dp),
                text = "x = ${mousePosition.x}",
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
            Text(
                modifier = Modifier,
                text = "y = ${mousePosition.y}",
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )

        }


        CitySchemeModeSwitch(
            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 10.dp),
            mode = schemeMode,
            onModeChanged = {
                onSchemeModeChange(it)
                if (it == Scheme2DMode.EDITOR) onSimulationModeChange(SimulationMode.PAUSE)
            }
        )

    }

}
