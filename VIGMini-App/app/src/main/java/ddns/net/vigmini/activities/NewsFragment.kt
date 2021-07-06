package ddns.net.vigmini.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ddns.net.vigmini.R

class NewsFragment : InformationFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.activity_cardview, container, false)
        getInformationData(view,"news")
        return view
    }
}