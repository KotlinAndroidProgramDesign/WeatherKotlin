package android.vv.com.weatherkotlin.data

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService{

    companion object {
        const val BASE_URL = "https://api.apixu.com/v1/"
    }

    /**
     * 搜索城市
     * https://api.apixu.com/v1/search.json?key=2f218cdf94354d489b380732182607&lang=zh&q=beijing
     *
     * 历史天气
     * https://api.apixu.com/v1/history.json?key=2f218cdf94354d489b380732182607&lang=zh&q=beijing&dt=2018-06-02
     *
     * 天气预报
     * https://api.apixu.com/v1/forecast.json?key=2f218cdf94354d489b380732182607&lang=zh&q=beijing&days=1
     *
     * 实时天气
     * https://api.apixu.com/v1/current.json?key=2f218cdf94354d489b380732182607&lang=zh&q=beijing
     */


    @GET("current.json")
    fun getWeatherCurrent(@Query("q") cityName: String): Observable<CurrentResult>

    @GET("forecast.json")
    fun getWeatherForecast(@Query("q") cityName: String, @Query("days") day: Int = 7): Observable<ForecastResult>

    @GET("history.json")
    fun getWeatherHistory(@Query("q") cityName: String, @Query("dt") dt: String): Observable<HistoryWeatherResult>

    @GET("search.json")
    fun getWeatherSearch(@Query("q") cityName: String): Observable<List<SearchEntity>>

}