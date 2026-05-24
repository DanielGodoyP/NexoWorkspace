package com.example.nexocitas_tfg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ContactoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        val ivVolverContacto = findViewById<ImageView>(R.id.ivVolverContacto)
        val etAsunto = findViewById<EditText>(R.id.etAsuntoContacto)
        val etMensaje = findViewById<EditText>(R.id.etMensajeContacto)
        val btnAbrirGmail = findViewById<MaterialButton>(R.id.btnAbrirGmail)

        ivVolverContacto.setOnClickListener {
            finish()
        }

        btnAbrirGmail.setOnClickListener {
            val asunto = etAsunto.text.toString().trim()
            val mensaje = etMensaje.text.toString().trim()

            if (asunto.isEmpty() || mensaje.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe un asunto y un mensaje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CREAMOS EL INTENT PARA ABRIR GMAIL
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:nexoworkspaceofficial@gmail.com") // Solo aplicaciones de correo lo abrirán
                putExtra(Intent.EXTRA_SUBJECT, asunto)
                putExtra(Intent.EXTRA_TEXT, mensaje)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No hay ninguna aplicación de correo instalada.", Toast.LENGTH_LONG).show()
            }
        }
    }
}