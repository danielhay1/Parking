package com.example.parking.utils

import android.net.Uri
import android.util.Log
import com.example.parking.objects.Post
import com.example.parking.objects.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask


class FirestoreManager() {
    private val USERS = "users"
    private val USER_PHOTO = "userPhoto"
    private val ALL_POST = "AllPost"

    private var db: FirebaseFirestore? = null

    interface UserReadyCallback {
        fun onUserReady(user: User)
    }
    interface ImageReadyCallback {
        fun imageUriCallback(imageUri: String)
    }

    interface CurrentUserImageCallBack {
        fun currentUserImageCallBack(photo: String?)
    }

    interface AllPostsCallback {
        fun postListReady(list: MutableList<Post>)
    }


    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getUserFromFireStore(userReadyCallback: UserReadyCallback) {


        AuthUtils.getCurrentUser()?.let {
            Log.d("firestore_manager", "getUserFromFireStore: uid=$it")
            db?.collection(USERS)?.document(it)?.get()?.addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    val user = documentSnapshot.toObject(User::class.java)
                    if (userReadyCallback != null) {
                        user?.let { it1 -> userReadyCallback.onUserReady(it1) }
                    }
                }
            }
        }
    }

    fun savePhotoToStorageUri(uri: Uri, imageReadyCallback: ImageReadyCallback) {

        // Create a storage reference from our app
        val storageRef = FirebaseStorage.getInstance().reference
        val mountainImagesRef = storageRef.child("image/" + System.currentTimeMillis())
        val uploadTask = mountainImagesRef.putFile(uri)
        uploadTask.addOnFailureListener { exception: Exception? -> }
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                Log.d("firestore_manager", "savePhotoToStorageUri: successed ")
                getImageUri(taskSnapshot, imageReadyCallback)
            }
    }


    private fun getImageUri(
        taskSnapshot: UploadTask.TaskSnapshot,
        imageReadyCallback: ImageReadyCallback
    ) {
        if (taskSnapshot.metadata != null) {
            if (taskSnapshot.metadata!!.reference != null) {
                val result: Task<Uri> = taskSnapshot.storage.downloadUrl
                result.addOnSuccessListener(OnSuccessListener<Uri> { uri: Uri ->
                    val imageUrl: String = uri.toString()
                    Log.d("firestore_manager", "imageUrl=$imageUrl")

                    if (imageReadyCallback != null) {
                        imageReadyCallback.imageUriCallback(imageUrl)
                    }
                })
            }
        }
    }

    fun updateUserPhotoInFireStore(userPhoto: String?) {
        val db = FirebaseFirestore.getInstance()
        val myRef = db.collection(USERS).document(AuthUtils.getCurrentUser()!!)
        myRef.update(USER_PHOTO, userPhoto).addOnCompleteListener(OnCompleteListener { task: Task<Void?>? ->
            Log.d("firestore_manager", "updateUserPhotoInFireStore: user image updated, imageUri")
        })
    }

    fun getUserPhotoFromFireStore(currentUserImageCallBack: CurrentUserImageCallBack) {
        db!!.collection(USERS).document(AuthUtils.getCurrentUser()!!).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val result = task.result
                    val user = result.toObject(User::class.java)
                    if (currentUserImageCallBack != null && user != null) {
                        currentUserImageCallBack.currentUserImageCallBack(user.userPhoto)
                    }
                }
            }
    }

    fun getPostListFromDB(allPostsCallback: AllPostsCallback) {
        db!!.collection(ALL_POST).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list: MutableList<Post> = ArrayList()
                    for (document in task.result) {
                        val post: Post = document.toObject(Post::class.java)
                        list.add(post)
                    }
                    allPostsCallback.postListReady(list)
                    Log.d("DSFdfs", "FDSsfd" + list.size.toString())
                } else {
                    Log.d("FDSsdfsdf", "Error getting documents: ", task.exception)
                }
            }
    }




}