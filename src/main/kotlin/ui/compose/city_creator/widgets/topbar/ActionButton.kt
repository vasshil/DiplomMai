package ui.compose.city_creator.widgets.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color = Color.Black,
    onClick: () -> Unit
) {

    IconButton(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp)
            .background(Color.LightGray),
        onClick = onClick
    ) {

        Icon(icon, contentDescription = null, tint = tint)

    }

}
