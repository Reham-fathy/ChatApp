package com.example.chat.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chat.R
import com.example.chat.databinding.ActivityProfileBinding
import com.example.chat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private var filePath: Uri?=null
    private val PICK_IMAGE_REQUEST:Int=2020

    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef:StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        storage=FirebaseStorage.getInstance()
        storageRef=storage.reference
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val user=snapshot.getValue(User::class.java)
                binding.userName.setText(
                    user!!.userName
                )

                if (user.profileImage=="")
                {
                  binding.userImage.setImageResource(R.drawable.ic_profile)
                }
                else

                {
                    Glide.with(this@ProfileActivity).load(user.profileImage).into(binding.userImage)

                }
            }
        })

        binding.imgBack.setOnClickListener {
            val intent= Intent(this@ProfileActivity, UsersActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.userImage.setOnClickListener{
           chooseImage()
        }
        binding.btnSave.setOnClickListener {
            uploadImage()
            binding.progressbar.visibility=View.VISIBLE
        }
    }

private fun chooseImage(){
    val intent:Intent= Intent()
    intent.type="image/*"
    intent.action=Intent.ACTION_GET_CONTENT
    startActivityForResult(Intent.createChooser(intent,"select Image"),PICK_IMAGE_REQUEST)
}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==PICK_IMAGE_REQUEST || resultCode!=null)
        {
            filePath = data!!.data
            try {
                var bitmap:Bitmap=MediaStore.Images.Media.getBitmap(contentResolver,filePath)
                binding.userImage.setImageBitmap(bitmap)


            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if (filePath!=null){
            var ref:StorageReference=storageRef.child("images/"+UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {

                    val hashMap:HashMap<String,String> = HashMap()
                    hashMap.put("userName",binding.userName.text.toString())
                    hashMap.put("profileImage",filePath.toString())
                    databaseReference.updateChildren(hashMap as Map<String,String>)
                    binding.progressbar.visibility=View.GONE

                    Toast.makeText(applicationContext,"uploaded",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    binding.progressbar.visibility=View.GONE
                    Toast.makeText(applicationContext,"failed"+it.message,Toast.LENGTH_SHORT).show()

                }
        }
    }
}