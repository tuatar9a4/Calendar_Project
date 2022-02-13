package com.devd.calenderbydw.adapters

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R

class PickerAdapter(private var str:ArrayList<String>) : RecyclerView.Adapter<PickerAdapter.YearPickerVH>() {

    private var oldPosition=0;
    private var newPosition=0;
    private var selectionPos=-1;
    val handler =  Handler();

    private var mListener : OnDateItemClickListener? =null
    private var checkInit =true
    interface OnDateItemClickListener{
        fun OnDateItemClick(v :View,pos: Int)

    }


    class YearPickerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvYear: TextView =itemView.findViewById(R.id.tvYear)

        fun bindItem (selectionPos : Int, position: Int, mListener:OnDateItemClickListener?){
            itemView.setOnClickListener {
                bindingAdapter?.notifyItemChanged(selectionPos)
                (bindingAdapter as PickerAdapter).setSelectedPosition(position)
                bindingAdapter?.notifyItemChanged(position)
                Log.d("도원","adapter Click : ${position}")
                mListener?.OnDateItemClick(it,position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearPickerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.datepicker_item,parent,false)

        return YearPickerVH(view)
    }

    override fun onBindViewHolder(holder: YearPickerVH, position: Int) {
        holder.tvYear.text=str[position]
        holder.bindItem(selectionPos,position,mListener)
        if (selectionPos==-1){
            return
        }
//        if (position==selectionPos){
//            holder.tvYear.setTextColor(Color.parseColor("#0000FF"))
//        }else{
//            holder.tvYear.setTextColor(Color.parseColor("#FFFFFF"))
//        }
    }

    override fun getItemCount(): Int {
        return str.size
    }

    fun setClickListener(mListener :OnDateItemClickListener){
        this.mListener=mListener;

    }

    fun setItems(items :ArrayList<String>){
        this.str=items;
        notifyDataSetChanged()
    }
    fun setSelectedPosition(pos :Int){
        selectionPos=pos
        notifyDataSetChanged()
    }

}