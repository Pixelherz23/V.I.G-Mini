package ddns.net.vigmini.datamodel

open class Information (val headline: String, val content: String){
    companion object{
        fun createInformationList(infos: ArrayList<ArrayList<String>>) : ArrayList<Information> {
            val information = ArrayList<Information>()

            for(i in infos.indices){
                information.add(Information(infos[i][0], infos[i][1]))
            }
            return information
        }
    }
}
