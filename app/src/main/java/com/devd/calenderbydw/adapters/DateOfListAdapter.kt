package com.devd.calenderbydw.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.item.DateOfListItem

class DateOfListAdapter(private val context : Context) : RecyclerView.Adapter<DateOfListAdapter.DateOfListVH>() {
    private var dateLists =ArrayList<DateOfListItem>()
    private var selectPos=0;
    private var oldPos=0;

    var mListener :OnItemClickListener?=null

    interface OnItemClickListener{
        fun onItemClickListener(v :View,year :Int,month :Int,day : Int,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateOfListVH {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.task_date_layout, parent, false)
        return DateOfListVH(view,mListener)
    }

    override fun onBindViewHolder(holder: DateOfListVH, position: Int) {
        holder.bindView(dateLists[position],position)
        if (selectPos==position){
            holder.selectView.visibility=View.VISIBLE
        }else{
            holder.selectView.visibility=View.GONE
        }
    }

    override fun getItemCount(): Int {
       return dateLists.size
    }

    fun selectPosition(position: Int){
        oldPos=selectPos
        this.selectPos=position
        notifyItemChanged(oldPos)
        notifyItemChanged(selectPos)
    }

    fun setOnItemClickListener(listener : OnItemClickListener){
        this.mListener=listener
    }

    fun setDateItems(items : ArrayList<DateOfListItem>){
        this.dateLists=items
        notifyDataSetChanged()
    }


    class DateOfListVH(private val itemView: View,private val mListener :OnItemClickListener?) : RecyclerView.ViewHolder(itemView) {


        val tvWeekOfDate =itemView.findViewById<TextView>(R.id.tvWeekOfDate)
        val tvDayOfMonth=itemView.findViewById<TextView>(R.id.tvDayOfMonth)
        val tvTaskCnt=itemView.findViewById<TextView>(R.id.tvTaskCnt)
        val selectView=itemView.findViewById<View>(R.id.selectView)

        fun bindView(item : DateOfListItem, pos :Int){
            tvWeekOfDate.text=when (item.weekStr){
                "1" -> "Mon"
                "2" -> "Tue"
                "3" -> "Wen"
                "4" -> "Thu"
                "5" -> "Fri"
                "6" -> "Sat"
                "7" -> "Sun"

                else -> {""}
            }
            tvDayOfMonth.text=item.dayOfMonth
            itemView.setOnClickListener {
                mListener?.let {
                    mListener.onItemClickListener(itemView,item.year.toInt(),item.month.toInt(),item.dayOfMonth.toInt(),pos)
                }
            }
            if (item.taskCnt!=0){
                tvTaskCnt.visibility=View.VISIBLE
                tvTaskCnt.text="+${item.taskCnt}"
            }else{
                tvTaskCnt.visibility=View.INVISIBLE
            }

        }
    }


}