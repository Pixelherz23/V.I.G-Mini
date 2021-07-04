var intervallNumber;
localStorage.setItem("moistvalue", "true");

/**
 * function to form a body and call sendrequestTemp
 * 
 * @param {*} tempswitch the DOM of the on/off temperature switch
 * @param {*} tempform the DOM of the temp value
 */
function loginTemp(tempswitch, tempform){
    var pkey = localStorage.getItem("schluessel"); // TODO: aus local storage?
    var url = 'https://vig-mini.ddns.net/greenhouse/settings/temperature';

    var body = {
        'product_key': pkey  
    }
    sendrequestTemp(body, url,tempswitch, tempform);
}

/**
 * Uses fetch to send a GET-Request to a specified url and puts the response 
 * into the temperature form/checks or unchecks the on/off Button.
 * The body will be additionally encoded.
 * 
 * @param {*} body the body formed by loginTemp
 * @param {*} url the url given by loginTemp
 * @param {*} tempswitch the DOM of the on/off temperature switch
 * @param {*} tempform the DOM of the temp value
 */
function sendrequestTemp(body, url,tempswitch, tempform){
        
    console.log("starting request Temp");
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
        if(response.status == 200){
            response.json().then(function(data) {
                    console.log(data);
                    tempform.value = data.MAX_TEMPERATURE;
                    if(data.TEMP_ON == 1){
                        tempswitch.setAttribute("checked", null);
                    }else if(tempswitch.getAttribute("checked")!= null){
                        tempswitch.removeAttribute("checked")
                    }
            });
        }else{
            alert("Fehler");
        }
    })
}


/**
 * Function to form a body and call sendrequestLight.
 * 
 * @param {*} lightswitch the DOM of the on/off light switch
 * @param {*} from the DOM of the from value
 * @param {*} to the DOM of the to value
 */
function loginLight(lightswitch,from,to){
    var pkey = localStorage.getItem("schluessel"); // TODO: aus local storage?
    var url = 'https://vig-mini.ddns.net/greenhouse/settings/light';

    var body = {
        'product_key': pkey  
    }
    sendrequestLight(body, url, lightswitch, from, to);
}


/**
 * Uses fetch to send a GET-Request to a specified url and puts the response 
 * into the temperature form/checks or unchecks the on/off Button.
 * The body will be additionally encoded.
 * 
 * @param {*} body the body formed by loginLight
 * @param {*} url the url given by loginLight
 * @param {*} lightswitch the DOM of the on/off light switch
 * @param {*} from the DOM of the from value
 * @param {*} to the DOM of the to value
 */
function sendrequestLight(body, url,lightswitch,from,to){
    
    console.log("starting request Light");
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
        if(response.status == 200){
            response.json().then(function(data) {
                for(var eintrag of data){

                    console.log(data);
                    from.value = eintrag.FROM_TIME;
                    to.value = eintrag.TO_TIME;

                    if(eintrag.TIMETABLE_ON == 1){
                        lightswitch.setAttribute("checked", null);
                    }else if(lightswitch.getAttribute("checked")!= null){
                        lightswitch.removeAttribute("checked")
                    }
                    break;
                }
            });
        }else{
            alert("Fehler");
        }
    })
}

/**
 * Function to form a body and call sendrequestMoist.
 * 
 * @param {*} div the wrapper DOM to put the future content in
 * @param {*} werteswitch the DOM of the values/timer switch for the soil moisture 
 */
function loginMoist(div, werteswitch){
    var pkey = localStorage.getItem("schluessel"); // TODO: aus local storage?
    var url = 'https://vig-mini.ddns.net/greenhouse/settings';

    var body = {
        'product_key': pkey  
    }

    sendrequestMoist(body, url,div, werteswitch );
}

/**
 * Uses fetch to send a GET-Request to a specified url and Iterates the response json
 * to create a value based form or a time based form.
 * The body will be additionally encoded.
 * 
 * @param {*} body the body formed by loginMoist
 * @param {*} url the url given by loginMoist
 * @param {*} div the wrapper DOM to put the created content inside
 * @param {*} werteswitch the DOM of the values/timer switch
 */
