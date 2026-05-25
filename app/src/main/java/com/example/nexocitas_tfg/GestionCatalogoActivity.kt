package com.example.nexocitas_tfg

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GestionCatalogoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var contenedor: LinearLayout

    private val listaEspacios = arrayOf(
        "Sala de Reuniones Ejecutiva",
        "Aula de Formación Nexo",
        "Estudio de Grabación & Podcast",
        "Escritorio Flex (Hot Desk)",
        "Escritorio Fijo Premium",
        "Cabina Privada (Phone Booth)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_catalogo)

        db = FirebaseFirestore.getInstance()
        contenedor = findViewById(R.id.contenedorInterruptores)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCatalogo)
        toolbar.setNavigationOnClickListener { finish() }

        cargarInterruptoresPremium()
    }

    private fun cargarInterruptoresPremium() {
        contenedor.removeAllViews()

        for (espacio in listaEspacios) {
            val icono = when(espacio) {
                "Sala de Reuniones Ejecutiva" -> "💼"
                "Aula de Formación Nexo" -> "👨‍🏫"
                "Estudio de Grabación & Podcast" -> "🎙️"
                "Escritorio Flex (Hot Desk)" -> "💻"
                "Escritorio Fijo Premium" -> "🖥️"
                "Cabina Privada (Phone Booth)" -> "📞"
                else -> "🏢"
            }

            val cardView = CardView(this)
            val cardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            cardParams.setMargins(0, 0, 0, 32)
            cardView.layoutParams = cardParams
            cardView.radius = 24f
            cardView.cardElevation = 8f
            cardView.setCardBackgroundColor(Color.WHITE)

            val layoutPrincipal = LinearLayout(this)
            layoutPrincipal.orientation = LinearLayout.HORIZONTAL
            layoutPrincipal.setPadding(40, 50, 40, 50)
            layoutPrincipal.gravity = Gravity.CENTER_VERTICAL
            layoutPrincipal.weightSum = 1f

            val layoutTextos = LinearLayout(this)
            layoutTextos.orientation = LinearLayout.VERTICAL
            val paramsTextos = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            layoutTextos.layoutParams = paramsTextos

            val tvNombre = TextView(this)
            tvNombre.text = "$icono $espacio"
            tvNombre.textSize = 16f
            tvNombre.setTypeface(null, Typeface.BOLD)
            tvNombre.setTextColor(Color.DKGRAY)
            layoutTextos.addView(tvNombre)

            val tvEstado = TextView(this)
            tvEstado.textSize = 13f
            tvEstado.setPadding(0, 8, 0, 0)
            layoutTextos.addView(tvEstado)

            layoutPrincipal.addView(layoutTextos)

            val interruptor = Switch(this)
            var esCargaInicial = true

            db.collection("catalogo_estados").document(espacio).get()
                .addOnSuccessListener { document ->
                    val estaActivo = if (document.exists()) document.getBoolean("activo") ?: true else true
                    interruptor.isChecked = estaActivo
                    actualizarDisenoVisual(interruptor, estaActivo, tvEstado, tvNombre)
                    esCargaInicial = false
                }

            interruptor.setOnCheckedChangeListener { _, isChecked ->
                // Actualizamos visuales siempre
                actualizarDisenoVisual(interruptor, isChecked, tvEstado, tvNombre)

                // Si es carga inicial, paramos aquí
                if (esCargaInicial) return@setOnCheckedChangeListener

                // Solo guardamos y avisamos si es acción manual
                val datos = hashMapOf("activo" to isChecked)
                db.collection("catalogo_estados").document(espacio).set(datos)

                if (!isChecked) {
                    publicarNotificacion("⚠️ Sala No Disponible", "El espacio '$espacio' ha sido desactivado temporalmente.")
                    Toast.makeText(this@GestionCatalogoActivity, "Aviso de cierre publicado", Toast.LENGTH_SHORT).show()
                } else {
                    publicarNotificacion("✅ Sala Disponible de Nuevo", "¡Buenas noticias! El espacio '$espacio' ya vuelve a estar operativo.")
                    Toast.makeText(this@GestionCatalogoActivity, "Aviso de reapertura publicado", Toast.LENGTH_SHORT).show()
                }
            }

            layoutPrincipal.addView(interruptor)
            cardView.addView(layoutPrincipal)
            contenedor.addView(cardView)
        }
    }

    private fun actualizarDisenoVisual(s: Switch, activo: Boolean, tvEstado: TextView, tvNombre: TextView) {
        if (activo) {
            tvEstado.text = "🟢 Visible en el catálogo"
            tvEstado.setTextColor(Color.parseColor("#4CAF50"))
            tvNombre.setTextColor(Color.DKGRAY)
        } else {
            tvEstado.text = "🔴 Oculto temporalmente"
            tvEstado.setTextColor(Color.parseColor("#F44336"))
            tvNombre.setTextColor(Color.GRAY)
        }
    }

    private fun publicarNotificacion(titulo: String, mensaje: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaFormateada = sdf.format(Date())

        val nuevaNotificacion = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "tipo" to "Global",
            "destinatario" to "todos",
            "fecha" to fechaFormateada,
            "fechaMillis" to System.currentTimeMillis(),
            "leidaPor" to emptyList<String>(),
            "borradaPor" to emptyList<String>()
        )
        db.collection("notificaciones").add(nuevaNotificacion)
    }
}
