package com.devd.calenderbydw.ui.diary.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.databinding.DiaryListItemBinding

class DiaryListAdapter : PagingDataAdapter<DiaryEntity, DiaryListAdapter.DiaryListViewHolder>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_list_item,parent,false)
        return DiaryListViewHolder(DiaryListItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: DiaryListViewHolder, position: Int) {
        getItem(holder.bindingAdapterPosition)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return snapshot().items.size
    }
    class DiaryListViewHolder(private val binding : DiaryListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : DiaryEntity){
            binding.tvDate.text = "${item.id.toString()}|${item.createDate}"
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