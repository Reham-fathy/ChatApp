package com.example.chat.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chat.R
import com.example.chat.adapter.UserAdapter
import com.example.chat.databinding.ActivityUsersBinding
import com.example.chat.firebase.FirebaseService
import com.example.chat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging

class UsersActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityUsersBinding
    var userList=ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUsersBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        FirebaseService.sharedPref=getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token=it.token
        }
      binding.rvUsers.layoutManager=LinearLayoutManager(this, LinearLayout.VERTICAL,false)


        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
binding.imgProfile.setOnClickListener{
    val intent= Intent(this@UsersActivity, ProfileActivity::class.java)
    startActivity(intent)
    finish()
}
        getUsersList()

    }

  fun getUsersList(){
      val firebase:FirebaseUser=FirebaseAuth.getInstance().currentUser!!
      var userid=firebase.uid
      FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

      val databaseReference:DatabaseReference=
          FirebaseDatabase.getInstance().getReference("Users")
      databaseReference.addValueEventListener(object :ValueEventListener{


          override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()

          }
          override fun onDataChange(snapshot: DataSnapshot) {
             userList.clear()
              val currentUser=snapshot.getValue(User::class.java)
              if (currentUser!!.profileImage=="")
              {
                  binding.imgProfile.setImageResource(R.drawable.ic_profile)
              }
              else

              {
                  Glide.with(this@UsersActivity).load(currentUser.profileImage).into(binding.imgProfile)

              }
              for (dataSnapShot:DataSnapshot in snapshot.children)
              {
                  val user=dataSnapShot.getValue(User::class.java)
                  if (!user!!.userId.equals(firebase.uid)){
                      userList.add(user)
                  }
              }
              var userAdapter=UserAdapter(this@UsersActivity,userList)
              binding.rvUsers.adapter=userAdapter
          }



      })
  }
}