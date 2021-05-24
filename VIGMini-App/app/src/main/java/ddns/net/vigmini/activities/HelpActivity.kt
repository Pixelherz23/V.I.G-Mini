package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.InformationAdapter
import ddns.net.vigmini.datamodel.Help
import ddns.net.vigmini.datamodel.News

class HelpActivity : AppCompatActivity() {
    lateinit var help: ArrayList<Help>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

        // Lookup the recyclerview in activity layout
        val helpRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        // Initialize news
        help = Help.createHelpList() // need to be implemented
        // Create adapter passing in the sample user data
        val adapter = InformationAdapter(help)
        // Attach the adapter to the recyclerview to populate items
        helpRecyclerView.adapter = adapter
        // Set layout manager to position the items
        helpRecyclerView.layoutManager = LinearLayoutManager(this)


    }
}