package com.gmail.suneclips3.tests

import java.lang.reflect.Method

//   TestNG seems to have a bug: not resolving method arguments properly when using reflection package classes with a data provider
//   So this wrapper is a workaround
class Arguments(val testClass: Class<*>, val testMethod: Method, val expectedKey: String?)