package com.example.chat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.chat.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityLoginBinding
    private  var auth: FirebaseAuth?=null
    private  var firebaseUser: FirebaseUser?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        auth= FirebaseAuth.getInstance()
        firebaseUser=auth!!.currentUser
        if (firebaseUser!=null)
        {
            val intent=Intent(this@LoginActivity, UsersActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            val email=binding.edtEmail.editText?.text.toString()
            val password=binding.edtPassword.editText?.text.toString()
            if (TextUtils.isEmpty(email)&&TextUtils.isEmpty(password))
            {
                Toast.makeText(applicationContext,"email and password are required",Toast.LENGTH_SHORT).show()

            }
            else{
                auth!!.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){
                    if (it.isSuccessful){
                        binding.edtEmail.editText?.setText("")
                        binding.edtPassword.editText?.setText("")
                        val intent=Intent(this@LoginActivity, UsersActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(applicationContext,"email or password invalid ",Toast.LENGTH_SHORT).show()

                    }


                }
            }
        }
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }


    }
}