package com.devd.calenderbydw.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devd.calenderbydw.data.remote.calendar.DiaryStickerResponse
import com.devd.calenderbydw.databinding.StickerBottomSheetDialogLayoutBinding
import com.devd.calenderbydw.utils.ConstVariable.STICKERS_TEMP_LIST
import com.devd.calenderbydw.utils.autoCleared
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerBottomSheetDialog() : BottomSheetDialogFragment() {
    private var builder: Builder? = null
    private var binding by autoCleared<StickerBottomSheetDialogLayoutBinding>()
    private val stickerAdapter = StickerItemAdapter()

    private constructor(builder: Builder) : this() {
        this.builder = builder
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StickerBottomSheetDialogLayoutBinding.inflate(inflater, container, false)
        binding.rcStickerItem.adapter = stickerAdapter
        stickerAdapter.setOnStickerClickListener(object : StickerClickListener {
            override fun onStickerClick(stickerId: String) {
                builder?.stickerClickListener?.onStickerClick(stickerId)
                dismiss()
            }
        })
        builder?.stickerList?.let {
            stickerAdapter.submitList(it)
        } ?: kotlin.run {
            stickerAdapter.submitList(listOf(DiaryStickerResponse("icon_sticker_happy.png")))
        }
        return binding.root
    }

    interface StickerClickListener {
        fun onStickerClick(stickerId: String)
    }

    open class Builder {
        var stickerClickListener: StickerClickListener? = null
        var stickerList: List<DiaryStickerResponse>? = null

        fun setOnStickerClickListener(listener: StickerClickListener) {
            stickerClickListener = listener
        }

        fun stickerList(list: List<DiaryStickerResponse>) {
            stickerList = list
        }

        fun build() = StickerBottomSheetDialog(this)
    }
}