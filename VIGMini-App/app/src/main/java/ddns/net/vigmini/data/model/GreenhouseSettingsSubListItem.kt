package ddns.net.vigmini.data.model

data class GreenhouseSettingsSubListItem(
    val FROM_TIME: String,
    val INTERVAL_TIME: Int,
    val MAX_TEMPERATURE: Int,
    val MIN_SOIL_MOISTURE: Int,
    val SOIL_MOISTURE_ON: Int,
    val TEMP_ON: Int,
    val TIMETABLE_ON: Int,
    val TO_TIME: String
)