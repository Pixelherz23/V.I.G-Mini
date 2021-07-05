
/**
 * Redirects the user to the details page if the greenhousename was clicked
 * 
 * @param {*} div the DOM which has been clicked
 */
function redirectDetails(div){
    localStorage.setItem("schluessel", div.id);
    localStorage.setItem("greenhousename", div.innerHTML);
    window.location = "./Details.html"
}

/**
 * Redirects the User to the greenhouse option page if the svg was clicked
 * 
 * @param {*} div the DOM which has been clicked
 */
function redirectOptions(div){
    localStorage.setItem("schluessel", div.id);
    localStorage.setItem("greenhousenameoptions", div.name);
    window.location = "./GW-Einstellungen.html"
}


/**
 * Uses fetch to send a GET-Request to a specified url.
 * The user will get notified if the new greenhouse has been added successfully.
 * 
 * @param {*} body the body given by loginNewGW
 * @param {*} url the url given by loginNewGW
 */
function sendrequestNewGW(body, url){
            
    console.log("starting request");
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
        if(response.status == 201){
            alert("erfolgreich hinzugefügt!");
            setTimeout(function(){
                window.location = "Uebersicht.html" 
            }, 1000);
        }else{
            alert("Fehlerhafter Produktkey");
        }
    })
}

/**
 * Uses fetch to send a GET-request and will create and load values into DOM's.
 * 
 * @param {*} body the body given by loginLoad
 * @param {*} url the url given by loginLoad
 */
async function sendrequestLoad(body, url){
            
    console.log("starting request");
    var formBody = [];
    for (var property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    
    url = url + "?" + formBody;
    console.log("body joined, body:", formBody);
    

    var response = await fetch(url, {
        method: 'GET',
        mode: "cors",
        headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
        },
        
    });

    
    if(response.status == 200){
        console.log("first request ok");
        var data = await response.json();
        
        console.log(data)
        for(var gw of data){
            GWLaden(gw);
            var pkey = gw.PRODUCT_KEY;
            var url2 = "https://vig-mini.ddns.net/greenhouse/measurements/now";
            url2 = url2 + "?product_key=" + pkey;
            var response2 = await fetch(url2, {
                method: 'GET',
                mode: "cors",
                headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
                },
                
            });
            if(response2.status == 200){
                console.log("second request ok");
                var innerData = await response2.json();
                console.log("key:", pkey, "data:", innerData);
                LoadGWData(gw.NAME, innerData);
            } else {
                console.log(`second request not ok ${response2.status}`);
            }
        };
        
    }else{
        alert("Fehler beim Verbinden mit der Datenbank");
    }
}

/**
 * Uses fetch to send a GET-Request to a specified url.
 */
function sendRequestFirstname(){
    var email = localStorage.getItem("email");
    var url = 'https://vig-mini.ddns.net/user/information/first-name';
    console.log("email: ", email);
    var body = {
        'e-mail': email,
    }

    console.log("starting request FN");
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
    .then(response =>{
        if(response.status == 200){
            response.text().then(function(data) {
                console.log(data);
                localStorage.setItem("showfirstname", data);
            });
        }
    })
}

