package com.gmail.suneclips3

class CallingDummyNonTest(private val dummy: DummyNonTest = DummyNonTest()) {

    fun callDummyNonTest() = dummy.callDoIt()
}