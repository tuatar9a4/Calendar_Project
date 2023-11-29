package com.devd.calenderbydw.ui.diary.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.databinding.DiaryListItemBinding
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_ETC
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_ETC
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY_TXT

class DiaryListAdapter : PagingDataAdapter<DiaryEntity, DiaryListAdapter.DiaryListViewHolder>(diff) {

    private var diaryClickListener : DiaryClickListener? =null

    fun setOnDiaryClickListener(listener: DiaryClickListener){
        diaryClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_list_item,parent,false)
        return DiaryListViewHolder(DiaryListItemBinding.bind(view),diaryClickListener)
    }

    override fun onBindViewHolder(holder: DiaryListViewHolder, position: Int) {
        getItem(holder.bindingAdapterPosition)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return snapshot().items.size
    }

    interface DiaryClickListener{
        fun onItemClick(item : DiaryEntity)
    }
    class DiaryListViewHolder(private val binding : DiaryListItemBinding,private val listener : DiaryClickListener?) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(item : DiaryEntity){
            binding.tvDate.text = "${item.year}.${item.month}.${item.day}"
            setWeatherText(item.weatherType,item.customWeather)
            setFeelText(item.feelingType,item.customFeel)
            setDiarySticker(item.stickerName)
            binding.tvTitle.text="${item.diaryContents}"
            binding.root.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
        private fun setWeatherText(type:Int,customString:String?){
            val weatherText =when(type){
                WEATHER_TYPE_SUNNY->{
                    WEATHER_TYPE_SUNNY_TXT
                }
                WEATHER_TYPE_CLOUDY->{
                    WEATHER_TYPE_CLOUDY_TXT
                }
                WEATHER_TYPE_RAIN->{
                    WEATHER_TYPE_RAIN_TXT
                }
                WEATHER_TYPE_SOSO->{
                    WEATHER_TYPE_SOSO_TXT
                }
                else->{
                    customString?:"알수 없음"
                }
            }
              binding.tvWeather.text="날씨 : ${weatherText}"
        }
        private fun setFeelText(type:Int,customString:String?){
            val fellText =when(type){
                FEEL_TYPE_GOOD->{
                    FEEL_TYPE_GOOD_TXT
                }
                FEEL_TYPE_BAD->{
                    FEEL_TYPE_BAD_TXT
                }
                FEEL_TYPE_SOSO->{
                    FEEL_TYPE_SOSO_TXT
                }
                FEEL_TYPE_HAPPY->{
                    FEEL_TYPE_HAPPY_TXT
                }
                else->{
                    customString?:"알수 없음"
                }
            }
               binding.tvFeel.text="기분 : ${fellText}"
        }

        private fun setDiarySticker(stickerName :String?){
            val key =itemView.context.getString(R.string.oracleBucketKey)
            val imagePath = itemView.context.getString(R.string.oracle_bucket_image_path,key)
            Glide.with(itemView.context)
                .load("${imagePath}${stickerName?:"icon_sticker_happy.png"}")
                .into(binding.ivFeelIcon)
        }
    }

    companion object{
        val diff = object : ItemCallback<DiaryEntity>(){
            override fun areItemsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.diaryContents == newItem.diaryContents &&
                        oldItem.createDate == newItem.createDate
            }
        }
    }

}