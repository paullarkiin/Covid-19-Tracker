package net.paullarkin.covidtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val TAG = "MainActivity" // Tag convent for this is generally always the class name.
class MainActivity : AppCompatActivity() {

    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        val covidService = retrofit.create(CovidService::class.java) // creates an instance of covid service.
        //Fetch the national data
        covidService.getNationalData().enqueue(object: Callback<List<CovidData>>{
            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG, "onResponse $response")
                val nationalData = response.body()
                if(nationalData == null){
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }
                // new concept: creating a member on MainActivity, as lateinit.
                // saves the national data into member variable, which is accessible through out the whole activity class.
                // creates this as a property on MainActivity
                nationalDailyData = nationalData.reversed() // reverse the list of data returned for graphing.
                Log.i(TAG, "Update graph with national data")
                updateDisplayWithData(nationalDailyData)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
              Log.e(TAG, "onFailure $t")
            }

        })
        // Fetch the state data
        covidService.getStatesData().enqueue(object: Callback<List<CovidData>>{
            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG, "onResponse $response")
                val stateData = response.body()
                if(stateData == null) {
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }
                perStateDailyData = stateData.reversed().groupBy { it.state }

                Log.i(TAG, "Update spinner with state names")
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }
        })
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        // create a new sparkAdapter with the data
        val adapter = CovidSparkAdapter(dailyData)
        sparkView.adapter = adapter

        // update radio buttons
        // call by id updated by fixing kotlin gradle version and adding the 'kotlin-android-extensions' plugin to build.gradle.
        radioButtonPositive.isChecked = true
        radioButtonMax.isChecked = true

        //display metric for most recent date
        updateInfoForDate(dailyData.last()) // this works because the data previously ordered.
    }

    private fun updateInfoForDate(covidData: CovidData) {

        tvMetricLabel.text = NumberFormat.getInstance().format(covidData.positiveIncrease)
        val outputDataFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        tvDateLabel.text = outputDataFormat.format(covidData.dateChecked)

    }
}