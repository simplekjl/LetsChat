package com.simplekjl.letschat.models

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

// to have an empty constructor in Kotlin we assign the values
@IgnoreExtraProperties
@Parcelize
data class CustomMessage(val text: String? = null, val name: String? = null, val photoURL: String? = null) : Parcelable