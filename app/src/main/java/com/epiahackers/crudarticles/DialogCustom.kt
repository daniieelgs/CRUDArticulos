package com.epiahackers.crudarticles

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogCustom {

    companion object{

        fun createDialogButtons(context: Activity, layout: Int, title: String, message: String, btnOk: Int, okCLick: (View) -> Unit, btnCancel: Int, cancelClick: (View) -> Unit): AlertDialog {

            var builder = MaterialAlertDialogBuilder(context)

            var inflater: LayoutInflater = context.layoutInflater

            var v: View = inflater.inflate(layout, null)

            builder.setView(v)

            val ok: Button = v.findViewById(btnOk)
            val cancel: Button = v.findViewById(btnCancel)

            val dialog = builder.create()

            ok.setOnClickListener{
                okCLick(it)
                dialog.dismiss()
            }
            cancel.setOnClickListener{
                cancelClick(it)
                dialog.dismiss()
            }

            dialog.setTitle(title)
            dialog.setMessage(message)

            return dialog
        }

        fun dialogRadioSelector(context: Activity, title: String, cancel: String, positive: String, positiveClick: (Int, String) -> Unit, checkedItem: Int, vararg singleItems: String): MaterialAlertDialogBuilder {

            var itemSelected = checkedItem

            return MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setNeutralButton(cancel) { _, _ -> }
                .setPositiveButton(positive) { _, _ -> positiveClick(itemSelected, singleItems[itemSelected]) }
                .setSingleChoiceItems(singleItems, checkedItem) { _, which -> itemSelected = which }

        }

    }

}