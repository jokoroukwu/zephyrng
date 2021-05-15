package com.gmail.suneclips3.sender

import java.lang.RuntimeException

class ZephyrException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable?) : super(message, exception)
}