package com.example.contactsavingapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModel(
    private val dao:ContactDao
):ViewModel() {
    private val _state= MutableStateFlow(ContactState()) //private immutable version of state
    private val _sortType= MutableStateFlow(SortType.FIRST_NAME)
    private val _contacts=_sortType.flatMapLatest { sortType ->
        when(sortType){
            SortType.FIRST_NAME -> dao.getOrderedByFirstName()
            SortType.LAST_NAME -> dao.getOrderedByLastName()
            SortType.PHONE_NUM -> dao.getOrderedByPhNos()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val state = combine(_state, _sortType, _contacts){state, sortType, contacts ->//This combines all 3 flows into a single flow. As soon as any of this flowtype changes then this block of code gets executed.
        state.copy(
            contacts=contacts,
            sortType=sortType,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactState())


    fun onEvent(event: ContactEvent){
        when(event){
            is ContactEvent.DeleteContact -> {
                viewModelScope.launch{
                    dao.deleteContact(event.contact)
                }

            }
            ContactEvent.HideDialog -> {
                _state.update { it.copy(
                    isAddingContact = false
                )}
            }
            ContactEvent.SaveContact -> {
                val firstName=state.value.firstName
                val lastName= state.value.lastName
                val phoneNum=state.value.phoneNum

                if(firstName.isBlank() || lastName.isBlank() || phoneNum.isBlank()){
                    return
                }
                val contact= Contact(
                    firstName=firstName,
                    lastName=lastName,
                    phoneNo=phoneNum
                )
                viewModelScope.launch {
                    dao.insertContact(contact)
                }
                _state.update { it.copy(
                    isAddingContact = false,
                    firstName="",
                    lastName="",
                    phoneNum = ""
                ) }
            }
            is ContactEvent.SetFirstName -> {
                _state.update{ it.copy(
                    firstName = event.firstName
                )}
            }
            is ContactEvent.SetLastName -> {
                _state.update{ it.copy(
                    lastName = event.lastName
                )}
            }
            is ContactEvent.SetPhoneNumber -> {
                _state.update{ it.copy(
                    phoneNum = event.phoneNumber
                )}
            }
            ContactEvent.ShowDialog -> {
                _state.update{ it.copy(
                    isAddingContact = true
                )}
            }
            is ContactEvent.SortContact -> {
                _sortType.value= event.sortType
            }
        }
    }
}