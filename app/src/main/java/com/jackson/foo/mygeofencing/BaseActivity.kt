package com.jackson.foo.mygeofencing

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    fun getRepository() = (application as MyGeofencingApp).getRepository()
}