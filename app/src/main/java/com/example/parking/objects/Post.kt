package com.example.parking.objects

import com.example.parking.utils.AuthUtils
import java.util.ArrayList

class Post(

    var locationParking: String = "",
    var numLiking:Int = 0,
    var imageParking: String = "",
    var infoParking: String = "",
    var latitude: String = "0",
    var longitude: String="0",
    var currentUserId: String = "",
    var docId:String = "",

) {
    val whoMakesLikes:ArrayList<String> = ArrayList()


    fun addWhoMakeLike(currentId:String) {
        whoMakesLikes.add(currentId)


    }

    fun deleteWhoDeleteLike(currentId:String) {
        whoMakesLikes.remove(currentId)
    }

    fun isCurrentUserMakeLike(): Boolean {
        return this.whoMakesLikes.contains(AuthUtils.getCurrentUser())

    }


}