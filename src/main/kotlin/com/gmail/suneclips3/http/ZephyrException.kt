package com.gmail.suneclips3.http

class ZephyrException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable?) : super(message, exception)
}