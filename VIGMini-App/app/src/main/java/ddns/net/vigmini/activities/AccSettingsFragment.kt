package ddns.net.vigmini.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ddns.net.vigmini.R
import ddns.net.vigmini.data.access.ApiService
import ddns.net.vigmini.main.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.awaitResponse
import java.lang.Exception

class AccSettingsFragment : Fragment() {

    companion object{
        private const val FIRSTNAME = "firstname"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
        private const val NEWPASSWORD = "newPassword"

        fun newInstance(firstname: String, name: String, email: String, password: String, newPassword: String) = AccSettingsFragment().apply {
            arguments = Bundle().apply {
                putString(FIRSTNAME, firstname)
                putString(NAME, name)
                putString(EMAIL, email)
                putString(PASSWORD, password)
                putString(NEWPASSWORD, newPassword)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.activity_acc_settings, container, false)

        val emailEditText: EditText = view.findViewById(R.id.accSettings_emailEditText)
        val firstNameEditText: EditText = view.findViewById(R.id.accSettings_firstNameEditText)
        val nameEditText: EditText = view.findViewById(R.id.accSettings_nameEditText)
        val passwordEditText: EditText = view.findViewById(R.id.accSettings_passwordEditText)
        val safeButton: Button = view.findViewById(R.id.accSettings_saveButton)

        emailEditText.setText(arguments?.getString(EMAIL))
        firstNameEditText.setText(arguments?.getString(FIRSTNAME))
        nameEditText.setText(arguments?.getString(NAME))

        passwordEditText.setText(arguments?.getString(PASSWORD))
        passwordEditText.setOnClickListener{
            val dialog = ChangePasswordDialog()
            this.fragmentManager?.let { it1 -> dialog.show(it1, "change password dialog") }
        }

        safeButton.setOnClickListener{
            Log.e("safeButton", "save")
            saveAccSettings(firstNameEditText.text.toString(), nameEditText.text.toString(),
                User.email!!, emailEditText.text.toString(), arguments?.getString(PASSWORD)!!,
                arguments?.getString(NEWPASSWORD)!!)
        }
        return view
    }

    fun saveAccSettings(firstName: String, name: String, email: String, newEmail: String, password: String, newPassword: String): Boolean{
        val api = ApiService.buildService()
        var wrongPw: Boolean = false
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = if(newEmail != email && newPassword != password){
                    api.updateUser(firstName, name, email, newEmail, password, newPassword).awaitResponse()
                }else if(newEmail != email){
                    api.updateUser(firstName, name, email, newEmail).awaitResponse()
                }else if(newPassword != password){
                    api.updateUser(firstName, name, email, password, newPassword).awaitResponse()
                }else{
                    api.updateUser(firstName, name, email).awaitResponse()
                }
                if (response.isSuccessful){
                    withContext(Dispatchers.Main){
                        Toast.makeText(activity, "Gespeichert",  Toast.LENGTH_SHORT).show()
                        User.firstName = firstName
                        User.lastName = name
                        User.email = newEmail
                    }
                }else if(response.code() == 403){
                    withContext(Dispatchers.Main){
                        wrongPw = true
                        Toast.makeText(activity, "Falsches Password",  Toast.LENGTH_SHORT).show()
                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(activity, response.code().toString(),  Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(activity, "Es ist etwas schief gelaufen",  Toast.LENGTH_SHORT).show()
                }
            }
        }
        return wrongPw
    }


}
