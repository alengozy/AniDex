package com.example.anidex.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class QuadTrigger<A, B, C, D>(a: LiveData<A>, b: LiveData<B>, c: LiveData<C>, d: LiveData<D>) : MediatorLiveData<Quad<A?, B?, C?, D?>>() {
    init {
        addSource(a) { value = Quad(it, b.value, c.value, d.value) }
        addSource(b) { value = Quad(a.value, it, c.value, d.value) }
        addSource(c) {value = Quad(a.value, b.value, it, d.value) }
        addSource(d) {value = Quad(a.value, b.value, c.value, it) }
    }
}
data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
){
    override fun toString(): String = "($first, $second, $third, $fourth)"
}