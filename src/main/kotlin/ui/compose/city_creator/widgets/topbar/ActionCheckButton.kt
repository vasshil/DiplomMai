package ui.compose.city_creator.widgets.topbar

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
fun ActionCheckButton(
    modifier: Modifier = Modifier,
    buttonMode: CreatorModeEnum,
    selectedMode: CreatorModeEnum,
    icon: ImageVector,
    tint: Color = Color.Black,
    onEditorModeChange: (CreatorModeEnum) -> Unit
) {

    val checked by derivedStateOf { buttonMode == selectedMode}

    IconButton(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp)
            .background(if (checked) Color.Gray else Color.Transparent),
        onClick = {
            println("click $buttonMode $selectedMode $checked")
            onEditorModeChange(if (checked) CreatorModeEnum.NONE else buttonMode)
        }
    ) {

        Icon(icon, contentDescription = null, tint = tint)

    }

}
