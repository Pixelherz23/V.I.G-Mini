package ddns.net.vigmini.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.main.User
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Exception

@DelicateCoroutinesApi
open class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
                            RegisterGreenhouseDialog.RegisterGreenhouseDialogListener, ChangePasswordDialog.ChangePasswordDialogListener{
    private lateinit var drawer: DrawerLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(User.isLoggedIn){
            setContentView(R.layout.activity_menu)

            val toolbar = findViewById<Toolbar>(R.id.menu_toolbar)
            setSupportActionBar(toolbar)

            drawer = findViewById(R.id.drawer_layout)

            val navigationView: NavigationView = findViewById(R.id.nav_view)
            navigationView.setNavigationItemSelectedListener(this)

            val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer.addDrawerListener(toggle)
            toggle.syncState()



            if(savedInstanceState == null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    HomeFragment.newInstance(User.email!!)
                ).commit()

                navigationView.setCheckedItem(R.id.nav_home)
            }
            /*
            val userNameView = findViewById<TextView>(R.id.navHeader_name) as TextView
            val emailNameView = findViewById<TextView>(R.id.navHeader_email) as TextView
            userNameView.setText("$User.firstName $User.lastName")
            emailNameView.setText(User.email)
             */
        }else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){

            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_home ->{
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    HomeFragment.newInstance(User.email!!)).commit()
            }
            R.id.nav_news ->{
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsFragment()).commit()
            }
            R.id.nav_help ->{
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    HelpFragment()).commit()
            }
            R.id.nav_settings ->{
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    AccSettingsFragment.newInstance(User.firstName!!, User.lastName!!, User.email!!, R.string.placeholder.toString(), R.string.placeholder.toString())).commit()
            }
            R.id.nav_logout ->{
                User.logOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun applyText(productKey: String) {
        activateGreenhouse(productKey, User.email!!)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
            HomeFragment.newInstance(User.email!!)).commit()
    }

    override fun applyPassword(oldPw: String, newPw: String, newPwWdh: String) {
        if(newPw == newPwWdh){
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                AccSettingsFragment.newInstance(User.firstName!!, User.lastName!!, User.email!!, oldPw, newPw)).commit()
        }else{
            Toast.makeText(applicationContext, "Neues Password wiederholen stimmt nicht überein",  Toast.LENGTH_LONG).show()
        }
    }

    fun activateGreenhouse(productKey: String, email: String){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{

                val response = api.registerGreenhouse(productKey, email).awaitResponse()
                if (response.isSuccessful){

                }else if(response.code() == 404){
                    withContext(Dispatchers.Main){
                        Toast.makeText(applicationContext, "Produkt Schlüssel existiert nicht",  Toast.LENGTH_SHORT).show()
                    }

                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Es ist etwas schief gelaufen",  Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}