package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.InformationAdapter
import ddns.net.vigmini.data.access.ApiService
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Exception

open class InformationActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

    }

    @DelicateCoroutinesApi
    protected fun getCurrentData(reference :InformationActivity, type: String){

        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = api.getInformation(type).awaitResponse()
                if (response.isSuccessful){
                    val data = response.body()!!

                    withContext(Dispatchers.Main){
                        // Lookup the recyclerview in activity layout
                        val newsRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
                        // Create adapter passing in the help data
                        val adapter = InformationAdapter(data)
                        // Attach the adapter to the recyclerview to populate items
                        newsRecyclerView.adapter = adapter
                        // Set layout manager to position the items
                        newsRecyclerView.layoutManager = LinearLayoutManager(reference)
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