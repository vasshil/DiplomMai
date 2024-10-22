package ui.compose.city_creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import model.City


@Composable
fun BuildingList(modifier: Modifier = Modifier, city: City) {

    LazyColumn(
        modifier = modifier,
    ) {

        city.buildings.forEach { building ->
            item {
                BuildingItem(
                    modifier = Modifier,
                    building = building
                )
            }
        }

    }

}
