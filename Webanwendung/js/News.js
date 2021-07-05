/**
 * Uses fetch to send a GET-Request to the given url and adds the response to the actual html.
 * 
 * @param {*} div the wrapper to put in the news content
 */

function sendrequestNews(div){
    fetch('https://vig-mini.ddns.net/information/news', {
        method: 'GET',
        mode: "cors",
        headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
        },
        
    })
    .then(response => {
        if(response.status == 200){
            response.json().then(function(data) {
                for(var headline of data){
                    /*<section>
                        <h1>Überschrift:</h1>
                        <article>
                        </article>
                    </section>*/
                    var section = document.createElement("section");
                    div.appendChild(section);

                    var überschrift = document.createElement("h1");
                    überschrift.setAttribute("class","headline");
                    var überschriftText = document.createTextNode(headline.HEADLINE);
                    überschrift.appendChild(überschriftText);
                    section.appendChild(überschrift);

                    var article = document.createElement("article");
                    article.setAttribute("class", "content");
                    var articleText = document.createTextNode(headline.CONTENT);
                    article.appendChild(articleText);
                    div.appendChild(article);
                }
                console.log(data);
            });
        }else{
            alert("Fehler");
        }
    })
}