package com.simplekjl.letschat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.simplekjl.letschat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var  mFirebaseDatabase : FirebaseDatabase
    private lateinit var mMessagesDatabaseReference: DatabaseReference
    private lateinit var viewBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mMessagesDatabaseReference = mFirebaseDatabase.getReference("messages")

    }
}
