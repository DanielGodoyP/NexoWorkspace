package com.example.nexocitas_tfg

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SobreNosotrosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_nosotros) // Crea este XML básico
        findViewById<ImageView>(R.id.ivVolverSobre).setOnClickListener { finish() }
    }
}