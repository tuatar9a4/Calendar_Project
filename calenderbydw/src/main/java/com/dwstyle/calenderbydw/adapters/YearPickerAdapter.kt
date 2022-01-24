package com.dwstyle.calenderbydw.adapters

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.utils.MyDatePicker

class YearPickerAdapter(private val str:ArrayList<String>) : RecyclerView.Adapter<YearPickerAdapter.YearPickerVH>() {

    private var oldPosition=0;
    private var newPosition=0;
    private var selectionPos=0;
    val handler =  Handler();


    class YearPickerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvYear =itemView.findViewById<TextView>(R.id.tvYear)

        fun dd (selectionPos : Int,position: Int){
            itemView.setOnClickListener {
                bindingAdapter?.notifyItemChanged(selectionPos)
                (bindingAdapter as YearPickerAdapter).setSele(position)
                bindingAdapter?.notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearPickerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.datepicker_item,parent,false)

        return YearPickerVH(view)
    }

    override fun onBindViewHolder(holder: YearPickerVH, position: Int) {
        holder.tvYear.text=str[position]
        if (position==selectionPos){
            holder.tvYear.setTextColor(Color.parseColor("#0000FF"))
        }else{
            holder.tvYear.setTextColor(Color.parseColor("#FFFFFF"))
        }
        holder.dd(selectionPos,position)
    }


    override fun getItemCount(): Int {
        return str.size
    }
    public fun setSele(pos: Int){
        selectionPos=pos;
    }

    fun setTextColor(position: Int){
        if (position!=-1){
            selectionPos=position
            newPosition=position


            val r :Runnable = Runnable() {
                kotlin.run {
                    notifyDataSetChanged();
                }
            };

            handler.post(r);
            oldPosition=newPosition
        }
    }

}