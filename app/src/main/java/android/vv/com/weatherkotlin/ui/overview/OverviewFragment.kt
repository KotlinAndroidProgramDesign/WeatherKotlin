package android.vv.com.weatherkotlin.ui.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.vv.com.weatherkotlin.R
import android.vv.com.weatherkotlin.util.extensions.DelegatesExt
import android.vv.com.weatherkotlin.data.CityEvent
import android.vv.com.weatherkotlin.data.UpdateForecastEvent
import android.vv.com.weatherkotlin.data.ForecastResult
import android.vv.com.weatherkotlin.data.HttpManager
import android.vv.com.weatherkotlin.data.WeatherObserver
import android.vv.com.weatherkotlin.util.*
import android.vv.com.weatherkotlin.ui.city.CityActivity
import android.vv.com.weatherkotlin.util.UiThreadHandler
import android.vv.com.weatherkotlin.util.rxbus.busPostSticky
import android.vv.com.weatherkotlin.util.rxbus.busRemoveStickyEvent
import android.vv.com.weatherkotlin.util.rxbus.busToObservableSticky
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.trello.rxlifecycle2.components.support.RxFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_overview.*
import org.jetbrains.anko.support.v4.toast
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class OverviewFragment : RxFragment() {

    companion object Factory {
        fun newInstance(): OverviewFragment {
            return OverviewFragment()
        }

        const val SP_KEY_UPDATE_TIME = "update_time"
        const val SP_VLAUE_DEFAULT_UPDATE_TIME = -1L

        const val SP_KEY_FORECAST = "last_forecast"
        const val SP_VLAUE_DEFAULT_FORECAST = "{}"

        //最小刷新间隔15分钟
        var MIN_REFRESH_INTERVAL: Long = 15 * 60 * 1000
    }


    var spCityName: String by DelegatesExt.preference(CityActivity.SP_KEY_CITY_NAME, CityActivity.SP_VLAUE_DEFAULT_NAME)
    var spUpdateTime: Long by DelegatesExt.preference(SP_KEY_UPDATE_TIME, SP_VLAUE_DEFAULT_UPDATE_TIME)
    var spLastForecast: String by DelegatesExt.preference(SP_KEY_FORECAST, SP_VLAUE_DEFAULT_FORECAST)
    var gson: Gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_overview)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requestWeather(spCityName)

        //注册修改城市的通知, 来重新拉取数据
        busToObservableSticky(CityEvent::class.java)
                .compose(this.bindToLifecycle())
                .subscribe {
            requestWeather(it.cityName, true)
        }


        srlOverview.apply {
            setColorSchemeResources(R.color.holo_purple, R.color.holo_blue_light,
                    R.color.holo_green_light, R.color.holo_orange_light)
            setOnRefreshListener {
                requestWeather(spCityName)
            }
        }



        try {
            val lfr = gson.fromJson(spLastForecast, ForecastResult::class.java)
            if (lfr.current != null) {
                loadData(lfr)
                DelegatesExt.forecastResult = lfr
            }
        } catch (e: JsonSyntaxException) {
//        throw Throwable("格式化出错")
        }

        activity!!.runOnUiThread {
            //输出格式为｛15：52 星期一 12/06｝
            val dateFormat = DateTimeFormat.forPattern("HH:mm E dd/MM ")
                    .withLocale(Locale.CHINA)
            tvCurrentTime.text = dateFormat.print(LocalDateTime())
        }

    }


    private fun requestWeather(cityName: String, isForced: Boolean = false) {

        //是否跳过15分钟内刷新一次的判断
        if (!isForced) {
            val currentTime: Long = Date().time
            if (currentTime - spUpdateTime < MIN_REFRESH_INTERVAL) {
                UiThreadHandler.postDelayed(Runnable { srlOverview.isRefreshing = false },1000)
                return;
            }
        }

        val weatherService = HttpManager.getInstance().weatherService
        weatherService.getWeatherForecast(cityName)
                .compose(this.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    srlOverview.isRefreshing = true
                }
                .doFinally {
                    srlOverview.isRefreshing = false
                }
                .subscribe(object : WeatherObserver<ForecastResult> {
                    override fun onError(e: Throwable) {

                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onWeatherError(message: String) {
                        toast(message)
                    }
                    override fun onWeatherNext(fr: ForecastResult) {

                        spUpdateTime = Date().time
                        tvLastUpdateTime.text = getTimeFormatText(spUpdateTime)

                        spLastForecast = gson.toJson(fr)
                        DelegatesExt.forecastResult = fr

                        busPostSticky(UpdateForecastEvent(fr))

                        loadData(fr)

                    }
                })

    }

    @SuppressLint("SetTextI18n")
    fun loadData(fr: ForecastResult) {

        tvMaxTemp.text = "${fr.forecast.forecastday[0].day.maxtemp_c}°"
        tvMinTemp.text = "${fr.forecast.forecastday[0].day.mintemp_c}°"
        with(fr.current) {

            tvCondition.text = condition.text
            ivCondition.loadImg(condition.icon)

            tvTemp.text = temp_c.toTempByCelsius()
            tvFeelLike.text = feelslike_c.toTempByCelsius()

            tvCloud.text = cloud.toPercent()
            tvWind.text = wind_dir.toWindDir() + wind_kph.toKph()
            tvHumidty.text = humidity.toPercent()
            tvVisibility.text = vis_km.toKm()
            tvPressure.text = "${pressure_mb}mbar"

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        busRemoveStickyEvent(CityEvent::class.java)
    }

    override fun onResume() {
        super.onResume()
        tvLastUpdateTime.text = getTimeFormatText(spUpdateTime)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            tvLastUpdateTime.text = getTimeFormatText(spUpdateTime)
        }
    }


    /**
     * 返回文字描述的日期
     * @param dates
     * @return
     */
    fun getTimeFormatText(dates: Long): String {
        if (spUpdateTime == SP_VLAUE_DEFAULT_UPDATE_TIME) {
            return ""
        }

        val difference = Date().time - dates
        val minutes = (difference / (1000 * 60)).toInt()

        return when {
            minutes < 1 -> "最近更新:刚刚"
            minutes < 60 -> "最近更新:$minutes 分钟前"
            minutes < 60 * 24 -> "最近更新:${minutes / 60}小时前"
            minutes < 60 * 24 * 30 -> "最近更新:${minutes / (60 * 24)}天前"
            minutes < 60 * 24 * 30 * 30 -> "最近更新:${minutes / (60 * 24 * 30)} 月前"
            else -> "老长时间了"
        }
    }


}

