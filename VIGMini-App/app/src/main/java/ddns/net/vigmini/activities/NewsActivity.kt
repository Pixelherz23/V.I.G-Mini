package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.InformationAdapter
import ddns.net.vigmini.datamodel.News

class NewsActivity : AppCompatActivity() {
    lateinit var news: ArrayList<News>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

        // Lookup the recyclerview in activity layout
        val newsRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        // Initialize news
        news = News.createNewsList() // need to be implemented
        // Create adapter passing in the sample user data
        val adapter = InformationAdapter(news)
        // Attach the adapter to the recyclerview to populate items
        newsRecyclerView.adapter = adapter
        // Set layout manager to position the items
        newsRecyclerView.layoutManager = LinearLayoutManager(this)


    }
}