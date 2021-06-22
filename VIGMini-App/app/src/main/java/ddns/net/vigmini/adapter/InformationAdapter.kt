package ddns.net.vigmini.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.data.model.Information
import ddns.net.vigmini.data.model.InformationItem

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
class InformationAdapter (private val infos: Information) : RecyclerView.Adapter<InformationAdapter.ViewHolder>() {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {

        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val headlineTextView = itemView.findViewById<TextView>(R.id.information_headline)
        val contentTextView = itemView.findViewById<TextView>(R.id.information_contentTextView)

    }

    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val infoView = inflater.inflate(R.layout.item_information, parent, false)
        // Return a new holder instance
        return ViewHolder(infoView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: InformationAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val info: InformationItem = infos[position]
        // Set item views based on your views and data model
        val textView = viewHolder.headlineTextView
        textView.setText(info.HEADLINE)
        val textView2 = viewHolder.contentTextView
        textView2.setText(info.CONTENT)

    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return infos.size
    }
}