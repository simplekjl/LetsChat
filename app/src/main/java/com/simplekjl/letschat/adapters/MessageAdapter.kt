package com.simplekjl.letschat.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.simplekjl.letschat.R
import com.simplekjl.letschat.models.CustomMessage


class MessageAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    objects: MutableList<CustomMessage>
) : ArrayAdapter<CustomMessage>(context, resource, textViewResourceId, objects) {


    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {


        val photoImageView = convertView.findViewById(R.id.photoImageView) as ImageView
        val messageTextView = convertView.findViewById(R.id.messageTextView) as TextView
        val authorTextView = convertView.findViewById(R.id.nameTextView) as TextView

        val message = getItem(position)

        val isPhoto = message.photoURL != null
        if (isPhoto) {
            messageTextView.visibility = View.GONE
            photoImageView.visibility = View.VISIBLE
            Glide.with(photoImageView.context)
                .load(message.photoURL)
                .into(photoImageView)
        } else {
            messageTextView.visibility = View.VISIBLE
            photoImageView.visibility = View.GONE
            messageTextView.setText(message.text)
        }
        authorTextView.setText(message.name)

        return convertView
    }
}