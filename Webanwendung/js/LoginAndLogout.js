/**
 * Checks if the token is set in localstorage
 * 
 * @returns returns true if it is set
 */
function isLoggedIn(){
    const token = localStorage.getItem("auth");
    console.log(token);
    if(token == null){
        return false;
    }else{
        return true;
    }
}

/**
 * redirects users which bypass the sign in page
 */
function autoRedirect(){
    const validLogin = isLoggedIn();

    if(validLogin == true ){

    }else{
        window.location = "./index.html";
    }

}
/**
 * Empties the localstorage and redirects the User to the sign in page
 */
function logOut(){
    localStorage.clear();
    console.log("token: ", localStorage.getItem("auth") )
    window.location = "./index.html"
}
/**
 * Appends the firstname of the User to the welcome Message
 * 
 * @param {*} namediv the DOM where to put the name
 */
function loadName(namediv){
    var divText = document.createTextNode("Willkommen, " + localStorage.getItem("showfirstname") + "!");
    namediv.appendChild(divText);
}