package ddns.net.vigmini.data.access

import ddns.net.vigmini.data.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiRequests {

    @GET("/information/{info}")
    fun getInformation(@Path("info") info: String): Call<Information>

    @GET("/greenhouse/all")
    fun getGreenhouses(@Query("e-mail") email: String): Call<Greenhouse>

    @GET("/greenhouse/measurements/now")
    fun getMeasurementsNow(@Query("product_key") productKey: String): Call<MeasurementsItem>

    @GET("/greenhouse/measurements/{interval}")
    fun  getMeasurements(@Path("interval") interval: String, @Query("product_key") productKey: String): Call<Measurements>

    @GET("/greenhouse/settings/{setting}")
    fun getGreenhouseSettings(@Path("setting") setting: String, @Query("product_key") productKey: String): Call<GreenhouseSettingsSubList>
}