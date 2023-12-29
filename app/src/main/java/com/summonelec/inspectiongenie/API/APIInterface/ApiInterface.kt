package com.jose.myfoodcourt.api.APIInterface


import com.summonelec.inspectiongenie.API.APIInterface.POJO.LoginResult
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    @FormUrlEncoded
    @POST("login/")
    suspend fun loginUser(
        @Field("email") username: String?,
        @Field("password") password: String?
    ): Response<LoginResult>

//    @GET("api/get_category/{rest_id}/")
//    suspend fun getCategory(
//        @Header("Authorization") auth: String?,
//        @Path("rest_id") rest_id: String?
//    ): Response<List<CategoryListModel>>
//
//    @GET("api/get_product/{res_id}/{cat_id}/")
//    suspend fun getProducts(@Header("Authorization") auth: String?,
//                            @Path("res_id") id: Int,
//                            @Path("cat_id") cat_id: Int
//    ): Response<List<ProductsListModel>>
//
//    @GET("api/get_product/{pro_id}/")
//    suspend fun getSingleProducts(@Header("Authorization") auth: String?,
//                            @Path("pro_id") id: Int
//    ): Response<List<ProductsListModel>>
//
//
//
//    @FormUrlEncoded
//    @POST("api/order_create/")
//    suspend fun orderplace(@Header("Authorization") auth: String?,@Field("order") order: String): Response<OrderResult>
//
//    @GET("api/get_order/{res_id}/")
//    suspend fun getOrders(@Header("Authorization") auth: String?,
//                                  @Path("res_id") id: Int
//    ): Response<List<OrderModel>>
//
//    @GET("api/get_order_items/{order_id}/")
//    suspend fun get_order_items(@Header("Authorization") auth: String?,
//                          @Path("order_id") id: Int
//    ): Response<List<OrderItemModel>>
//
//    //kitchen
//
//    @GET("api/get_kitchen_order/{res_id}/{status}/")
//    suspend fun getKitchenOrders(@Header("Authorization") auth: String?,
//                          @Path("res_id") id: Int,
//                          @Path("status") status: String
//    ): Response<List<OrderModel>>
//
//    @FormUrlEncoded
//    @PUT("api/update_kitchen_order/{id}/")
//    suspend fun update_kitchen_order(@Header("Authorization") auth: String?, @Path("id") cat_id: Int,@Field("kitchen_status") kitchen_status: String): Response<OrderUpdateResult>
//
//    //admin
//    @FormUrlEncoded
//    @POST("api/userCreate/")
//    suspend fun create_user(@Header("Authorization") auth: String?,@Field("restarant_id") rest_id: Int?,@Field("username") username: String,@Field("user_type") user_type: String): Response<OrderUpdateResult>
//
//    @GET("api/getUsers/{rest_id}/")
//    suspend fun getUsers(
//        @Header("Authorization") auth: String?,
//        @Path("rest_id") rest_id: Int?
//    ): Response<List<UserModel>>
//
//
//    @GET("api/get_category/{rest_id}/")
//    suspend fun getAdminCategory(
//        @Header("Authorization") auth: String?,
//        @Path("rest_id") rest_id: String?
//    ): Response<List<CategoryModel>>
//
//    @GET("api/restarant/{rest_id}/")
//    suspend fun getrestarant(
//        @Header("Authorization") auth: String?,
//        @Path("rest_id") rest_id: String?
//    ): Response<List<RestrantModel>>
//
//
//    @Multipart
//    @PUT("api/update_category/{id}/")
//    suspend fun update_category(@Header("Authorization") auth: String?,
//                                     @Path("id") cat_id: Int,
//                                     @Part("name") name: RequestBody?,
//                                     @Part("is_active") is_active: RequestBody?,
//    ): Response<categoryResult>
//    @Multipart
//    @PUT("api/update_category/{id}/")
//    suspend fun update_image_category(@Header("Authorization") auth: String?,
//                                @Path("id") cat_id: Int,
//                                @Part("name") name: RequestBody?,
//                                @Part("is_active") is_active: RequestBody?,
//                                @Part image: MultipartBody.Part?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @PUT("api/update_category/{id}/")
//    suspend fun delete_category(@Header("Authorization") auth: String?,
//                                      @Path("id") cat_id: Int,
//                                      @Part("is_active") is_active: RequestBody?
//    ): Response<categoryResult>
//
//    @GET("api/get_all_product/{rest_id}/")
//    suspend fun getAllProducts(@Header("Authorization") auth: String?,
//                                  @Path("rest_id") id: Int
//    ): Response<List<ProductsListModel>>
//
//
//    @GET("api/get_product/{pro_id}/")
//    suspend fun getProduct(@Header("Authorization") auth: String?,
//                               @Path("pro_id") id: Int
//    ): Response<List<ProductsListModel>>
//
//    @GET("api/get_all_ingredient/{rest_id}/")
//    suspend fun getAllIngredient(@Header("Authorization") auth: String?,
//                               @Path("rest_id") id: Int
//    ): Response<List<IngredientModel>>
//
//    @GET("api/get_order/{rest_id}/")
//    suspend fun getAllorder(@Header("Authorization") auth: String?,
//                                 @Path("rest_id") id: Int
//    ): Response<List<OrdersModel>>
//
//
//    @Multipart
//    @POST("api/create_category/")
//    suspend fun create_category(@Header("Authorization") auth: String?,
//                                @Part("name") name: RequestBody?,
//                                @Part("restarant_id") restarant_id: RequestBody?,
//                                @Part("status") status: RequestBody?,
//                                @Part("is_active") is_active: RequestBody?,
//                                @Part("created_by") created_by: RequestBody?,
//                                @Part image: MultipartBody.Part?,
//                              ): Response<categoryResult>
//
//    @Multipart
//    @PUT("api/update_product/{id}/{restarant_id}/")
//    suspend fun update_image_product(@Header("Authorization") auth: String?,
//                                     @Path("id") pro_id: Int,
//                                     @Path("restarant_id") rest_id: Int,
//                                @Part("name") name: RequestBody?,
//                                @Part("description") description: RequestBody?,
//                                @Part("price") price: RequestBody?,
//                                @Part("category_id") category_id: RequestBody?,
//                                @Part("restarant_id") restarant_id: RequestBody?,
//                                @Part("is_active") is_active: RequestBody?,
//                                @Part("created_by") created_by: RequestBody?,
//                                @Part image: MultipartBody.Part?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @PUT("api/update_product/{id}/{restarant_id}/")
//    suspend fun update_product(@Header("Authorization") auth: String?,
//                                     @Path("id") pro_id: Int,
//                                     @Path("restarant_id") rest_id: Int,
//                                     @Part("name") name: RequestBody?,
//                                     @Part("description") description: RequestBody?,
//                                     @Part("price") price: RequestBody?,
//                                     @Part("category_id") category_id: RequestBody?,
//                                     @Part("restarant_id") restarant_id: RequestBody?,
//                                     @Part("is_active") is_active: RequestBody?,
//                                     @Part("created_by") created_by: RequestBody?
//    ): Response<categoryResult>
//    @Multipart
//    @PUT("api/update_product/{id}/{restarant_id}/")
//    suspend fun delete_product(@Header("Authorization") auth: String?,
//                               @Path("id") pro_id: Int,
//                               @Path("restarant_id") rest_id: Int,
//                               @Part("is_active") is_active: RequestBody?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @POST("api/create_product/")
//    suspend fun create_product(@Header("Authorization") auth: String?,
//                               @Part("name") name: RequestBody?,
//                               @Part("description") description: RequestBody?,
//                               @Part("price") price: RequestBody?,
//                               @Part("category_id") category_id: RequestBody?,
//                               @Part("restarant_id") restarant_id: RequestBody?,
//                               @Part("is_active") is_active: RequestBody?,
//                               @Part("created_by") created_by: RequestBody?,
//                               @Part image: MultipartBody.Part?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @POST("api/create_ingredient/")
//    suspend fun create_ingredient(@Header("Authorization") auth: String?,
//                                @Part("name") name: RequestBody?,
//                                @Part("restarant_id") restarant_id: RequestBody?,
//                                @Part("status") status: RequestBody?,
//                                @Part("is_active") is_active: RequestBody?,
//                                @Part("created_by") created_by: RequestBody?,
//                                @Part image: MultipartBody.Part?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @PUT("api/update_ingredient/{id}/")
//    suspend fun update_ingredient(@Header("Authorization") auth: String?,
//                                @Path("id") cat_id: Int,
//                                  @Part("name") name: RequestBody?,
//                                  @Part("is_active") is_active: RequestBody?,
//    ): Response<categoryResult>
//    @Multipart
//    @PUT("api/update_ingredient/{id}/")
//    suspend fun update_image_ingredient(@Header("Authorization") auth: String?,
//                                      @Path("id") cat_id: Int,
//                                      @Part("name") name: RequestBody?,
//                                        @Part("is_active") is_active: RequestBody?,
//                                      @Part image: MultipartBody.Part?,
//    ): Response<categoryResult>
//
//    @Multipart
//    @PUT("api/update_ingredient/{id}/")
//    suspend fun delete_ingredient(@Header("Authorization") auth: String?,
//                                @Path("id") cat_id: Int,
//                                @Part("is_active") is_active: RequestBody?
//    ): Response<categoryResult>
//
//
}