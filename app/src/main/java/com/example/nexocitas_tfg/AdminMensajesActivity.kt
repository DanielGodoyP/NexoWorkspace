package com.example.nexocitas_tfg

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminMensajesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_mensajes)

        db = FirebaseFirestore.getInstance()

        val ivVolver        = findViewById<ImageView>(R.id.ivVolverAdminMensajes)
        val rgDestinatario  = findViewById<RadioGroup>(R.id.rgDestinatario)
        val tilEmail        = findViewById<TextInputLayout>(R.id.tilEmailCliente)
        val etEmailCliente  = findViewById<TextInputEditText>(R.id.etEmailCliente)
        val etTitulo        = findViewById<TextInputEditText>(R.id.etTituloMensaje)
        val etCuerpo        = findViewById<TextInputEditText>(R.id.etCuerpoMensaje)
        val btnEnviar       = findViewById<MaterialButton>(R.id.btnEnviarMensaje)

        ivVolver.setOnClickListener { finish() }

        rgDestinatario.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbIndividual) {
                tilEmail.visibility = View.VISIBLE
            } else {
                tilEmail.visibility = View.GONE
                etEmailCliente.text?.clear()
            }
        }

        btnEnviar.setOnClickListener {
            val titulo      = etTitulo.text.toString().trim()
            val cuerpo      = etCuerpo.text.toString().trim()
            val esIndividual = rgDestinatario.checkedRadioButtonId == R.id.rbIndividual
            val emailDestino = etEmailCliente.text.toString().trim()

            if (titulo.isEmpty() || cuerpo.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena el título y el mensaje.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (esIndividual && emailDestino.isEmpty()) {
                Toast.makeText(this, "Debes indicar el email del cliente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEnviar.isEnabled = false

            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaActual  = formatoFecha.format(Date())
            val destinatarioFinal = if (esIndividual) emailDestino else "todos"

            val nuevaNotificacion = hashMapOf(
                "titulo"       to titulo,
                "mensaje"      to cuerpo,
                "fecha"        to fechaActual,
                "destinatario" to destinatarioFinal,
                "leidaPor"     to emptyList<String>()
            )

            db.collection("notificaciones").add(nuevaNotificacion)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Mensaje enviado correctamente!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al enviar el mensaje.", Toast.LENGTH_SHORT).show()
                    btnEnviar.isEnabled = true
                }
        }
    }
}