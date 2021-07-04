/**
 * This function creates a body of the given DOM's and checks if the user did the correct Input e.g.
 * password1 = password2.
 * If thats the case the function will the sendrequest function with the newly created body.
 * 
 * 
 * @param {*} fn DOM of firstname
 * @param {*} ln DOM of lastname
 * @param {*} mail DOM of e-mail adress
 * @param {*} pw1 DOM of first password
 * @param {*} pw2 DOM of repeated password
 */

function weiterleiten( fn, ln, mail ,pw1, pw2 ){
    var firstname = fn.value;
    var lastname = ln.value;
    var email = mail.value;
    var password = pw1.value;
    var url = 'https://vig-mini.ddns.net/user/new';

    var body = { 
        'firstname': firstname,
        'lastname': lastname,
        'e-mail': email,
        'password': password  
    }
    

    console.log("body:", body );
    //TODO: evtl. Check auf Eingabekritierien: z.b. PW min. 6 zeichen etc.
    if( checkfirstname(fn) && checklastname(ln) && 
        checkemail(mail) && checkpw(pw1) ){
        

        if( pw2.value != pw1.value){

            pw2.setCustomValidity('Passwörter müssen übereinstimmen');
            
        }else{

            pw2.setCustomValidity('');
            sendrequest(body, url);
            console.log("erfolg");
            
            
        }
        
    }

}

/**
 * Checks if the firstname has atleast 3 characters and it may only contain alphabets
 * 
 * @param {*} firstname the firstname of the user
 * @returns returns true if the firstname input is not empty
 */
function checkfirstname(firstname){
    var allowedChars = /[a-z]+[a-z]*([-][a-z]+[a-z])?/          
    if(firstname.value.length >= 3 && firstname.value.match(allowedChars)){
        return true;
    }else{
        firstname.setCustomValidity("Vorname muss min. 3 Buchstaben enthalten! (keine Sonderzeichen/Zahlen)")
        return false;
    }
}

/**
 * Checks if the lastname has atleast 3 characters and it may only contain alphabets
 * 
 * @param {*} lastname the lastname of the user
 * @returns returns true if the lastname input is not empty
 */
function checklastname(lastname){
    var allowedChars = /[a-z]+[a-z]*([-][a-z]+[a-z])?/
    if(lastname.value.length >= 3 && lastname.value.match(allowedChars)){
        return true;
    }else{
        lastname.setCustomValidity("Vorname muss min. 3 Buchstaben enthalten! (keine Sonderzeichen/Zahlen)");
        return false;
    }
}

/**
 * Checks if the email is empty or not
 * 
 * @param {*} email the e-mail adress of the user
 * @returns returns true if the e-mail adress input is not empty
 */
function checkemail(email){
    if(email.value == ''){
        return false;
    }else{return true;}
}

/**
 * Checks if the password has at least one upper case letter, at least one lower case letter, at least one digit, at least one special character
 * and minium of eight in length
 * 
 * @param {*} pw the password of the user
 * @returns returns true if the password input is not empty
 */
function checkpw(pw){
    var allowedChars = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/
    if(pw.value.match(allowedChars)){
        return true;
    }else{
        pw.setCustomValidity("Passwort muss min. 8 Zeichen lang sein und min. 1 Großbuchstaben, min. 1 Kleinbuchstaben und min 1 Sonderzeichen(#?!@$%^&*-)");
        return false;
    }
}


/**
 * Sends a fetch request to a certain URL with the body given by the weiterleiten function.
 * The body will be additionally encoded.
 * If the status of the response is 201 it will relocate the user to the Sign In page.
 * 
 * @param {*} body the body formed by the weiterleiten function
 * @param {*} url the url to send the fetch request to
 */
function sendrequest(body, url){
    var responseReal;
    console.log("starting request");
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

        if(responseReal == 201){
            setTimeout(function(){
                window.location = "./index.html";
            }, 1000);
            
        }else{
            alert("User existiert bereits!")
        }
    })

}