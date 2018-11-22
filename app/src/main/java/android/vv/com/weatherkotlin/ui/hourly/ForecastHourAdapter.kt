package android.vv.com.weatherkotlin.ui.hourly

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.vv.com.weatherkotlin.R
import android.vv.com.weatherkotlin.data.HourEntity
import android.vv.com.weatherkotlin.util.*
import kotlinx.android.synthetic.main.item_forecast_hour.view.*


class ForecastHourAdapter(var datas: List<HourEntity> = emptyList<HourEntity>()) : RecyclerView.Adapter<ForecastHourAdapter.ViewHolder>() {

    fun addData(newDatas: List<HourEntity>?){
        if (newDatas != null) {
            datas = newDatas
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(datas.get(position));
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_forecast_hour))
    }

    override fun getItemCount() = datas.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindData(he: HourEntity) {
            //日期格式为2017-06-05 00:00，只需要显示 10位之后的 00：00
            itemView.tvTime.text = he.time.substring(10)
            itemView.ivCondition.loadImg(he.condition.icon)
            itemView.tvTemp.text = he.temp_c.toTempByCelsius()
            itemView.tvWind.text = he.wind_dir.toWindDir() + he.wind_kph.toKph()
        }
    }
}