package com.xinyanruanjian.imagemapview.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.xinyanruanjian.imagemapview.ImageMapView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageMapView>(R.id.imv).setOnAreaClickListener { imv, area ->
            Log.i(javaClass.simpleName, "Area was clicked: $area")
            Toast.makeText(this, "${area.name} was clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}