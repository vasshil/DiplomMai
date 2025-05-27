package ui.compose.city_creator.widgets.side_panel.delivery_panel


enum class DeliveryPanelMode(val localization: String, val iconPath: String) {
    DRONES("БПЛА", "icons/ic_helicopter.png"),
    CARGOS("Грузы", "icons/ic_box.png")
}