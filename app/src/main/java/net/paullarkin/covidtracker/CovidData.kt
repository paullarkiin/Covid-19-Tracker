package net.paullarkin.covidtracker

import com.google.gson.annotations.SerializedName
import java.util.*

//covidtracking.com
data class CovidData(
   // @SerializedName("dateChecked") val dateChecked: String,
     val dateChecked: Date,
     val positiveIncrease: Int,
     val negativeIncrease: Int,
     val deathIncrease: Int,
     val state: String
    )
// if variable matches the name of the param then you can remove the  @SerializedName("dateChecked")
