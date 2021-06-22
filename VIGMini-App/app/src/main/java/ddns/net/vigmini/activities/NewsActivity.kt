package ddns.net.vigmini.activities

import android.os.Bundle
import ddns.net.vigmini.R

class NewsActivity : InformationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

        // Initialize news
        getCurrentData(this, "news")
    }
}