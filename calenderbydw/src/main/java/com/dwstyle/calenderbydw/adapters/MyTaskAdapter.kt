package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R

class MyTaskAdapter(
    private  val context : Context,
    private  val taskDate :  ArrayList<String>,
    private  val taskLists :HashMap<String,ArrayList<String>>) : RecyclerView.Adapter<MyTaskAdapter.MyTaskAdapterVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTaskAdapterVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout,parent,false)

        return MyTaskAdapterVH(view)
    }

    override fun onBindViewHolder(holder: MyTaskAdapterVH, position: Int) {

        holder.bind(taskDate[position],position,taskLists,context)
    }

    override fun getItemCount(): Int {
        return taskDate.size
    }


    class MyTaskAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTaskDate=itemView.findViewById<TextView>(R.id.tvTaskDate)
        val llTaskTitleBox=itemView.findViewById<LinearLayout>(R.id.llTaskTitleBox)

        fun bind(item : String,pos :Int,taskLists :HashMap<String,ArrayList<String>>,context :Context){
            tvTaskDate.text = (item)
            llTaskTitleBox.removeAllViews()
            if (taskLists.containsKey(item)){
                for (str in taskLists[item]!!){
                    val textView = TextView(context)
                    val params =LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.bottomMargin=13
                    textView.layoutParams=params
                    textView.text="- ${str}"
                    textView.setTextColor(Color.parseColor("#FFFFFF"))
                    llTaskTitleBox.addView(textView)
                }
            }else{
                val textView = TextView(context)
                textView.setTextColor(Color.parseColor("#FFFFFF"))
                textView.text="일정이 없네요~"
                textView.gravity=Gravity.CENTER
                llTaskTitleBox.addView(textView)
            }
        }

    }
}