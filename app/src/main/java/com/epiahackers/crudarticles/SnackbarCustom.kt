package com.epiahackers.crudarticles

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class SnackbarCustom {

    companion object{

        var snackabr_time = Snackbar.LENGTH_SHORT

        var position: SnackbarLayout = SnackbarLayout.BOTTOM

        fun snackBarSimple(view: View, message: String, time: Int = snackabr_time): Snackbar {
            val snack = Snackbar.make(view, message, time)

            val viewSnack = snack.view
            val params = viewSnack.layoutParams as FrameLayout.LayoutParams
            params.gravity = position.gravity
            viewSnack.layoutParams = params

            return snack
        }

        fun snackBarCorrect(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)): Snackbar{

            val sb = snackBarSimple(view, message, time)

            with(context){
                sb.setBackgroundTint(getColor(R.color.snackbar_correct))
                sb.setTextColor(getColor(R.color.snackbar_correct_text))

                val tv: TextView =  sb.view.findViewById(com.google.android.material.R.id.snackbar_text)
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_task_black_18, 0, 0, 0)
                tv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            }

            return sb
        }

        fun snackBarInfo(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)): Snackbar{

            val sb = snackBarSimple(view, message, time)

            with(context){
                sb.setBackgroundTint(getColor(R.color.snackbar_info))
                sb.setTextColor(getColor(R.color.snackbar_info_text))
            }

            return sb
        }

        fun snackBarError(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)): Snackbar{
            val sb = snackBarSimple(view, message, time)

            with(context){
                sb.setBackgroundTint(getColor(R.color.snackbar_error))
                sb.setTextColor(getColor(R.color.snackbar_error_text))

                val sbv: View = sb.view

                val tv: TextView =  sbv.findViewById(com.google.android.material.R.id.snackbar_text)
                //tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.snackbar_error))
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_outline_black_16, 0, 0, 0)
                tv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)

            }

            return sb
        }

        fun showSnackbarError(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)) = snackBarError(context, message, time, view).show()
        fun showSnackbarInfo(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)) = snackBarInfo(context, message, time, view).show()
        fun showSnackBarCorrect(context: Activity, message: String, time: Int = snackabr_time, view: View = context.findViewById(android.R.id.content)) = snackBarCorrect(context, message, time, view).show()

    }

    enum class SnackbarLayout(val gravity: Int){

        TOP(Gravity.TOP), CENTER(Gravity.CENTER), BOTTOM(Gravity.BOTTOM)

    }

}