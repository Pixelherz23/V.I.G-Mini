package ddns.net.vigmini.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.data.access.SEEKBAR_TEMP_RANGE
import ddns.net.vigmini.data.access.TIMETABLE_LIGHT
import ddns.net.vigmini.data.access.TIMETABLE_SOIL_MOISTURE
import ddns.net.vigmini.data.model.GreenhouseSettings
import ddns.net.vigmini.data.model.GreenhouseSettingsSubList
import ddns.net.vigmini.data.model.GreenhouseSettingsSubListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class GreenhouseSettingsAdapter (private val settings: GreenhouseSettings,
                                 private val productKey: String,
                                 private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolderTemperature(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val tempValue: TextView = itemView.findViewById(R.id.temperature_valueTextView)
        val tempSwitch: SwitchCompat = itemView.findViewById(R.id.temperature_switch)
        val tempSeekBar: SeekBar = itemView.findViewById(R.id.temperature_SeekBar)
        val tempButton: Button = itemView.findViewById(R.id.temperature_saveButton)
    }

    inner class ViewHolderSoilMoisture(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val soilMoistureValue: TextView = itemView.findViewById(R.id.soilMoistureV_valueTextView)
        val soilMoistureVSwitch: SwitchCompat = itemView.findViewById(R.id.soilMoistureV_switch)
        val soilMoistureSeekBar: SeekBar = itemView.findViewById(R.id.soilMoistureV_SeekBar)
        val soilMoistureSaveButton: Button = itemView.findViewById(R.id.soilMoistureV_saveButton)
        val soilMoistureTimeButton: Button = itemView.findViewById(R.id.soilMoistureV_timeButton)


    }

    inner class ViewHolderSoilMoistureTime(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val soilMoistureTime: TextView = itemView.findViewById(R.id.soilMoistureT_time)
        val soilMoistureTSwitch: SwitchCompat = itemView.findViewById(R.id.soilMoistureT_switch)
        val soilMoistureTSaveButton: Button = itemView.findViewById(R.id.soilMoistureT_saveButton)
        val soilMoistureTTimeButton: Button = itemView.findViewById(R.id.soilMoistureT_timeButton)
        val soilMoistureTValueButton: Button = itemView.findViewById(R.id.soilMoistureT_ValueButton)
    }

    inner class ViewHolderLight(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val lightFrom: TextView = itemView.findViewById(R.id.ligth_from)
        val lightTo: TextView = itemView.findViewById(R.id.ligth_to)
        val lightSwitch: SwitchCompat = itemView.findViewById(R.id.ligth_switch)
        val lightSaveButton: Button = itemView.findViewById(R.id.ligth_saveButton)
        val lightFromButton: Button = itemView.findViewById(R.id.ligth_fromButton)
        val lightToButton: Button = itemView.findViewById(R.id.ligth_toButton)

    }

    override fun getItemViewType(position: Int): Int {
        return when (position){
            0 -> 0
            1 -> {
                if(settings[1][0].TIMETABLE_ON == 0 && settings[1][1].TIMETABLE_ON == 0 && settings[1][2].TIMETABLE_ON == 0){
                    1
                }else{
                    2
                }
            }
            else -> 3
        }
    }

    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        when (viewType){
            0 -> {
                val infoView = inflater.inflate(R.layout.item_temperature, parent, false)
                return ViewHolderTemperature(infoView)
            }
            1 -> {
                val infoView = inflater.inflate(R.layout.item_soil_moisture_value, parent, false)
                return ViewHolderSoilMoisture(infoView)
            }
            2 -> {
                val infoView = inflater.inflate(R.layout.item_soil_moisture_time, parent, false)
                return ViewHolderSoilMoistureTime(infoView)
            }
            else -> {
                val infoView = inflater.inflate(R.layout.item_light, parent, false)
                return ViewHolderLight(infoView)
            }
        }
        // Inflate the custom layout

        // Return a new holder instance
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        // Get the data model based on position

        val subSettings: GreenhouseSettingsSubList = settings[position]

        when(getItemViewType(position)){
            0 -> {
                // Set item views based on your views and data model
                viewHolder as ViewHolderTemperature
                val temp: GreenhouseSettingsSubListItem = subSettings[0]

                viewHolder.tempValue.setText(temp.MAX_TEMPERATURE.toString() + "°C")
                viewHolder.tempSwitch.isChecked = temp.TEMP_ON == 1
                viewHolder.tempSeekBar.progress = temp.MAX_TEMPERATURE - SEEKBAR_TEMP_RANGE

                viewHolder.tempSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        if (seekBar != null) {
                            viewHolder.tempValue.setText((seekBar.progress + SEEKBAR_TEMP_RANGE).toString() + "°C")
                        }
                    }

                })
                viewHolder.tempButton.setOnClickListener {
                    val temp: String = viewHolder.tempValue.text.toString()
                    val tempOn: Boolean = viewHolder.tempSwitch.isChecked
                    updateGreenhouseSettings(temp.substring(0, 2).toInt(), 0, if(tempOn) 1 else 0, 0, "", "", 0)
                }
            }
            1 -> {
                viewHolder as ViewHolderSoilMoisture
                viewHolder.soilMoistureValue.setText(subSettings[0].MIN_SOIL_MOISTURE.toString() + "%")
                viewHolder.soilMoistureVSwitch.isChecked = subSettings[0].SOIL_MOISTURE_ON == 1
                viewHolder.soilMoistureSeekBar.progress = subSettings[0].MIN_SOIL_MOISTURE

                viewHolder.soilMoistureSaveButton.setOnClickListener {
                    val soil: String = viewHolder.soilMoistureValue.text.toString()
                    val soilOn: Boolean = viewHolder.soilMoistureVSwitch.isChecked
                    updateGreenhouseSettings( 0, soil.substring(0, 2).toInt(), 0, if(soilOn) 1 else 0, "", "", 0)
                }
            }
            2 -> {
                viewHolder as ViewHolderSoilMoistureTime
                viewHolder.soilMoistureTime.setText(subSettings[0].FROM_TIME.subSequence(0, 5))
                viewHolder.soilMoistureTSwitch.isChecked = subSettings[0].TIMETABLE_ON == 1
            }
            3 -> {
                viewHolder as ViewHolderLight
                viewHolder.lightFrom.setText(subSettings[0].FROM_TIME.subSequence(0, 5))
                viewHolder.lightTo.setText(subSettings[0].TO_TIME.subSequence(0, 5))
                viewHolder.lightSwitch.isChecked = subSettings[0].TIMETABLE_ON == 1
            }
            else -> Log.e("onBind", "Error")
        }
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return settings.size
    }

    private fun updateGreenhouseSettings(maxTemp: Int, minSoilMoisture: Int,
                                            tempOn: Int, soilMoistureOn: Int,
                                            fromTime: String, toTime: String, timeTableOn: Int){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = if(maxTemp != 0){
                    api.updateSettingsTemp(productKey, maxTemp, tempOn).awaitResponse()
                }else if(minSoilMoisture != 0){
                    api.updateSettingsSoilMoisture(productKey, minSoilMoisture, soilMoistureOn).awaitResponse()
                }else if(fromTime != "" && toTime != ""){
                    api.updateSettingsLight(productKey, fromTime, toTime, timeTableOn, TIMETABLE_LIGHT, 1).awaitResponse()
                }else{
                    api. updateSettingsSoilMoistureTime(productKey, fromTime, timeTableOn, TIMETABLE_SOIL_MOISTURE, 1).awaitResponse()
                }
                if(response.isSuccessful){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Gespeichert",  Toast.LENGTH_SHORT).show()
                    }
                }else if(response.code() == 404){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Gewächshaus existiert nicht",  Toast.LENGTH_SHORT).show()
                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, response.code().toString(),  Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Es ist etwas schief gelaufen",  Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
