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