package ddns.net.vigmini.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.data.model.Greenhouse
import ddns.net.vigmini.data.model.GreenhouseItem
import kotlinx.coroutines.*
import retrofit2.awaitResponse

class GreenhouseAdapter (private val greenhouses: Greenhouse) : RecyclerView.Adapter<GreenhouseAdapter.ViewHolder>(){


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {

        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val nameTextView: TextView = itemView.findViewById(R.id.greenhouse_name)
        val temperatureTextView: TextView = itemView.findViewById(R.id.greenhouse_temperature_value)
        val humidityTextView: TextView = itemView.findViewById(R.id.greenhouse_humidity_value)
        val soilMoistureTextView: TextView = itemView.findViewById(R.id.greenhouse_soil_moisture_value)

    }

    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenhouseAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val infoView = inflater.inflate(R.layout.item_greenhouse, parent, false)
        // Return a new holder instance
        return ViewHolder(infoView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: GreenhouseAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val greenhouse: GreenhouseItem = greenhouses[position]
        // Set item views based on your views and data model
        val name = viewHolder.nameTextView
        name.setText(greenhouse.NAME)

        getMeasurements(greenhouse.PRODUCT_KEY, viewHolder)
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return greenhouses.size
    }


    @SuppressLint("SetTextI18n")
    fun getMeasurements(productKey: String, viewHolder: GreenhouseAdapter.ViewHolder){
        val api = ApiService.buildService()
        val temp = viewHolder.temperatureTextView
        val humidity = viewHolder.humidityTextView
        val soilMoisture = viewHolder.soilMoistureTextView
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getMeasurementsNow(productKey).awaitResponse()
                if (response.isSuccessful) {
                    val data = response.body()!!

                    withContext(Dispatchers.Main) {

                        temp.setText("${data.TEMPERATURE} °C")
                        humidity.setText("${data.HUMIDITY}%")
                        soilMoisture.setText("${data.SOIL_MOISTURE}%")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    temp.setText("/ °C")
                    humidity.setText("/ %")
                    soilMoisture.setText("/ %")
                }
            }
        }
    }
}