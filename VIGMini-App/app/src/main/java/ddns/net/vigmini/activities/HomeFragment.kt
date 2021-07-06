package ddns.net.vigmini.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ddns.net.vigmini.R
import ddns.net.vigmini.adapter.GreenhouseAdapter
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.data.access.PRODUCT_KEY
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Exception

@DelicateCoroutinesApi
class HomeFragment : Fragment() {

    companion object{
        private const val EMAIL: String = "EMAIL"
        fun newInstance(email: String) = HomeFragment().apply{
           arguments = Bundle().apply {
               putString(EMAIL, email)
           }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.activity_main, container, false)
        getGreenhouseData(view)

        val newGwButton: FloatingActionButton = view.findViewById(R.id.main_button)
        newGwButton.setOnClickListener{
            val dialog = RegisterGreenhouseDialog()
            this.fragmentManager?.let { it1 -> dialog.show(it1, "activate greenhouse dialog") }
        }
        return view
    }

    private fun getGreenhouseData(view: View){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                var email: String = ""
                arguments?.getString(EMAIL)?.let {
                    email = it
                }
                val response = api.getGreenhouses(email).awaitResponse()
                if (response.isSuccessful){
                    val data = response.body()!!

                    withContext(Dispatchers.Main){
                        // Lookup the recyclerview in activity layout
                        val mainRecyclerView = view.findViewById<View>(R.id.main_recyclerView) as RecyclerView
                        // Create adapter passing in the help data
                        val adapter = GreenhouseAdapter(data)
                        // Attach the adapter to the recyclerview to populate items
                        mainRecyclerView.adapter = adapter
                        // Set layout manager to position the items
                        mainRecyclerView.layoutManager = LinearLayoutManager(activity)

                        adapter.onItemClick = { greenhouse ->

                            val intent = Intent(activity, GreenhouseDetailActivity::class.java)
                            intent.putExtra(PRODUCT_KEY, greenhouse.PRODUCT_KEY)
                            startActivity(intent)
                        }
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){

                }
            }


        }
    }
}