package android.vv.com.weatherkotlin.ui.daily

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.vv.com.weatherkotlin.R
import android.vv.com.weatherkotlin.util.extensions.DelegatesExt
import android.vv.com.weatherkotlin.data.UpdateForecastEvent
import android.vv.com.weatherkotlin.data.ForecastResult
import android.vv.com.weatherkotlin.util.adapter.StickyHeaderDecoration
import android.vv.com.weatherkotlin.util.rxbus.busRemoveStickyEvent
import android.vv.com.weatherkotlin.util.rxbus.busToObservableSticky
import com.trello.rxlifecycle2.components.support.RxFragment
import kotlinx.android.synthetic.main.fragment_daily.*


class DailyFragment : RxFragment() {

    private lateinit var mAdapter : ForecastDailyAdapter

    companion object Factory {
        fun newInstance(): DailyFragment {
            return DailyFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_daily, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAdapter  = ForecastDailyAdapter()

        recyclerView.apply {
            setHasFixedSize(true)

            layoutManager = object :LinearLayoutManager(context){
                override fun canScrollVertically(): Boolean {
                    return true
                }
            }
            val decor = StickyHeaderDecoration(mAdapter)
            adapter = mAdapter
            addItemDecoration(decor, 0)
        }

        loadData(DelegatesExt.forecastResult)

        busToObservableSticky(UpdateForecastEvent::class.java)
                .compose(this.bindToLifecycle())
                .subscribe {
            loadData(it.fr)
        }

    }

    private fun loadData(fr: ForecastResult?) {
        fr?.let { mAdapter.addData(fr.forecast.forecastday) }
    }

    override fun onDestroy() {
        super.onDestroy()
        busRemoveStickyEvent(UpdateForecastEvent::class.java)
    }

}