function sendrequestMoist(body,url, div , werteswitch){
    console.log("starting request Moist");
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
        if(response.status == 200){
            response.json().then(function(data) {
                for(var intervall of data){
        

                    if(intervall.PRODUCT_KEY == localStorage.getItem("schluessel") && intervall.TIMETABLE_TYPE == "soilMoisture"){
                
                        if(intervall.TIMETABLE_ON == 1){
                            intervallNumber = intervall.INTERVAL_TIME;
                            console.log("intervall:", intervall)
                            localStorage.setItem("intervalnumber", intervallNumber);
                            createMoistIntervall(div);
                            localStorage.setItem("moistvalue", "false")

                            break;
                 

                        }else{
                            intervallNumber = 1;
                            localStorage.setItem("intervalnumber", intervallNumber);
                            createMoistValue(div);
                            werteswitch.setAttribute("checked", null);
                            break;
                        }
                    
                    }
                }
            });
        }else{
            alert("Fehler");
        }
    })
}


/**
 * Function to create a time based form
 * 
 * @param {*} div the wrapper DOM to store the content in
 */
function createMoistIntervall(div){
    
    //<label for="bfrom">von:</label>
    var fromLabel = document.createElement("label");
    fromLabel.setAttribute("for", "bfrom");
    var fromLabelText = document.createTextNode("von:");
    fromLabel.appendChild(fromLabelText);
    div.appendChild(fromLabel);

    //<input required class="boxdesign" type="text" id="bfrom" name="bfrom"></input>
    var input = document.createElement("input");
    input.setAttribute("required", null);
    input.setAttribute("class", "boxdesign");
    input.setAttribute("type", "text");
    input.setAttribute("id", "bfrom");
    input.setAttribute("name", "bfrom");
    div.appendChild(input);
}

/**
 * Function to create a value based form
 * 
 * @param {*} div the wrapper DOM to store the content in
 */
function createMoistValue(div){

    //<label for="BFeuchtigkeit">min:</label>
    var label = document.createElement("label");
    label.setAttribute("for", "BFeuchtigkeit");
    var labelText = document.createTextNode("min:");
    label.appendChild(labelText);
    div.appendChild(label);

    //<input required class="boxdesign" type="text" id="BFeuchtigkeit" name="BFeuchtigkeit">
    var input = document.createElement("input");
    input.setAttribute("required", null);
    input.setAttribute("class", "boxdesign");
    input.setAttribute("type", "text");
    input.setAttribute("id", "BFeuchtigkeit");
    input.setAttribute("name", "BFeuchtigkeit");
    div.appendChild(input);

    //% 
    var divText = document.createTextNode("%");
    div.appendChild(divText);

}

/**
 * Function to form a body for sendrequestMoistValueTrue
 * 
 * @param {*} div the DOM to store the future content in
 * @param {*} soilswitch the DOM of the on/off soil moisture switch
 */
function loginMoistValueTrue(div,soilswitch){
    console.log("div:" , div)
    var pkey = localStorage.getItem("schluessel"); // TODO: aus local storage?

    var url = 'https://vig-mini.ddns.net/greenhouse/settings/soil-moisture/value';
    var body = {
        'product_key': pkey  
    }
    sendrequestMoistValueTrue(body, url,div, soilswitch);
    
}

/**
 * Uses fetch to create a GET-Request to store the response into the specified DOM.
 * 
 * @param {*} body the body formed by loginMoistValueTrue
 * @param {*} url the url given by loginMoistValueTrue
 * @param {*} div the DOM to store the future content in
 * @param {*} soilswitch the DOM of the on/off soil moisture switch
 */
function sendrequestMoistValueTrue(body,url, div, soilswitch){
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
        if(response.status == 200){
            response.json().then(function(data) {
                console.log(data);
                div.value = data.MIN_SOIL_MOISTURE;

                if(data.SOIL_MOISTURE_ON == 1){
                    soilswitch.setAttribute("checked", null);
                }else if(soilswitch.getAttribute("checked")!= null){
                    soilswitch.removeAttribute("checked")
                }
            });
        }else{
            alert("Fehler");
        }
    })
}

/**
 * Function to form a body for sendrequestMoistValueTrue
 * 
 * @param {*} bfrom the DOM to store the future content inside
 * @param {*} soilswitch the DOM of the on/off soil moisture switch
 */
