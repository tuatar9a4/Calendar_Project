package com.devd.calenderbydw.ui.dialog

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.dialog.BottomSheetItem
import com.devd.calenderbydw.databinding.CustomBottomSheetDialogItemBinding

class DialogItemAdapter : ListAdapter<BottomSheetItem, DialogItemAdapter.DialogItemVH>(diff) {

    private var itemClickListener : CustomBottomSheet.BottomSheetClickListener? =null

    fun setOnItemClickListener(listener : CustomBottomSheet.BottomSheetClickListener){
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogItemVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_bottom_sheet_dialog_item,parent,false)
        return DialogItemVH(CustomBottomSheetDialogItemBinding.bind(view),itemClickListener)
    }

    override fun onBindViewHolder(holder: DialogItemVH, position: Int) {
        holder.onBind(currentList[holder.bindingAdapterPosition])
    }


    class DialogItemVH(private val binding : CustomBottomSheetDialogItemBinding,private val listener : CustomBottomSheet.BottomSheetClickListener?) : ViewHolder(binding.root){
        fun onBind(item : BottomSheetItem){
            binding.tvBottomSheetText.text = item.text
            item.gravity?.let {
                binding.tvBottomSheetText.gravity = item.gravity
            }
            binding.root.setOnClickListener {
                listener?.onItemClick(item.type,item.text?:"")
            }
            if(item.isCheck){
                binding.ivCheck.visibility= View.VISIBLE
            }else{
                binding.ivCheck.visibility= View.GONE
            }
        }
    }

    companion object{
        val diff = object :ItemCallback<BottomSheetItem>(){
            override fun areItemsTheSame(
                oldItem: BottomSheetItem,
                newItem: BottomSheetItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: BottomSheetItem,
                newItem: BottomSheetItem
            ): Boolean {
                return  oldItem.type == newItem.type
            }
        }
    }

}