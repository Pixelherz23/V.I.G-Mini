package ddns.net.vigmini.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.main.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.lang.Exception
@DelicateCoroutinesApi
class LoginActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.login_emailEditText)
        val passwordEditText: EditText = findViewById(R.id.login_passwordEditText)
        val loginButton: Button = findViewById(R.id.login_loginButton)
        val forgotPwTextView: TextView = findViewById(R.id.login_forgotPasswordTextView)
        val registerTextView: TextView = findViewById(R.id.login_registerTextView)

        loginButton.setOnClickListener{
            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()
            login(email, password)
        }

        forgotPwTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }


    private fun login(email: String, password: String){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = api.userLogin(email, password).awaitResponse()
                if(response.isSuccessful){
                    Log.e("bla", "error")
                    val response2 = api.getUser(email).awaitResponse()

                    if(response2.isSuccessful){
                        val data = response2.body()!!
                        User.logIn(data[0][0], data[0][1], email)
                        val intent = Intent(applicationContext, MenuActivity::class.java)
                        startActivity(intent)
                    }else{
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(applicationContext, "Irgendwas ist schief gelaufen",  Toast.LENGTH_SHORT).show()
                        }

                    }

                }else if(response.code() == 403){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, "Email oder Passwort flasch", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, "${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, e.toString(),  Toast.LENGTH_SHORT).show()
                    Log.e("error", e.toString())
                }
            }
        }
    }


}