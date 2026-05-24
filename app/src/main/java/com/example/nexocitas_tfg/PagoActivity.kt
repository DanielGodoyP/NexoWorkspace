package com.example.nexocitas_tfg

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PagoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        // Recibir el precio total de la pantalla anterior
        val precioTotal = intent.getStringExtra("PRECIO_TOTAL") ?: "0"

        val tvTotalAPagar = findViewById<TextView>(R.id.tvTotalAPagar)
        tvTotalAPagar.text = "$precioTotal €"

        val ivVolverPago = findViewById<ImageView>(R.id.ivVolverPago)
        val btnConfirmarPago = findViewById<MaterialButton>(R.id.btnConfirmarPago)

        val etTarjeta = findViewById<EditText>(R.id.etNumeroTarjeta)
        val etCaducidad = findViewById<EditText>(R.id.etCaducidad)
        val etCVV = findViewById<EditText>(R.id.etCVV)

        val layoutCargando = findViewById<LinearLayout>(R.id.layoutCargandoPago)

        ivVolverPago.setOnClickListener {
            // Si le da a volver, cancelamos el pago
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnConfirmarPago.setOnClickListener {
            val tarjeta = etTarjeta.text.toString().trim()
            val caducidad = etCaducidad.text.toString().trim()
            val cvv = etCVV.text.toString().trim()

            // Validaciones básicas para que parezca real
            if (tarjeta.length < 16) {
                Toast.makeText(this, "El número de tarjeta debe tener 16 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (caducidad.isEmpty() || cvv.length < 3) {
                Toast.makeText(this, "Rellena la caducidad y el CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ocultar teclado y mostrar pantalla de carga
            layoutCargando.visibility = View.VISIBLE
            btnConfirmarPago.isEnabled = false

            // SIMULAR ESPERA DEL BANCO (2 segundos)
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "¡Pago autorizado con éxito!", Toast.LENGTH_LONG).show()

                // Le decimos a la pantalla anterior que el pago ha ido bien
                setResult(Activity.RESULT_OK)
                finish()
            }, 2000)
        }
    }
}