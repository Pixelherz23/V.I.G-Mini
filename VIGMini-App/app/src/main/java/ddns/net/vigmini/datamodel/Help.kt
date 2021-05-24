package ddns.net.vigmini.datamodel

class Help (headline: String, content: String) : Information(headline, content){
    companion object{
        fun createHelpList() : ArrayList<Help> {
            var list = arrayListOf(arrayListOf("Hilfe 1", "Lorem Ipsum"), arrayListOf("Hilfe 2", "Dies ist eine Hilfestellung"))
            var help = Information.createInformationList(list)
            return help as ArrayList<Help>
        }

    }
}
