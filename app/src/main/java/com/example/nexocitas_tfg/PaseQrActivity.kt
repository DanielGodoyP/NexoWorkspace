package com.example.nexocitas_tfg

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class PaseQrActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pase_qr)

        db = FirebaseFirestore.getInstance()

        val idReserva = intent.getStringExtra("RESERVA_ID") ?: "0000"
        val sala = intent.getStringExtra("SALA_NOMBRE") ?: "Espacio"
        val fecha = intent.getStringExtra("FECHA") ?: "--/--/----"
        val horario = intent.getStringExtra("HORARIO") ?: "--:--"

        val tvSala = findViewById<TextView>(R.id.tvSalaQR)
        val tvFecha = findViewById<TextView>(R.id.tvFechaQR)
        val tvHora = findViewById<TextView>(R.id.tvHoraQR)
        val ivQR = findViewById<ImageView>(R.id.ivCodigoQR)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolverPase)
        val btnSimularTorno = findViewById<MaterialButton>(R.id.btnSimularTorno)

        tvSala.text = "📖 $sala"
        tvFecha.text = "📅 Fecha: $fecha"
        tvHora.text = "⏰ Horario: $horario"

        btnVolver.setOnClickListener { finish() }

        // ✨ NUEVA LÓGICA: Simulación de escaneo para el TFG
        btnSimularTorno.setOnClickListener {
            // Desactivamos el botón para evitar dobles clics
            btnSimularTorno.isEnabled = false

            db.collection("reservas").document(idReserva).update("estado", "Check-in completado")
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Acceso validado! Torniquete abierto ✅", Toast.LENGTH_LONG).show()
                    finish() // Volvemos a Mis Reservas, que se recargará automáticamente
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error de red al conectar con el torno", Toast.LENGTH_SHORT).show()
                    btnSimularTorno.isEnabled = true
                }
        }

        try {
            val datosEncriptadosParaTorno = "NEXOWORKSPACE_ACCESO_VALIDO\n" +
                    "ID_RESERVA: $idReserva\n" +
                    "ESPACIO: $sala\n" +
                    "DIA: $fecha\n" +
                    "HORAS: $horario\n" +
                    "STATUS: APAGADO_Y_CONFIRMADO"

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(datosEncriptadosParaTorno, BarcodeFormat.QR_CODE, 512, 512)

            val ancho = bitMatrix.width
            val alto = bitMatrix.height
            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)

            for (x in 0 until ancho) {
                for (y in 0 until alto) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }

            ivQR.setImageBitmap(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}