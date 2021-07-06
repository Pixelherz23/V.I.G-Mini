package ddns.net.vigmini.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.GreenhouseAdapter
import ddns.net.vigmini.adapter.GreenhouseDetailsAdapter
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.data.access.PRODUCT_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.lang.Exception

class GreenhouseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)
        getMeasurementsData(this)
    }

    private fun getMeasurementsData(reference: GreenhouseDetailActivity){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                // email ist hard coded
                val response = api.getMeasurements("day", intent.getStringExtra(PRODUCT_KEY)!!).awaitResponse()
                if (response.isSuccessful){
                    val data = response.body()!!

                    withContext(Dispatchers.Main){
                        // Lookup the recyclerview in activity layout
                        val detailRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
                        // Create adapter passing in the help data
                        val adapter = GreenhouseDetailsAdapter(data, intent.getStringExtra(
                            PRODUCT_KEY)!!, applicationContext)
                        // Attach the adapter to the recyclerview to populate items
                        detailRecyclerView.adapter = adapter
                        // Set layout manager to position the items
                        detailRecyclerView.layoutManager = LinearLayoutManager(reference)

                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Seems like something went wrong",  Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}