package android.vv.com.weatherkotlin.ui.hourly

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.vv.com.weatherkotlin.R
import android.vv.com.weatherkotlin.data.*
import android.vv.com.weatherkotlin.util.extensions.DelegatesExt
import android.vv.com.weatherkotlin.ui.city.CityActivity
import android.vv.com.weatherkotlin.util.rxbus.busRemoveStickyEvent
import com.google.gson.Gson
import com.trello.rxlifecycle2.components.support.RxFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily.*
import org.jetbrains.anko.support.v4.toast


class HourFragment : RxFragment() {

    private lateinit var  mAdapter : ForecastHourAdapter
    private var gson: Gson = Gson()
    var spCityName: String by DelegatesExt.preference(CityActivity.SP_KEY_CITY_NAME, CityActivity.SP_VLAUE_DEFAULT_NAME)

    companion object Factory {
        fun newInstance(): HourFragment {
            return HourFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hour, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        requestWeather("2018-07-22")
        mAdapter = ForecastHourAdapter()

        recyclerView.apply {
            setHasFixedSize(true)
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = mAdapter
        }
    }

    private fun requestWeather(date: String) {
        val weatherService = HttpManager.getInstance().weatherService
        weatherService.getWeatherHistory(spCityName, date)
                .compose(this.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {

                }
                .doFinally {

                }
                .subscribe(object : WeatherObserver<HistoryWeatherResult> {
                    override fun onError(e: Throwable) {
                        toast("请求数据失败")
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onWeatherError(message: String) {
                        toast(message)
                    }
                    override fun onWeatherNext(result: HistoryWeatherResult) {
                        loadData(result)
                    }
                })
    }



    private fun loadData(fr: HistoryWeatherResult?) {
        if (fr == null) {
            return
        }
        mAdapter.addData(fr.forecast.forecastday[0].hour)
    }

    override fun onDestroy() {
        super.onDestroy()
        busRemoveStickyEvent(UpdateForecastEvent::class.java)
    }
}