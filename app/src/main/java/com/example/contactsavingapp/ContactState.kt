package com.example.contactsavingapp

data class ContactState(
    val contacts: List<Contact> = emptyList(),
    val firstName:String="",
    val lastName:String="",
    val phoneNum:String="",
    val isAddingContact:Boolean=false,
    val sortType:SortType=SortType.FIRST_NAME
)
