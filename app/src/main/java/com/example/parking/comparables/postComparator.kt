package com.example.parking.comparables

import com.example.parking.objects.Post

class postComparator :Comparator<Post> {
    override fun compare(o1: Post?, o2: Post?): Int {
        if(o1 == null || o2 == null){
            return 0
        }
        return o2.docId.compareTo(o1.docId)
    }
}