function loginMoistValueFalse(bfrom, soilswitch){
    var pkey = localStorage.getItem("schluessel"); // TODO: aus local storage?
    var intervall = 1;

    var url = 'https://vig-mini.ddns.net/greenhouse/settings/soil-moisture/time';
    var body = {
        'product_key': pkey,
        'interval': intervall
    }
    sendrequestMoistValueFalse(body, url,bfrom, soilswitch);
}


/**
 * Uses fetch to create a GET-Request to store the response into the specified DOM.
 * 
 * @param {*} body the body formed by loginMoistValueTrue
 * @param {*} url the url given by loginMoistValueTrue
 * @param {*} bfrom the DOM to store the future content inside
 * @param {*} soilswitch the DOM of the on/off soil moisture switch
 */
function sendrequestMoistValueFalse(body,url, bfrom, soilswitch){
    var formBody = [];
    for (var property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    formBody = formBody.join("&");
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
        if(response.status == 200){
            response.json().then(function(data) {
                console.log(data);
                bfrom.value = data.FROM_TIME;
                
                if(data.SOIL_MOISTURE_ON == 1){
                    soilswitch.setAttribute("checked", null);
                }else if(soilswitch.getAttribute("checked")!= null){
                    soilswitch.removeAttribute("checked")
                }
            });
        }else{
            alert("Fehler");
        }
    })
}

/**
 * Function to set and remove the "checked"-Attribute of a specific toggle button when clicked.
 * 
 * @param {*} event 
 * @param {*} switches the DOM of the specific toggle button
 * @returns returns true if the "checked"-Attribute was not set before
 */
function updateChecked(event, switches){

    if (switches.getAttribute("checked") != null) {
        switches.removeAttribute("checked");
        console.log("Uhrzeit/off");
        return false;
    }else{
        switches.setAttribute("checked",null);
        console.log("Werte/on");
        return true;
    }
}

/**
 * Removes the childs of a DOM and calls createMoistIntervall to replace the removed childs.
 * 
 * @param {*} bform the DOM which will have all childs removed
 */
function replaceWithIntervall(bform){
    while(bform.firstChild){
        bform.removeChild(bform.lastChild);
    }

    createMoistIntervall(bform);

}

/**
 * Removes the childs of a DOM and calls createMoistValue to replace the removed childs.
 * 
 * @param {*} bform the DOM which will have all childs removed
 */
function replaceWithValue(bform){
    while(bform.firstChild){
        bform.removeChild(bform.lastChild);
    }

    createMoistValue(bform);
}

/**
 * Function to append the greenhouse name to given DOM
 * 
 * @param {*} div the DOM to append the greenhouse name to
 */
function loadGWName(div){
    var texttoappend = document.createTextNode(localStorage.getItem("greenhousenameoptions"));
    div.appendChild(texttoappend);

}

/**
 * Checks if the DOM value has only digits between 0 and 100
 * 
 * @param {*} moistdom the DOM which is about to be checked
 * @returns returns true if the DOM value matches the regex
 */
function checkMoistValue(moistdom){
    var regex = /^(?!0+(?:\.0+)?$)\d?\d(?:\.\d\d?)?$|^0$|^100$/
    console.log(moistdom.value)
    if(moistdom.value.match(regex)){
        return true;
    }else{
        moistdom.setCustomValidity("Bitte Werte zwischen 0(.01) und 100 %")
        return false;
    }
}

/**
 * Checks if the DOM value has only digits between 20 and 40
 * 
 * @param {*} tempdom the DOM which is about to be checked
 * @returns returns true if the DOM value matches the regex
 */
function checkTemp(tempdom){
    var regex = /^[2-3]\d|^40$/
    if(tempdom.value.match(regex)){
        return true;
    }else{
        tempdom.setCustomValidity("Bitte Werte zwischen 20 und 40 °C")
        return false;
    }
}

/**
 * Checks the DOM value for this pattern: HH:MM:SS
 * 
 * @param {*} timedom the DOM which is about to be checked
 */
function checkTime(timedom){
    var regex = /^(((([0-1]?[0-9])|(2[0-3])):?[0-5][0-9]:?[0-5][0-9]+$))/
    if(timedom.value.match(regex)){
        return true;
    }else{
        timedom.setCustomValidity("Bitte im Format (H)H:MM:SS (0:00:00 - 23:59:59)")
        return false;
    }
}