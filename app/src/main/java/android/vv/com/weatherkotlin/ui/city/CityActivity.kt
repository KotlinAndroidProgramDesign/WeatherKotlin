package android.vv.com.weatherkotlin.ui.city

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.vv.com.weatherkotlin.R
import android.widget.TextView
import android.vv.com.weatherkotlin.util.extensions.DelegatesExt
import android.vv.com.weatherkotlin.data.CityEvent
import android.vv.com.weatherkotlin.data.SearchEntity
import android.vv.com.weatherkotlin.data.HttpManager
import android.vv.com.weatherkotlin.data.WeatherObserver
import android.vv.com.weatherkotlin.util.rxbus.busPostSticky
import android.vv.com.weatherkotlin.util.ctx
import android.vv.com.weatherkotlin.util.toPinyin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_city.*
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit

class CityActivity : AppCompatActivity() {

    companion object Factory {
        const val SP_KEY_CITY_NAME = "cityName"
        const val SP_VLAUE_DEFAULT_NAME = "beijing"

        fun getStartIntent(cxt: Context): Intent {
            return Intent(cxt, CityActivity::class.java)
        }
    }

    var dialog: ProgressDialog? = null
    var spCityName: String by DelegatesExt.preference( SP_KEY_CITY_NAME, SP_VLAUE_DEFAULT_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = DrawerArrowDrawable(toolbar.ctx).apply { progress = 1f }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                if (TextUtils.isEmpty(etSearch.text)) {
                    toast(R.string.city_name_empty)
                    return@OnEditorActionListener false
                }
                if (etSearch.text.toString().toPinyin().equals(spCityName)) {
                    toast(R.string.city_name_equals)
                    return@OnEditorActionListener false
                }

                seacherCity(etSearch.text.toString().toPinyin())
            }
            return@OnEditorActionListener false
        })

        etSearch.setText(spCityName)
        etSearch.setSelection(spCityName.length);

    }


    private fun seacherCity(cityName: String) {
//        cityName.log("cityName")
        var weatherService = HttpManager.getInstance().weatherService
        weatherService.getWeatherSearch(cityName)
                //防止重复点击
                .throttleFirst(5L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (dialog === null) {
                        dialog = indeterminateProgressDialog(getString(R.string.city_name_search))
                    }
                    dialog?.show();
                }
                .doFinally {
                    dialog?.dismiss();
                }
                .subscribe(object : WeatherObserver<List<SearchEntity>> {
                    override fun onWeatherNext(t: List<SearchEntity>) {
                        if (!t.isEmpty() || t.size > 0) {

                            alert("当前城市为 $spCityName，是否切换到 $cityName", "切换城市") {
                                yesButton {
                                    spCityName = cityName
                                    busPostSticky(CityEvent(cityName))
                                    finish()
                                }
                                noButton { }
                            }.show()
                        } else {
                            toast(R.string.city_name_error)
                        }
                    }

                    override fun onWeatherError(message: String) {
                        toast(message)
                    }

                })
    }

}
