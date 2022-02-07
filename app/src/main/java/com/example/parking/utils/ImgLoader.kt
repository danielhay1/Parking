package com.example.parking.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ImgLoader(val context: Context) {
    private var instance: ImgLoader? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance : ImgLoader? = null
        @Synchronized
        fun getInstance(context: Context) : ImgLoader {
            if (instance == null)
                instance = ImgLoader(context.getApplicationContext())
            return instance as ImgLoader
        }
    }

    fun getImgIdentifier(name: String?): Int {
        return context.getResources()
            .getIdentifier(name, "drawable", context.getPackageName())
    }

    fun loadImg(name: String, img: ImageView) {
        /*
         * Use to load img.
         * */
        img.setImageResource(getImgIdentifier(name))
    }

    fun loadImgByGlide(name: String, img: ImageView) {
        /*
         * Use to load img using glide.
         * */
        Glide.with(context)
            .load(getImgIdentifier(name))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(img)
    }
}