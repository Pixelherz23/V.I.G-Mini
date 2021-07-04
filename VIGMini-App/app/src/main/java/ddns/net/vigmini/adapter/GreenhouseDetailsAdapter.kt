package ddns.net.vigmini.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.data.Set
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.DETAIL_ITEMS
import ddns.net.vigmini.data.model.Measurements

class GreenhouseDetailsAdapter (private val measurements: Measurements) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val anyChartView :AnyChartView = itemView.findViewById(R.id.detail_anyChartView)

    }

    inner class ButtonBarViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val spinnerDiagram :Spinner = itemView.findViewById(R.id.detailChart_spinner)
        val spinnerInterval :Spinner = itemView.findViewById(R.id.detailInterval_spinner)

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
        val cartesian: Cartesian = AnyChart.line()
        cartesian.animation(true)

        val chartData = ArrayList<DataEntry>()
        when(type){
            1->{
                cartesian.title("Temperatur")
                for(measurement in measurements){
                    chartData.add(ValueDataEntry(measurement.TIME_STAMP.substring(0, 15), measurement.TEMPERATURE.toDouble()))
                }
            }
            2->{
                cartesian.title("Luftfeuchtigkeit")
                for(measurement in measurements){
                    chartData.add(ValueDataEntry(measurement.TIME_STAMP.substring(0, 15), measurement.HUMIDITY))
                }
            }
            3->{
                cartesian.title("Bodenfeuchtigkeit")
                for(measurement in measurements){
                    chartData.add(ValueDataEntry(measurement.TIME_STAMP.substring(0, 15), measurement.SOIL_MOISTURE.toDouble()))
                }
            }
        }
        /*val set = Set.instantiate()
        set.data(chartData)
        val mapping = set.mapAs("{x: 'x', value 'value'}")
        cartesian.line(mapping)*/
        holder as ViewHolder
        holder.anyChartView.setChart(cartesian)
    }

}