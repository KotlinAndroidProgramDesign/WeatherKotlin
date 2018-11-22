package android.vv.com.weatherkotlin.data

data class CityEvent(var cityName:String)

data class UpdateForecastEvent(var fr: ForecastResult)