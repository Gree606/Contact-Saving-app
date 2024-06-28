package com.example.contactsavingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    val firstName:String,
    val lastName:String,
    val phoneNo:String,
    @PrimaryKey(autoGenerate = true)
    val id:Int=0
)
