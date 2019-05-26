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
    objects: MutableList<CustomMessage>
) : ArrayAdapter<CustomMessage>(context, resource, objects) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View = (context as Activity).layoutInflater.inflate(R.layout.item_message, parent, false)

        val photoImageView = view.findViewById(R.id.photoImageView) as ImageView
        val messageTextView = view.findViewById(R.id.messageTextView) as TextView
        val authorTextView = view.findViewById(R.id.nameTextView) as TextView

        val message = getItem(position)

        val isPhoto = message?.photoURL != null
        if (isPhoto) {
            messageTextView.visibility = View.GONE
            photoImageView.visibility = View.VISIBLE
            Glide.with(photoImageView.context)
                .load(message?.photoURL)
                .into(photoImageView)
        } else {
            messageTextView.visibility = View.VISIBLE
            photoImageView.visibility = View.GONE
            messageTextView.text = message?.text
        }
        authorTextView.text = message?.name

        return view
    }


    class MessageViewHolder
}