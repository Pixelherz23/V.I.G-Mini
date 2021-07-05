var TempChart = null;
var HumidChart = null;
var MoistChart = null;

/**
 * Function to draw Multiple Graphs into a specified canvas area.
 * To create the graphs the chart.js framework was used.
 * 
 * @param {*} data the json file which contains measurements
 * @param {*} chart the canvas area to draw the graph on
 * @param {*} flag number between 0 and 2. The value of the number indicates wether the Temp, Humid or Moisture Graph is drawn.
 * @param {*} anzeigeText the label text for the graph
 * @returns returns the created instance of the graph
 */
function createChartTemp(data,chart,flag,anzeigeText){
    let tempArr = [];
    let timeArr = [];
    let measurementArr = [];
    for(var measurement of data){
        switch(flag){
            case 0:
                tempArr.push(measurement.TEMPERATURE);
                measurementArr.push(measurement.TIME_STAMP); 
                break;
            case 1:
                tempArr.push(measurement.HUMIDITY);
                measurementArr.push(measurement.TIME_STAMP);  
                break;
            case 2:
                tempArr.push(measurement.SOIL_MOISTURE);
                measurementArr.push(measurement.TIME_STAMP);
                break;
        }  
    }

    let counter = 0;
    let stepSize = Math.ceil(tempArr.length / 10);

    for(var eintrag of tempArr){
        if(counter % stepSize == 0){ 
            let timeStamp = measurementArr[counter];
            let time = timeStamp.split(' ');
            let secondsplit = time[4].split(":");
            timeArr.push(time[1]+ " " +time[2]+ " "+secondsplit[0]+ ":" + secondsplit[1]);
        }else{
            let timeStamp = measurementArr[counter];
            let time = timeStamp.split(' ');
            let secondsplit = time[4].split(":");
            timeArr.push(time[1]+ " " +time[2]+ " "+secondsplit[0]+ ":" + secondsplit[1]);
        }
        counter++;
    }

    var ctx = chart.getContext('2d');
    var myChart = new Chart(ctx, {
    type: 'line',
        data: {
            labels: timeArr,
            datasets: [{
                label: anzeigeText,
                data: tempArr,
                pointBackgroundColor: "rgba(0,0,255, 1)",
                borderColor: "rgba(0,153,255, 1)"
            }]
        },
        options: {
            showXLabels: 5,
            responsive: false,
            
            scales: {
                y: {
                    beginAtZero: false
                },
                x: {
                    ticks: {
                        maxTicksLimit: 10,
                        showXLabels: 5,
                        }
                }
                
                
            }
        }
    });

    return myChart;
}



/**
 * Function to create a body and handle certain cases of the time parameter.
 * For each case a new url will be given to the sendrequest function
 * 
 * @param {*} tempChartName the DOM of the temperature graph
 * @param {*} humidChartName the DOM of the humidity graph
 * @param {*} moistChartname the DOM of the soil moisture graph
 * @param {*} time the value of the drop down menu DOM
 */
function login(tempChartName, humidChartName, moistChartname,time){
    console.log("time:", time);
    var pkey = localStorage.getItem("schluessel");

    switch(time){
        case "(24 Stunden)":
            var url = 'https://vig-mini.ddns.net/greenhouse/measurements/day';
            break;
        case "(1 Woche)":
            var url = 'https://vig-mini.ddns.net/greenhouse/measurements/week';
            break;
        case "(1 Monat)":
            var url = 'https://vig-mini.ddns.net/greenhouse/measurements/month';
            break;

    }

    var body = {
        'product_key': pkey,
    }
    sendrequest(body, url, tempChartName,humidChartName, moistChartname,time);
}


/**
 * Uses fetch to send a GET-Request to a specified url. Uses the response data to call multiple functions,
 * which will create graphs. Also deletes already existing charts to replace them with new ones if the
 * User chose another drop down entry.
 * 
 * @param {*} body 
 * @param {*} url 
 * @param {*} tempChartName 
 * @param {*} humidChartName 
 * @param {*} moistChartname 
 * @param {*} time 
 */
function sendrequest(body, url, tempChartName, humidChartName, moistChartname, time){
        
    console.log("starting request");
    var formBody = [];
    for (var property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    
    url = url + "?" + formBody;
    

    fetch(url, {
        method: 'GET',
        mode: "cors",
        headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
        },
        
    })
    .then(response => {
        if(response.status == 200){
            response.json().then(function(data){

                if(TempChart != null || HumidChart != null || MoistChart !=null){
                    TempChart.destroy();
                    HumidChart.destroy();
                    MoistChart.destroy();
                }
                console.log(data);
                TempChart = createChartTemp(data, tempChartName,0,"Temperatur"+ time+  ":");
                HumidChart =createChartTemp(data, humidChartName,1,"Luftfeuchtigkeit"+ time +":");
                MoistChart = createChartTemp(data, moistChartname,2,"Bodenfeuchtigkeit"+ time +":");
            });
        }else{
            alert("Abrufen der Daten Fehlgeschlagen!");
        }
    })
}

/**
 * Simple function to append the greenhouse name to the html file.
 * 
 * @param {*} div the DOM to append the greenhouse name to
 */
function loadGWName(div){
    var textappend = document.createTextNode(localStorage.getItem("greenhousename"))
    div.appendChild(textappend);
}