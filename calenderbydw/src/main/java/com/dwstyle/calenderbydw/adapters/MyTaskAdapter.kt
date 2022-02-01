package com.dwstyle.calenderbydw.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.SendSyncData
import com.google.android.gms.wearable.Wearable

class MyTaskAdapter(
    private  val context : Context,
    private  var taskDate :  ArrayList<String>,
    private  var taskLists :HashMap<String,ArrayList<String>>) : RecyclerView.Adapter<MyTaskAdapter.MyTaskAdapterVH>() {

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

    fun setItems(taskDate :  ArrayList<String>,taskLists :HashMap<String,ArrayList<String>>){
        this.taskDate=taskDate
        this.taskLists=taskLists
        notifyDataSetChanged()
    }


    class MyTaskAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTaskDate=itemView.findViewById<TextView>(R.id.tvTaskDate)
        val llTaskTitleBox=itemView.findViewById<LinearLayout>(R.id.llTaskTitleBox)

        fun bind(item : String,pos :Int,taskLists :HashMap<String,ArrayList<String>>,context :Context){
            val tempStr =item.split(".")
            if (tempStr[3]=="일" || tempStr[3]=="Sun"){
                tvTaskDate.setTextColor(context.getColor(R.color.sunColor))
            }else if (tempStr[3]=="토" || tempStr[3]=="Sat"){
                tvTaskDate.setTextColor(context.getColor(R.color.satColor))
            }else{
                tvTaskDate.setTextColor(Color.parseColor("#FFFFFF"))
            }
            tvTaskDate.text = (item)
            llTaskTitleBox.removeAllViews()
            if (taskLists.containsKey("${tempStr[0]}.${tempStr[1]}.${tempStr[2]}")){
                for (str in taskLists["${tempStr[0]}.${tempStr[1]}.${tempStr[2]}"]!!){
                    val splitIndex  =str.split("&")
                    val lllayout =ConstraintLayout(context)
                    val llparams =ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                    llparams.topMargin=10
                    llparams.bottomMargin=10
                    lllayout.layoutParams=llparams
                    lllayout.clipChildren=false

                    val textView = TextView(context)
                    val button =Button(context)
                    val params =ConstraintLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.topToTop=0
                    params.leftToLeft=0
                    textView.layoutParams=params
                    textView.setPadding(0,0,45,0)
                    textView.text="- ${splitIndex[0]}"
                    textView.setTextColor(Color.parseColor("#FFFFFF"))
//                    llTaskTitleBox.addView(textView)


                        val params2 =ConstraintLayout.LayoutParams(30,30)
                        params2.topToTop=0
                        params2.endToEnd=0
                        params2.startToEnd=textView.id
                        params2.rightMargin=10
                        button.layoutParams=params2
                        button.text = "삭제"
                        button.background=context.getDrawable(R.drawable.delete_icon_w)
                        button.setOnClickListener {
                            val deleteAlert =AlertDialog.Builder(context)
                                .setTitle("일정을 삭제하시겠습니까?")
                                .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, int ->
                                    TaskDatabaseHelper.deleteTask(splitIndex[1],TaskDatabaseHelper(context,"wearTask.db",null,3).writableDatabase)
                                    llTaskTitleBox.removeView(lllayout)
                                    SendSyncData.changeDBToBytes(context, Wearable.getDataClient(context))
                                })
                                .setNegativeButton("Cancel",null)
                                .create()

                        deleteAlert.show()
                    }
//                    llTaskTitleBox.addView(button)
                    lllayout.addView(textView)
                    lllayout.addView(button)
                    llTaskTitleBox.addView(lllayout)
                }
            }else{
                val textView = TextView(context)
                textView.setTextColor(Color.parseColor("#FFFFFF"))
                textView.text="No Scheduler"
                textView.gravity=Gravity.CENTER
                llTaskTitleBox.addView(textView)
            }
        }

    }
}