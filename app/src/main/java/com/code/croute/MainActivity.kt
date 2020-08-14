package com.code.croute

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.code.croutefactory.CBuilder
import com.code.routeconstants.ComponentA
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        action1.setOnClickListener {
            CBuilder.create(this)
                .name(ComponentA.name)
                .action(ComponentA.action1)
                .params(mutableMapOf())
                .build()
        }
        action2.setOnClickListener {
            var rb = CBuilder.create(this)
                .name(ComponentA.name)
                .action(ComponentA.action2)
                .params(object : HashMap<String, String>() {
                    init {
                        put("p1", "1")
                        put("p2", "2")
                    }
                })
                .build()
            Log.e(MainActivity::class.java.simpleName, rb.toString())
        }
    }
}