package com.example.nexocitas_tfg

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PagoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        val precioTotal = intent.getStringExtra("PRECIO_TOTAL") ?: "0"

        val tvTotalAPagar = findViewById<TextView>(R.id.tvTotalAPagar)
        tvTotalAPagar.text = "$precioTotal €"

        val ivVolverPago = findViewById<ImageView>(R.id.ivVolverPago)
        val btnConfirmarPago = findViewById<MaterialButton>(R.id.btnConfirmarPago)

        val etTarjeta = findViewById<EditText>(R.id.etNumeroTarjeta)
        val etCaducidad = findViewById<EditText>(R.id.etCaducidad)
        val etCVV = findViewById<EditText>(R.id.etCVV)

        val layoutCargando = findViewById<LinearLayout>(R.id.layoutCargandoPago)

        // 1. FORZAR TECLADO (El de caducidad lo ponemos en DATETIME para que admita la barra '/')
        etTarjeta.inputType = InputType.TYPE_CLASS_NUMBER
        etCaducidad.inputType = InputType.TYPE_CLASS_DATETIME
        etCVV.inputType = InputType.TYPE_CLASS_NUMBER

        // 2. AUTO-FORMATO PARA LA FECHA DE CADUCIDAD (Añade la "/" automáticamente)
        etCaducidad.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private var hyphenStart = 0

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (count > 0 && s[start] == '/') {
                    deletingHyphen = true
                    hyphenStart = start
                } else {
                    deletingHyphen = false
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (isFormatting) return
                isFormatting = true

                // Si el usuario borra la barra, borramos también el número anterior
                if (deletingHyphen && hyphenStart > 0) {
                    s.delete(hyphenStart - 1, hyphenStart)
                }

                // Quitamos todo lo que no sea número
                var numbersOnly = s.toString().replace("[^\\d]".toRegex(), "")

                // Limitamos a 4 números máximo (MMYY)
                if (numbersOnly.length > 4) {
                    numbersOnly = numbersOnly.substring(0, 4)
                }

                // Construimos el texto con la barra
                val formatted = StringBuilder()
                for (i in numbersOnly.indices) {
                    formatted.append(numbersOnly[i])
                    if (i == 1 && numbersOnly.length > 2) {
                        formatted.append("/")
                    }
                }

                s.replace(0, s.length, formatted.toString())
                isFormatting = false
            }
        })

        // Controlar la flecha de volver (Arriba a la izquierda)
        ivVolverPago.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // Controlar el botón/gesto nativo de "Atrás" en Android
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        btnConfirmarPago.setOnClickListener {
            val tarjeta = etTarjeta.text.toString().trim()
            val caducidad = etCaducidad.text.toString().trim()
            val cvv = etCVV.text.toString().trim()

            if (tarjeta.length < 16) {
                Toast.makeText(this, "El número de tarjeta debe tener 16 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hacemos la validación más inteligente: quitamos la barra para contar solo los números
            val caducidadSoloNumeros = caducidad.replace("/", "")
            if (caducidadSoloNumeros.length < 4) {
                Toast.makeText(this, "Caducidad inválida (Usa MM/AA)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvv.length < 3) {
                Toast.makeText(this, "Rellena el CVV correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            layoutCargando.visibility = View.VISIBLE
            btnConfirmarPago.isEnabled = false
            ivVolverPago.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "¡Pago autorizado con éxito!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }, 2000)
        }
    }
}