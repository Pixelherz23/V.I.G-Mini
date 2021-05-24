package ddns.net.vigmini.datamodel


class News (headline: String, content: String) : Information(headline, content){


    companion object{
        fun createNewsList() : ArrayList<News>{
            var list = arrayListOf(arrayListOf("News 1", "Lorem Ipsum"), arrayListOf("News 2", "Dies ist eine News"))
            var news = createInformationList(list)
            return news as ArrayList<News>
        }
    }



}

