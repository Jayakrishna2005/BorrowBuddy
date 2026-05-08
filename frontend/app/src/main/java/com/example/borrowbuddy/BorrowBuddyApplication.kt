package com.example.borrowbuddy

import android.app.Application
import android.content.Context

class BorrowBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: BorrowBuddyApplication? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
