package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R

class MyTaskAdapter(context : Context,taskItems : ArrayList<String>) : RecyclerView.Adapter<MyTaskAdapter.MyTaskAdapterVH>() {

    val context=context;
    val taskItems=taskItems


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTaskAdapterVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout,parent,false)

        return MyTaskAdapterVH(view)
    }

    override fun onBindViewHolder(holder: MyTaskAdapterVH, position: Int) {
        holder.bind(taskItems[position],position)
    }

    override fun getItemCount(): Int {
        return taskItems.size
    }


    class MyTaskAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val task1=itemView.findViewById<TextView>(R.id.task1)

        fun bind(item :String,pos :Int){
            task1.text = ("${pos},,${item}")
        }

    }
}