package com.devd.calenderbydw.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import com.devd.calenderbydw.databinding.CommonDialogBinding
import com.devd.calenderbydw.utils.autoCleared

class CommonDialog() : DialogFragment() {

    private var builder: Builder? = null
    private var binding by autoCleared<CommonDialogBinding>()

    private constructor(builder: Builder) : this() {
        this.builder = builder
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            binding = CommonDialogBinding.inflate(layoutInflater, null, false)
            initView()
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            return builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initView() {
        builder?.title?.let {
            binding.tvTitle.text = it
            binding.tvTitle.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.tvTitle.visibility = View.GONE
        }

        builder?.titleColor?.let {
            binding.tvTitle.setTextColor(it)
        }

        builder?.message?.let {
            binding.tvMessage.text = it
            binding.tvMessage.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.tvMessage.visibility = View.GONE
        }

        builder?.messageColor?.let {
            binding.tvMessage.setTextColor(it)
        }

        builder?.negativeBtnString?.let {
            binding.btnLeftNegative.text = it
            binding.btnLeftNegative.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.btnLeftNegative.visibility = View.GONE
        }
        builder?.negativeBtnStrColor?.let {
            binding.btnLeftNegative.setTextColor(it)
        }
        builder?.negativeBtnBgColor?.let {
            binding.btnLeftNegative.setBackgroundColor(it)
        }

        builder?.positiveBtnString?.let {
            binding.btnRightPositive.text = it
            binding.btnRightPositive.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.btnRightPositive.visibility = View.GONE
        }

        builder?.positiveBtnStrColor?.let {
            binding.btnRightPositive.setTextColor(it)
        }
        builder?.positiveBtnBgColor?.let {
            binding.btnRightPositive.setBackgroundColor(it)
        }

        binding.btnLeftNegative.setOnClickListener {
            builder?.negativeBtnClickListener?.onClick()
            dismiss()
        }


        binding.btnRightPositive.setOnClickListener {
            builder?.positiveBtnClickListener?.onClick()
            dismiss()
        }

    }

    class Builder {
        var title: String? = null
        @ColorInt var titleColor: Int? = null
        var message: String? = null
        @ColorInt var messageColor: Int? = null
        var negativeBtnString: String? = null
        @ColorInt var negativeBtnStrColor: Int? = null
        @ColorInt var negativeBtnBgColor: Int? = null
        var positiveBtnString: String = "확인"
        @ColorInt var positiveBtnStrColor: Int? = null
        @ColorInt var positiveBtnBgColor: Int? = null
        var negativeBtnClickListener: CommonDialogClickListener? = null
        var positiveBtnClickListener: CommonDialogClickListener? = null


        fun title(title: String) {
            this.title = title
        }
        fun ytitleColor(@ColorInt color:Int){
            this.titleColor = color
        }
        fun message(message: String) {
            this.message = message
        }
        fun messageColor(@ColorInt color:Int){
            this.messageColor = color
        }
        fun negativeBtnStr(negativeStr: String) {
            this.negativeBtnString = negativeStr
        }
        fun negativeBtnStrColor(@ColorInt color :Int){
            this.negativeBtnStrColor = color
        }
        fun negativeBtnBgColor(@ColorInt color :Int){
            this.negativeBtnBgColor = color
        }
        fun positiveBtnStr(positiveStr: String) {
            this.positiveBtnString = positiveStr
        }
        fun positiveBtnStrColor(@ColorInt color :Int){
            this.positiveBtnStrColor = color
        }
        fun positiveBtnBgColor(@ColorInt color :Int){
            this.positiveBtnBgColor = color
        }
        fun negativeBtnClickListener(listener: CommonDialogClickListener) {
            this.negativeBtnClickListener = listener
        }
        fun positiveBtnClickListener(listener: CommonDialogClickListener) {
            this.positiveBtnClickListener = listener
        }
        fun build() = CommonDialog(this)
    }

    interface CommonDialogClickListener {
        fun onClick()
    }

}