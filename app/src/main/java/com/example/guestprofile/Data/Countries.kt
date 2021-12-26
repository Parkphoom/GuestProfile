package com.example.guestprofile.Data

import com.opencsv.bean.CsvBindByPosition

data class Countries(
    @CsvBindByPosition(position = 0)
    val CCA2: String,
    @CsvBindByPosition(position = 1)
    val Name: String,
    @CsvBindByPosition(position = 2)
    val CCA3: String,
    @CsvBindByPosition(position = 3)
    val Nationality: String
)