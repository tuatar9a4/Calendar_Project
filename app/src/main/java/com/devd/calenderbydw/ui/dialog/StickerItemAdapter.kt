package com.devd.calenderbydw.ui.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.remote.calendar.DiaryStickerResponse
import com.devd.calenderbydw.databinding.StickerBottomSheetItemBinding

class StickerItemAdapter : ListAdapter<DiaryStickerResponse, StickerItemAdapter.StickerItemVH>(diff) {
    private var stickerClickListener : StickerBottomSheetDialog.StickerClickListener? =null

    fun setOnStickerClickListener(listener: StickerBottomSheetDialog.StickerClickListener){
        stickerClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerItemVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sticker_bottom_sheet_item,parent,false)
        return StickerItemVH(StickerBottomSheetItemBinding.bind(view),stickerClickListener)
    }

    override fun onBindViewHolder(holder: StickerItemVH, position: Int) {
        holder.bind(currentList[holder.bindingAdapterPosition])
    }


    class StickerItemVH(private val binding : StickerBottomSheetItemBinding,private val listener :StickerBottomSheetDialog.StickerClickListener?) :ViewHolder(binding.root){
        private val key =itemView.context.getString(R.string.oracleBucketKey)
        private val imagePath = itemView.context.getString(R.string.oracle_bucket_image_path,key)
        fun bind(imageId :DiaryStickerResponse){
            Glide.with(itemView.context)
                .load("${imagePath}${imageId.stickerName}")
                .into(binding.ivStickerItem)
            binding.root.setOnClickListener {
                listener?.onStickerClick(imageId.stickerName)
            }
        }
    }

    companion object{
        val diff = object : ItemCallback<DiaryStickerResponse>(){
            override fun areItemsTheSame(oldItem: DiaryStickerResponse, newItem: DiaryStickerResponse): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DiaryStickerResponse, newItem: DiaryStickerResponse): Boolean {
                return oldItem.stickerName == newItem.stickerName
            }
        }
    }

}