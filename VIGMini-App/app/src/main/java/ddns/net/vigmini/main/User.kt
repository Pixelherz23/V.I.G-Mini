package ddns.net.vigmini.main

object User {
    var isLoggedIn : Boolean = false
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null

    fun logIn(firstName: String, lastName: String, email: String){
        isLoggedIn = true
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }

    fun logOut(){
        isLoggedIn = false
        this.firstName = null
        this.lastName = null
        this.email = null
    }

    fun changeAttributes(firstName: String, lastName: String, email: String): Boolean{
        if(isLoggedIn){
            this.firstName = null
            this.lastName = null
            this.email = null
            return true
        } else return false
    }
}