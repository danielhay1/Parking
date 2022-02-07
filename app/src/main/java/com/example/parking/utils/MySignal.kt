package com.example.parking.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast

class MySignal() {
    private var instance: MySignal? = null

    companion object {
        @Volatile private var instance : MySignal? = null
        @Synchronized
        fun getInstance() : MySignal {
            if (instance == null)
                instance = MySignal()
            return instance as MySignal
        }
    }

    public fun toast(context: Context,msg: String) {
        Log.d("pttt", "toast: $msg")
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    public fun alertDialog(
        activity: Activity,
        title: String?,
        msg: String?,
        pos: String?,
        neg: String?,
        onClickListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(msg) //set positive button
            .setPositiveButton(pos, onClickListener) //set negative button
            .setNegativeButton(neg) { dialogInterface, i ->
                //set what should happen when negative button is clicked
            }.show()
    }
}