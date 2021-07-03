package ddns.net.vigmini.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.GreenhouseAdapter
import ddns.net.vigmini.adapter.InformationAdapter
import ddns.net.vigmini.data.access.ApiService
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Exception
import java.lang.ref.PhantomReference

open class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getGreenhouseData(this)
    }

    @DelicateCoroutinesApi
    private fun getGreenhouseData(reference: MainActivity){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                // email ist hard coded
                val response = api.getGreenhouses("abcdef123@gmx.de").awaitResponse()
                if (response.isSuccessful){
                    val data = response.body()!!

                    withContext(Dispatchers.Main){
                        // Lookup the recyclerview in activity layout
                        val mainRecyclerView = findViewById<View>(R.id.main_recyclerView) as RecyclerView
                        // Create adapter passing in the help data
                        val adapter = GreenhouseAdapter(data)
                        // Attach the adapter to the recyclerview to populate items
                        mainRecyclerView.adapter = adapter
                        // Set layout manager to position the items
                        mainRecyclerView.layoutManager = LinearLayoutManager(reference)
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