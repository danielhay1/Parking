package com.example.parking.objects

import android.net.Uri
import android.util.Log
import com.example.parking.InterFaces.ImageUriCallBack
import com.example.parking.utils.AuthUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import com.example.parking.InterFaces.GetAllPostCallBack
import com.example.parking.InterFaces.GetMyPostsCallBack
import com.example.parking.fragments.MyPostsFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*


class DBManager {
    private val db = Firebase.firestore
    private var imageUriCallBack: ImageUriCallBack? = null
    private var getAllPostCallBack: GetAllPostCallBack? = null
    private var getMyPostsCallBack: GetMyPostsCallBack? = null


    companion object {
        private const val USERS = "users"
        private const val POST = "post"
        const val ALL_POST = "AllPost"
    }

    fun initImageUriCallBack(imageUriCallBack: ImageUriCallBack?) {
        this.imageUriCallBack = imageUriCallBack
    }

    fun initGetAllPostCallBack(getAllPostCallBack: GetAllPostCallBack) {
        this.getAllPostCallBack = getAllPostCallBack

    }

    fun initGetMyPostCallBack(myPostsFragment: GetMyPostsCallBack) {
        this.getMyPostsCallBack = myPostsFragment

    }

    fun saveNewUserToFireStore(user: User) {

        val newUser = AuthUtils.getCurrentUser()?.let { db.collection(USERS).document(it) }

        newUser?.set(user)?.addOnCompleteListener {
            if (it.isSuccessful) {

            }
        }


    }

    fun savePhotoToStorageUri(uri: Uri) {

        // Create a storage reference from our app
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val mountainImagesRef: StorageReference =
            storageRef.child("image/" + System.currentTimeMillis())
        val uploadTask: UploadTask = mountainImagesRef.putFile(uri)
        uploadTask.addOnFailureListener { exception -> }
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                getImageUri(
                    taskSnapshot
                )
            }.addOnFailureListener {

            }
    }


    private fun getImageUri(taskSnapshot: UploadTask.TaskSnapshot) {
        if (taskSnapshot.metadata != null) {
            if (taskSnapshot.metadata!!.reference != null) {
                val result: Task<Uri> = taskSnapshot.storage.downloadUrl
                result.addOnSuccessListener { uri: Uri ->
                    val imageUrl = uri.toString()
                    imageUriCallBack?.imageUriCallBack(imageUrl)
                }
            }
        } else {
            Log.d("sdffsdfsd", "FDSfsdfsdsd")
        }
    }

    fun saveNewPostInFireStore(post: Post?) {
        savePostInMyPost(post!!)
        savePostInAllPost(post)
    }

    private fun savePostInMyPost(post: Post) {
        val newUser = db.collection(USERS).document(post.currentUserId)
            .collection(POST).document(post.docId)
        newUser.set(post).addOnSuccessListener { aVoid: Void? -> }
    }

    private fun savePostInAllPost(post: Post) {
        val newUser = db.collection(ALL_POST).document(post.docId)
        newUser.set(post).addOnSuccessListener { aVoid: Void? -> }
    }


    fun getAllPost() {

        db.collection(ALL_POST).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Log.d("FDSsdfsdf", "Error getting documents: ")
                    val list: ArrayList<Post> = ArrayList()
                    for (document in task.result) {
                        val post: Post = document.toObject(Post::class.java)
                        list.add(post)

                    }

                    getAllPostCallBack?.getAllPostCallBack(list)
                } else {
                    Log.d("FDSsdfsdf", "Error getting documents: ")
                }
            }


//        registrationAllPost = query
//            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
//                val allPost = ArrayList<Post>()
//                if (value != null) {
//                    //when there isn't collection of post
//                    if (value.size() == 0) {
//                        // create one
//                        createAllPostCollection()
//                        //there is already collection in FireStore
//                    } else {
//
//                        // we want to return to all post
//                        if (returnAllPost) {
//                            // get all post from fire store
//                            insertAllPostToArray(allPost, value)
//                        } else {
//                            //insert only the post that was changed
//                            insertOnlyPostWasChanges(allPost, value)
//                        }
//                    }
//                }
//                //                    getOnlyPostsAccordingTypeOfParking(allPost);
//                passDataAccordingReturnAllPostValue(allPost)
//            }
    }


    fun getMyPostsFromDB() {
        db.collection(USERS).document(AuthUtils.getCurrentUser()!!).collection(POST).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Log.d("FDSsdfsdf", "Error getting documents: ")
                    val list: ArrayList<Post> = ArrayList()
                    for (document in task.result) {
                        val post: Post = document.toObject(Post::class.java)
                        list.add(post)

                    }
                    getMyPostsCallBack?.getMyPostsCallBack(list)
                } else {
                    Log.d("FDSsdfsdf", "Error getting documents: ")
                }
            }

    }

    fun updateLike(post: Post) {

        db.collection(ALL_POST)
            .document(post.docId).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    var updatePost = documentSnapshot.toObject(Post::class.java)
                    if (updatePost != null) {
                        Log.d("FSDfdssfd" , "DSFfsdfd" + updatePost.numLiking)
                        updatePost = post
                        updateAllPostInFireStore(updatePost)
                        updatePostInMyPostFireStore(updatePost)
                    }
                }
            }
    }

    private fun updatePostInMyPostFireStore(updatePost: Post) {
        db.collection(USERS)
            .document(updatePost.currentUserId)
            .collection(POST)
            .document(updatePost.docId)
            .set(updatePost).addOnSuccessListener { unused: Void? -> }
    }

    private fun updateAllPostInFireStore(updatePost: Post) {
        db.collection(ALL_POST)
            .document(updatePost.docId).set(updatePost)
            .addOnSuccessListener { unused: Void? ->
                Log.d("SDFfds" , "FDSfds")

            }

    }

    fun deleteLikeFromFireStore(post: Post) {
        db.collection(ALL_POST)
            .document(post.docId).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                if (task.isSuccessful) {
                    handleDeleteLikeFireStore(task)
                }
            }

    }

    private fun handleDeleteLikeFireStore(task: Task<DocumentSnapshot?>) {
        val documentSnapshot = task.result!!
        val updatePost = documentSnapshot.toObject(Post::class.java)
        if (updatePost != null) {
            updateAllPostInFireStore(updatePost)
            updatePostInMyPostFireStore(updatePost)
        }

    }
}
