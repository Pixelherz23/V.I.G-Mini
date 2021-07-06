package ddns.net.vigmini.data.access

import ddns.net.vigmini.adapter.GreenhouseSettingsAdapter
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

    @FormUrlEncoded
    @POST("/greenhouse/settings/update")
    fun updateSettingsTemp(@Field("product_key") productKey: String , @Field("max_temp") maxTemp: Int,
                           @Field("temp_on") tempOn: Int): Call<Void>

    @FormUrlEncoded
    @POST("/greenhouse/settings/update")
    fun updateSettingsSoilMoisture(@Field("product_key") productKey: String , @Field("min_soil_moisture") minSoilMoisture: Int,
                                   @Field("soil_moisture_on") soilMoistureOn: Int): Call<Void>

    @FormUrlEncoded
    @POST("/greenhouse/settings/update")
    fun updateSettingsSoilMoistureTime(@Field("product_key") productKey: String , @Field("from_tome") fromTime: String,
                                       @Field("timetable_on") timetableOn: Int, @Field("timetable_type") timetableType: String,
                                       @Field("interval") interval: Int): Call<Void>

    @FormUrlEncoded
    @POST("/greenhouse/settings/update")
    fun updateSettingsLight(@Field("product_key") productKey: String , @Field("from_tome") fromTime: String,
                                       @Field("to_tome") toTime: String, @Field("timetable_on") timetableOn: Int,
                                       @Field("timetable_type") timetableType: String,
                                       @Field("interval") interval: Int): Call<Void>

    @FormUrlEncoded
    @POST("/user/new")
    fun newUser(@Field("firstname") firstname: String , @Field("lastname") lastname: String,
                 @Field("e-mail") email: String, @Field("password") password: String): Call<Void>

    @FormUrlEncoded
    @POST("/user/login")
    fun userLogin(@Field("e-mail") email: String, @Field("password") password: String): Call<Void>

    @GET("/user/information")
    fun getUser(@Query("e-mail") email: String): Call<ArrayList<ArrayList<String>>>

    @FormUrlEncoded
    @POST("/user/information/update")
    fun updateUser(@Field("firstname") firstname: String , @Field("lastname") lastname: String, @Field("e-mail") email: String): Call<Void>

    @FormUrlEncoded
    @POST("/user/information/update")
    fun updateUser(@Field("firstname") firstname: String, @Field("lastname") lastname: String,
                   @Field("e-mail") email: String, @Field("new_e-mail") newEmail: String): Call<Void>

    @FormUrlEncoded
    @POST("/user/information/update")
    fun updateUser(@Field("firstname") firstname: String, @Field("lastname") lastname: String,
                   @Field("e-mail") email: String, @Field("new_e-mail") newEmail: String,
                   @Field("password") password: String, @Field("new_password") newPassword: String): Call<Void>

    @FormUrlEncoded
    @POST("/user/information/update")
    fun updateUser(@Field("firstname") firstname: String, @Field("lastname") lastname: String,
                   @Field("e-mail") email: String, @Field("password") password: String,
                   @Field("new_password") newPassword: String): Call<Void>


    @GET("/greenhouse/activate")
    fun registerGreenhouse(@Query("product_key") productKey: String, @Query("e-mail") email: String): Call<Void>
}