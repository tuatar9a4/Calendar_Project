package com.devd.calenderbydw.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.devd.calenderbydw.data.local.dialog.BottomSheetItem
import com.devd.calenderbydw.databinding.CustomBottomSheetDialogLayoutBinding
import com.devd.calenderbydw.utils.autoCleared
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class CustomBottomSheetDialog() : BottomSheetDialogFragment() {

    private var builder: Builder? = null
    private var binding by autoCleared<CustomBottomSheetDialogLayoutBinding>()

    private constructor(builder: Builder) : this() {
        this.builder = builder
    }

    private val dialogItemAdapter = DialogItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CustomBottomSheetDialogLayoutBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.tvBottomSheetTitle.text = builder?.title?.ifEmpty {
            binding.tvBottomSheetTitle.visibility = View.GONE
            ""
        }
        binding.rcSheetList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcSheetList.adapter = dialogItemAdapter
        builder?.itemClickListener?.let { dialogItemAdapter.setOnItemClickListener(onInterceptorItemClick()) }
        builder?.bottomSheetItems?.let {
            dialogItemAdapter.submitList(it) {
                builder?.scrollPos?.let { pos->
                    binding.rcSheetList.scrollToPosition(pos)
                }
            }
        }
    }

    private fun onInterceptorItemClick()= object :BottomSheetClickListener{
        override fun onItemClick(type: Int, text: String) {
            builder?.itemClickListener?.onItemClick(type, text)
            dismiss()
        }
    }

    interface BottomSheetClickListener {
        fun onItemClick(type: Int, text: String)
    }

    open class Builder {
        var title: String? = null
        var bottomSheetItems: List<BottomSheetItem>? = null
        var itemClickListener: BottomSheetClickListener? = null
        var scrollPos : Int? =null
        fun build() = CustomBottomSheetDialog(this)
        fun title(title: String) = apply { this.title = title }
        fun sheetItem(sheetItem: List<BottomSheetItem>) =
            apply { this.bottomSheetItems = sheetItem }

        fun setItemClickListener(itemClickListener: BottomSheetClickListener) =
            apply { this.itemClickListener = itemClickListener }

        fun scrollPos(pos :Int) = apply { this.scrollPos = pos }

    }
}