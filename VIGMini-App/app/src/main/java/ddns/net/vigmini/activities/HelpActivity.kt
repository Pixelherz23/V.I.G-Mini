package ddns.net.vigmini.activities

import android.os.Bundle
import ddns.net.vigmini.R

class HelpActivity : InformationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardview)

        // Initialize help
        getCurrentData(this, "help")
    }
}