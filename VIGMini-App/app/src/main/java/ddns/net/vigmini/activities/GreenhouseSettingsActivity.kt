package ddns.net.vigmini.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.GreenhouseSettingsAdapter
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.data.access.PRODUCT_KEY
import ddns.net.vigmini.data.model.GreenhouseSettings
import ddns.net.vigmini.data.model.GreenhouseSettingsSubList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.lang.Exception

class GreenhouseSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)
        getGreenhouseSettings(this)

    }

    private fun getGreenhouseSettings(reference :GreenhouseSettingsActivity){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // hardcoded Product Key
                val responseTemp = api.getGreenhouseSettings("temperature2", intent.getStringExtra(
                    PRODUCT_KEY)!!).awaitResponse()
                val responseSoilMoisture = api.getGreenhouseSettings("soil-moisture", intent.getStringExtra(
                    PRODUCT_KEY)!!).awaitResponse()
                val responseLight = api.getGreenhouseSettings("light", intent.getStringExtra(
                    PRODUCT_KEY)!!).awaitResponse()
                if(responseTemp.isSuccessful && responseSoilMoisture.isSuccessful && responseLight.isSuccessful){
                    val data = GreenhouseSettings()
                    data.add(responseTemp.body()!!)
                    data.add(responseSoilMoisture.body()!!)
                    data.add(responseLight.body()!!)

                    withContext(Dispatchers.Main){
                        val gSettingsRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
                        val adapter = GreenhouseSettingsAdapter(data, intent.getStringExtra(PRODUCT_KEY).toString(), applicationContext)
                        gSettingsRecyclerView.adapter = adapter
                        gSettingsRecyclerView.layoutManager = LinearLayoutManager(reference)
                    }
                }else{
                    Log.e("GSettings","NoData")
                }

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Seems like something went wrong",  Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}