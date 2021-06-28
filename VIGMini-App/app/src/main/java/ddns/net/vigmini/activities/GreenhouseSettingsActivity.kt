package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R

class GreenhouseSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

        val gSettingsRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        gSettingsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}