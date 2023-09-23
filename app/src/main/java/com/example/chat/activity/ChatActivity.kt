package com.example.chat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chat.R
import com.example.chat.RetrofitInstance
import com.example.chat.adapter.ChatAdapter
import com.example.chat.databinding.ActivityChatBinding
import com.example.chat.model.Chat
import com.example.chat.model.NotificationData
import com.example.chat.model.PushNotification
import com.example.chat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class ChatActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityChatBinding
    var firebaseUser:FirebaseUser?= null
    var reference:DatabaseReference ?= null
    var chatList =ArrayList<Chat>()
    var topic=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityChatBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        binding.chatRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayout.VERTICAL,false)
        var intent =getIntent()
        var userId=intent.getStringExtra("userId")
        var userName=intent.getStringExtra("userName")
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        firebaseUser=FirebaseAuth.getInstance().currentUser
        reference=FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        reference!!.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
              val user=snapshot.getValue(User::class.java)
                binding.tvuserName.text=user!!.userName
                if (user.profileImage=="")
                {
                    binding.imgProfile.setImageResource(R.drawable.ic_profile)
                }
                else

                {
                    Glide.with(this@ChatActivity).load(user.profileImage).into(binding.imgProfile)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        binding.btnSendMessage.setOnClickListener {
            var message:String=binding.etMessage.text.toString()
            if (message.isEmpty()){
                Toast.makeText(applicationContext,"message is empty",Toast.LENGTH_SHORT).show()
                binding.etMessage.setText("")
            }
            else{
                sendMessage(firebaseUser!!.uid,userId,message)
                binding.etMessage.setText("")
                topic="/topics/$userId"
                PushNotification(NotificationData(userName!!,message),topic).also {
                    sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser!!.uid,userId)
    }

    private fun sendMessage(sendId:String,receiveId:String,message:String){
        var reference:DatabaseReference? = FirebaseDatabase.getInstance().getReference()
        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("sendId",sendId)
        hashMap.put("receiveId",receiveId)
        hashMap.put("message",message)
        reference!!.child("chat").push().setValue(hashMap)

    }
    fun readMessage(sendId:String,receiveId:String){
        val databaseReference:DatabaseReference=
            FirebaseDatabase.getInstance().getReference("chat")
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot:DataSnapshot in snapshot.children){
                    val chat=dataSnapShot.getValue(Chat::class.java)
                    if (chat!!.sendId.equals(sendId) || chat!!.receiveId.equals(receiveId)
                        && chat!!.sendId.equals(receiveId) || chat!!.receiveId.equals(sendId)  ){
                        chatList.add(chat)
                    }
                }
                val chatAdapter=ChatAdapter(this@ChatActivity,chatList)
                binding.chatRecyclerView.adapter=chatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun sendNotification(notification:PushNotification)= CoroutineScope(Dispatchers.IO).launch {
        try {
            val response=RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Log.d("TAG","Response:${Gson().toJson(response)}")
            }
            else{
                Log.d("TAG",response.errorBody()!!.toString())
            }
        }
        catch (e:Exception){
            Log.e("TAG",e.toString())
        }
    }

}