package com.code.componenta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.code.routeconstants.ComponentA
import java.lang.Exception

class ComponentAActivity : AppCompatActivity() {

    companion object {
        fun getResult(params: MutableMap<String, String>): Int {
            if (params.isNullOrEmpty()) {
                return -1
            }
            try {
                var p1 = params.get(ComponentA.p1)?.toInt()
                var p2 = params.get(ComponentA.p2)?.toInt()
                return p1!! + p2!!
            } catch (e: Exception) {
            }
            return -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_componenta)
    }
}