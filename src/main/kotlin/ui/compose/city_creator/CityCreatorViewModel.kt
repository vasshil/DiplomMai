package ui.compose.city_creator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.City
import model.landscape.Building

class CityCreatorViewModel {

    private val scope = CoroutineScope(Dispatchers.IO)

    val cityFlow = MutableStateFlow(City())

    fun setCity(city: City) {
        scope.launch {
            cityFlow.emit(city)
        }
    }

    fun updateBuilding(building: Building) {
        cityFlow.update { city ->
            city.copy(
                buildings = city.buildings.map { b ->
                    if (b.id == building.id) building else b
                }.toMutableList()
            )
        }
//        scope.launch {
//            cityFlow.emit (//{ city ->
//                cityFlow.value.copy(
//                    buildings = cityFlow.value.buildings.map { b ->
//                        if (b.id == building.id) building else b
//                    }.toMutableList()
//                )
//            )
//        }

    }


}