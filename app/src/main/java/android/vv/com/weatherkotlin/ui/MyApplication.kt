package android.vv.com.weatherkotlin.ui

import android.app.Application
import android.vv.com.weatherkotlin.util.extensions.DelegatesExt
import com.squareup.leakcanary.BuildConfig
import com.squareup.leakcanary.LeakCanary

class MyApplication : Application() {

    //companion object 伴⽣对象定义 扩展函数和属性：
    companion object {
        var instance: MyApplication by DelegatesExt.notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if(BuildConfig.DEBUG){
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
        }

    }
}