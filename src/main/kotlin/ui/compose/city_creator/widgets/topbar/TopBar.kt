package ui.compose.city_creator.widgets.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import ui.compose.city_creator.CityCreatorMode
import ui.compose.city_creator.CitySchemeMode
import ui.compose.common.BASE_STATION_COLOR
import ui.compose.common.DESTINATION_COLOR
import ui.compose.common.FOCUSED_BUILDING_COLOR


@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    mousePosition: Offset,
    editorMode: CityCreatorMode,
    saveCity: () -> Unit,
    loadCity: () -> Unit,
    onEditorModeChange: (CityCreatorMode) -> Unit,
    schemeMode: CitySchemeMode,
    onSchemeModeChange: (CitySchemeMode) -> Unit,
) {

    Row(
        modifier = modifier
            .background(Color.LightGray),
    ) {

        if (schemeMode == CitySchemeMode.EDITOR) {

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CityCreatorMode.ADD_BUILDING,
                selectedMode = editorMode,
                icon = Icons.Filled.AddHome,
                tint = FOCUSED_BUILDING_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CityCreatorMode.ADD_BASE_STATION,
                selectedMode = editorMode,
                icon = Icons.Filled.AddCircle,
                tint = BASE_STATION_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CityCreatorMode.ADD_DESTINATION,
                selectedMode = editorMode,
                icon = Icons.Filled.AddCircle,
                tint = DESTINATION_COLOR,
                onEditorModeChange = onEditorModeChange
            )

            ActionCheckButton(
                modifier = Modifier,
                buttonMode = CityCreatorMode.REMOVE,
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

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
        ) {

            Text(
                modifier = Modifier,
                text = "x = ${mousePosition.x}",
                fontSize = 10.sp,
            )
            Text(
                modifier = Modifier,
                text = "y = ${mousePosition.y}",
                fontSize = 10.sp,
            )

        }


        CitySchemeModeSwitch(
            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 10.dp),
            mode = schemeMode,
            onModeChanged = onSchemeModeChange
        )

    }

}
