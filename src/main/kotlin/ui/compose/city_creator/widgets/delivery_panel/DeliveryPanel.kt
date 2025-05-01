package ui.compose.city_creator.widgets.delivery_panel

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import model.City
import ui.compose.city_creator.widgets.delivery_panel.drones.DronesList

@Composable
fun DeliveryPanel(
    modifier: Modifier = Modifier,
    city: City,
) {

    DronesList(
        modifier = modifier,
        city = city,
        onFocusChange = { focused, droneId ->

        },
        onDroneChanged = { drone ->

        }

    )

}