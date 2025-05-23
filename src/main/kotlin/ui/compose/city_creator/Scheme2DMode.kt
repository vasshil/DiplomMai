package ui.compose.city_creator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

enum class Scheme2DMode(val localizedName: String, val icon: ImageVector) {
    VIEW("Просмотр", Icons.Default.Visibility),
    EDITOR("Редактор", Icons.Default.Edit),
}