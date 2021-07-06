package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.InformationAdapter
import ddns.net.vigmini.data.access.ApiService
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Exception

open class InformationFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    @DelicateCoroutinesApi
    protected fun getInformationData(view: View, type: String){

        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = api.getInformation(type).awaitResponse()
                if (response.isSuccessful){
                    val data = response.body()!!

                    withContext(Dispatchers.Main){
                        // Lookup the recyclerview in activity layout
                        val newsRecyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
                        // Create adapter passing in the help data
                        val adapter = InformationAdapter(data)
                        // Attach the adapter to the recyclerview to populate items
                        newsRecyclerView.adapter = adapter
                        // Set layout manager to position the items
                        newsRecyclerView.layoutManager = LinearLayoutManager(activity)
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){

                }
            }


        }

    }
}