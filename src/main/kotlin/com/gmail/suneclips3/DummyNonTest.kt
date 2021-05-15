package com.gmail.suneclips3

class DummyNonTest {


    private fun doIt(){
        throw RuntimeException()
    }
    public fun callDoIt() = doIt()
}