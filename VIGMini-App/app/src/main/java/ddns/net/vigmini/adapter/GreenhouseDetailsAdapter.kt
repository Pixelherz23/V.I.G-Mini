package ddns.net.vigmini.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import ddns.net.vigmini.R

import ddns.net.vigmini.activities.GreenhouseSettingsActivity
import ddns.net.vigmini.data.access.DETAIL_ITEMS
import ddns.net.vigmini.data.access.PRODUCT_KEY

import ddns.net.vigmini.data.model.Measurements



class GreenhouseDetailsAdapter (private val measurements: Measurements, private val productKey: String, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val anyChartView :LineChart = itemView.findViewById(R.id.detail_anyChartView)

    }

    inner class ButtonBarViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val buttonSettings: Button = itemView.findViewById(R.id.detailSettings_button)
        val buttonInterval: Button = itemView.findViewById(R.id.detailSettings_button)

    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0) 2 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        when(viewType){
            1 -> {
                val infoView = inflater.inflate(R.layout.item_detail_button_bar, parent, false)
                // Return a new holder instance
                return  ButtonBarViewHolder(infoView)
            }
            else -> {
                val infoView = inflater.inflate(R.layout.item_detail_any_chart, parent, false)
                // Return a new holder instance
                return  ViewHolder(infoView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            1 -> {
                holder as ButtonBarViewHolder
                holder.buttonSettings.setOnClickListener {
                    val intent = Intent(context, GreenhouseSettingsActivity::class.java)
                    intent.putExtra(PRODUCT_KEY, productKey)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent)
                }
            }
            2 -> {
                lineChart(position, holder)
            }
        }
    }

    override fun getItemCount(): Int {
        return DETAIL_ITEMS
    }

    private fun lineChart(type: Int, holder: RecyclerView.ViewHolder){

        var label: String = ""
        var xAxisValues = ArrayList<String>()
        val lineEntry = ArrayList<Entry>()

        when(type){
            1->{
                label = "Temperatur"
                var index: Int = 0
                for(measurement in measurements){
                    xAxisValues.add(measurement.TIME_STAMP.substring(0, 22))
                    lineEntry.add(Entry(measurement.TEMPERATURE.toFloat(), index))
                    index++
                }
            }
            2->{
                label = "Luftfeuchtigkeit"
                var index: Int = 0
                for(measurement in measurements){
                    xAxisValues.add(measurement.TIME_STAMP.substring(0, 22))
                    lineEntry.add(Entry(measurement.HUMIDITY.toFloat(), index))
                    index++
                }
            }
            3->{
                label = "Bodenfeuchtigkeit"
                var index: Int = 0
                for(measurement in measurements){
                    xAxisValues.add(measurement.TIME_STAMP.substring(0, 22))
                    lineEntry.add(Entry(measurement.SOIL_MOISTURE.toFloat(), index))
                    index++
                }
            }
        }

        val lineDataSet = LineDataSet(lineEntry, label)
        val data = LineData(xAxisValues, lineDataSet)
        holder as ViewHolder
        holder.anyChartView.data = data
    }

}