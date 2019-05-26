package com.simplekjl.letschat

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.simplekjl.letschat.adapters.MessageAdapter
import com.simplekjl.letschat.databinding.ActivityMainBinding
import com.simplekjl.letschat.models.CustomMessage
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMessagesDatabaseReference: DatabaseReference
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var mMessagesAdapter: MessageAdapter

    private lateinit var userName: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        userName = ANONYMOUS

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mMessagesDatabaseReference = mFirebaseDatabase.getReference("messages")

        // Initialize message ListView and its adapter
        val customMessages =  mutableListOf<CustomMessage>()
        mMessagesAdapter = MessageAdapter(this, R.layout.item_message, customMessages)

        //initilize progress bar
        viewBinding.progressBar.visibility = View.INVISIBLE

        //Image Picker button

        viewBinding.photoPickerButton.setOnClickListener {
            // TODO do something
        }

        //Sedn button
        viewBinding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // nothing to do
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewBinding.sendButton.isEnabled = s.toString().trim().isNotEmpty()
            }
        })
        // adding filters
        viewBinding.messageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))
        //JAVA equivalent
        //mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        //Send messages action
        viewBinding.sendButton.setOnClickListener { _ ->
            //TODO send data to the database
            viewBinding.messageEditText.setText("")
        }
    }
}
