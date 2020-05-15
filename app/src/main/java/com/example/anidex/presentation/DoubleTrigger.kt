package com.example.anidex.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

//src: https://stackoverflow.com/questions/49493772/mediatorlivedata-or-switchmap-transformation-with-multiple-parameters
class DoubleTrigger<A, B>(a: LiveData<A>, b: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(a) { value = it to b.value }
        addSource(b) { value = a.value to it }
    }
}