package ui.compose.city_creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    buttonMode: CityCreatorMode,
    selectedMode: CityCreatorMode,
    icon: ImageVector,
    tint: Color = Color.Black,
    onModeChange: (CityCreatorMode) -> Unit
) {

    val checked by derivedStateOf { buttonMode == selectedMode}

    IconButton(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp)
            .background(if (checked) Color.Gray else Color.LightGray),
        onClick = {
            println("click $buttonMode $selectedMode $checked")
            onModeChange(if (checked) CityCreatorMode.NONE else buttonMode)
        }
    ) {

        Icon(icon, contentDescription = null, tint = tint)

    }

}
