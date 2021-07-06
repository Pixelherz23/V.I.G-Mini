package ddns.net.vigmini.activities

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailEditText: EditText = findViewById(R.id.register_emailEditText)
        val firstnameEditText: EditText = findViewById(R.id.register_firstNameEditText)
        val nameEditText: EditText = findViewById(R.id.register_nameEditText)
        val passwordEditText: EditText = findViewById(R.id.register_passwordEditText)
        val passwordWdhEditText: EditText = findViewById(R.id.register_passwordWdhEditText)
        val loginButton: Button = findViewById(R.id.register_registerButton)
        val loginTextView: TextView = findViewById(R.id.register_loginTextView)

        loginButton.setOnClickListener{
            val email: String = emailEditText.text.toString()
            val firstName: String = firstnameEditText.text.toString()
            val name: String = nameEditText.text.toString()
            val password: String = passwordEditText.text.toString()
            val passwordWdh: String = passwordWdhEditText.text.toString()

            if(password == passwordWdh){
                register(firstName, name, email, password)
            }else{
                Toast.makeText(applicationContext, "Passwort stimmt nicht überein", Toast.LENGTH_SHORT).show()
            }

        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    fun register(firstName: String, name: String, email: String, password: String){
        val api = ApiService.buildService()

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = api.newUser(firstName, name, email, password).awaitResponse()
                if(response.isSuccessful){
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

                }else if(response.code() == 409){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, "Email bereits registriert", Toast.LENGTH_SHORT).show()
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