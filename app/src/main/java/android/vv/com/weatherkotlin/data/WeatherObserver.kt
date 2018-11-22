package android.vv.com.weatherkotlin.data

import android.vv.com.weatherkotlin.util.log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.net.ConnectException
import java.net.SocketTimeoutException

interface WeatherObserver<T> : Observer<T> {
    override fun onNext(t: T) {
        onWeatherNext(t)
    }

    override fun onError(e: Throwable) {
        val error = e.message
        if (error === null) {
            return
        }
        e.message?.log("error")

        var message: String?

        if (e is SocketTimeoutException) {
            message = "请求超时"
            onWeatherError(message)
            return
        }
        if (e is ConnectException) {
            message = "网络中断，请检查您的网络状态"
            onWeatherError(message)
            return
        }

        if (error.contains("401")) {
            message = "Api key 出错"
            onWeatherError(message)
            return
        }
        if (error.contains("400")) {
            message = "城市名称出错"
            onWeatherError(message)
            return
        }
        if (error.contains("403")) {
            message = "API key 次数已经超过"
            onWeatherError(message)
            return
        }
        onWeatherError(error)

    }

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {

    }

    fun onWeatherNext(t: T)
    fun onWeatherError(message: String)
}