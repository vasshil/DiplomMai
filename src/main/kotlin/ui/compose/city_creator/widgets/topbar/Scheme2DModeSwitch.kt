package ui.compose.city_creator.widgets.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.compose.city_creator.Scheme2DMode
import ui.compose.common.MODE_SWITCH_BG_COLOR
import ui.compose.common.MODE_SWITCH_SELECTED_COLOR
import ui.compose.common.MODE_SWITCH_UNSELECTED_COLOR

@Composable
fun CitySchemeModeSwitch(
    modifier: Modifier = Modifier,
    mode: Scheme2DMode,
    onModeChanged: (Scheme2DMode) -> Unit
) {

    Row(
        modifier = modifier.background(color = MODE_SWITCH_BG_COLOR, shape = RoundedCornerShape(5.dp)).padding(5.dp),
    ) {

        for (i in Scheme2DMode.entries.indices) {
            val m = Scheme2DMode.entries[i]

            Card(
                modifier = Modifier.clickable { onModeChanged(m) },
                backgroundColor = if (m == mode) MODE_SWITCH_SELECTED_COLOR else MODE_SWITCH_UNSELECTED_COLOR,
                elevation = if (m == mode) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp).align(Alignment.CenterVertically),
                        imageVector = m.icon,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = m.localizedName,
                        fontSize = 12.sp
                    )
                }
            }
            if (i != Scheme2DMode.entries.lastIndex) {
                Spacer(modifier = Modifier.width(5.dp))
            }
        }


    }

}