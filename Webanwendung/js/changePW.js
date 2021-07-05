
/**
 * function to form a body to call the sendrequestNewPw function with.
 * 
 * @param {*} pw1 the DOM of the old password
 * @param {*} pw2 the DOM of the new password
 */
function loginPWChange(pw1, pw2){
  
    console.log(localStorage.getItem("fn"));
    var firstname = localStorage.getItem("fn");
    var lastname = localStorage.getItem("ln");
    var email = localStorage.getItem("email");
    var password1 = pw1.value;
    var password2 = pw2.value;
    var url = 'https://vig-mini.ddns.net/user/information/update';

    var body = { 
        'firstname': firstname,
        'lastname': lastname,
        'e-mail': email,
        'password': password1,
        'new_password': password2   
    }
        
    

    if(checkpw(pw2)){
        sendrequestNewPW(body, url);
        console.log("erfolg");
    }else{
        alert("Bitte überprüfen sie ihre Eingabe des neues Passwortes")
    }
    

}

/**
 * Checks if the password has at least one upper case letter, at least one lower case letter, at least one digit, at least one special character
 * and minium of eight in length
 * 
 * @param {*} pw the password which will be checked
 * @returns returns true if the password matches the regex
 */
function checkpw(pw){
    var allowedChars = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/
    if(pw.value.match(allowedChars)){
        pw.setCustomValidity("")
        return true;
    }else{
        pw.setCustomValidity("Passwort muss min. 8 Zeichen lang sein und min. 1 Großbuchstaben, min. 1 Kleinbuchstaben und min 1 Sonderzeichen(#?!@$%^&*-)");
        return false;
    }
}

/**
 * Uses fetch send a POST-Request to a specified url.
 * If the change was successfull the user will get notified.
 * 
 * @param {*} body 
 * @param {*} url 
 */
function sendrequestNewPW(body, url){

    var formBody = [];
    for (var property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    
    formBody = formBody.join("&");
    console.log("body joined, body:", formBody);
    

    fetch(url, {

        method: 'POST',
        mode: "cors",
        headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formBody
        
    })
    .then(response => {
        responseReal = response.status;

        if(responseReal == 200){
            alert("Passwort erfolgreich geändert!");
            window.location ="./Acc-Einstellungen.html"
        }else{
            alert("Altes Password falsch!")
        }
    })
}

/**
 * function to form a body to call function sendrequestUserData with.
 * 
 */
function loginUserData(){
    var email = localStorage.getItem("email");
    var url = 'https://vig-mini.ddns.net/user/information'

    var body = { 
        'e-mail': email,
    }

    sendrequestUserData(url, body);

}

/**
 * Uses fetch to send a GET-Request and stores the reponse into the localstorage
 * 
 * @param {*} url the url given by loginUserData
 * @param {*} body the body given by loginUserData
 */
function sendrequestUserData(url,body){

    var formBody = [];
    for (var property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    url = url + "?" + formBody;
    console.log("body joined, body:", formBody);
    

    fetch(url, {

        method: 'GET',
        mode: "cors",
        headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
        },
        
    })
    .then(response => {
        responseReal = response.status;

        if(responseReal == 200){
            response.json().then(function(data) {
                console.log(data);
                localStorage.setItem("fn", data[0][0]);
                localStorage.setItem("ln", data[0][1]);
            })
        }else{
            alert("Fehler")
        }
    })
        
}