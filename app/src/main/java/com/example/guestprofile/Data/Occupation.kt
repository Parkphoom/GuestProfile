package com.example.guestprofile.Data

import com.google.gson.annotations.SerializedName

class Occupation(
    id: String,
    occupation: String
) {
    @SerializedName("id")
    val id: String = ""

    @SerializedName("occupation")
    val occupation: String = ""
}