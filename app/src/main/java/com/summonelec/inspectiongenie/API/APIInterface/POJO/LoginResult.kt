package com.summonelec.inspectiongenie.API.APIInterface.POJO

import com.google.gson.annotations.SerializedName


data class LoginResult(
    @SerializedName("code")
     var code: String? = null,
     @SerializedName("user_id")
     var userId: Int? = null,
     @SerializedName("name")
     var name: String? = null,
     @SerializedName("email")
     var email: String? = null,
     @SerializedName("user_image")
     var user_image: String? = null,
     @SerializedName("message")
     var message: String? = null)