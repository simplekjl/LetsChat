package com.simplekjl.letschat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simplekjl.letschat.adapters.MessageAdapter
import com.simplekjl.letschat.databinding.ActivityMainBinding
import com.simplekjl.letschat.models.CustomMessage
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000
    val RC_SIGN_IN: Int = 12
    val RC_PHOTO_PICKER: Int = 2
    //firebase
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMessagesDatabaseReference: DatabaseReference
    private var mChildEventListener: ChildEventListener? = null
    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mChatStorageReference: StorageReference
    //activity
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var mMessagesAdapter: MessageAdapter


    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        userName = ANONYMOUS

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()


        mMessagesDatabaseReference = mFirebaseDatabase.getReference("messages")
        mChatStorageReference = mFirebaseStorage.reference.child("chat_photos")


        // Initialize message ListView and its adapter
        val customMessages = mutableListOf<CustomMessage>()
        mMessagesAdapter = MessageAdapter(this, R.layout.item_message, customMessages)
        viewBinding.messageListView.adapter = mMessagesAdapter

        //initialize progress bar
        viewBinding.progressBar.visibility = View.INVISIBLE

        //Image Picker button

        viewBinding.photoPickerButton.setOnClickListener {
            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
        }

        //Send button
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

            var message = CustomMessage(viewBinding.messageEditText.text.toString(), userName, null)
            mMessagesDatabaseReference.push().setValue(message)
            viewBinding.messageEditText.setText("")
        }


        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            var user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                // user signed in
                //Toast.makeText(applicationContext, "Welcome, you've signed in.", Toast.LENGTH_LONG).show()
                onSignedInInitilized(user.displayName.toString())
            } else {
                startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                        ).build(), RC_SIGN_IN
                )
            }
        }
    }

    private fun onSignedInInitilized(name: String) {
        userName = name
        attachDatabaseListener()
    }

    private fun onSignedOutCleanUp() {
        userName = ANONYMOUS
        mMessagesAdapter.clear()
        detachDatabaseListener()
    }

    private fun attachDatabaseListener() {
        if (mChildEventListener == null) {
            // listener to our database
            mChildEventListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    var customMessage = p0.getValue(CustomMessage::class.java)
                    //  var customMessage  = p0.value as? CustomMessage
                    mMessagesAdapter.add(customMessage)
                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }
            }
            // adding the listener to the reference
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener!!)
        }
    }

    private fun detachDatabaseListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener!!)
            mChildEventListener = null
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        mAuthStateListener?.let {
            mFirebaseAuth.removeAuthStateListener(it)
        }
        mMessagesAdapter.clear()
        detachDatabaseListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // case for sign in
        if (resultCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Signed out!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            var selectedImageUri: Uri = data?.data ?: Uri.EMPTY
            var photoRef: StorageReference =
                mChatStorageReference.child(selectedImageUri.lastPathSegment)
            //upload the file to Firebase
            var uploadTask = photoRef.putFile(selectedImageUri)

            //register observers to listen for when the download is done or if it falls
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation photoRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    var customMessage = CustomMessage(null, userName, downloadUri.toString())
                    mMessagesDatabaseReference.push().setValue(customMessage)
                } else {
                    // Handle failures
                    Toast.makeText(
                        applicationContext, "Due connection issues your upload will continue once your " +
                                "ethernet connection comes back", Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.signOut -> {
                AuthUI.getInstance().signOut(this)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
