package android.vv.com.weatherkotlin.data

import android.vv.com.weatherkotlin.util.log
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.Type

internal class GsonResponseBodyConverter<T>(private val gson: Gson, private val type: Type) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        val response = value.string()
        //httpResult 只解析result字段
        response.log("result")


        try {
            val httpResult = gson.fromJson(response, ErrorResult::class.java)
            if (httpResult != null) {
                throw ApiException(22)
            }
        } catch (e: Exception) {

        }

        return gson.fromJson<T>(response, type)


    }


}
