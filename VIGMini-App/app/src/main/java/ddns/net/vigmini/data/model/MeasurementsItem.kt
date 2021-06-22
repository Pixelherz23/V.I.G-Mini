package ddns.net.vigmini.data.model

import java.util.*

data class MeasurementsItem(
    val HUMIDITY: Int,
    val SOIL_MOISTURE: Double,
    val TEMPERATURE: Double,
    val TIME_STAMP: Date
)