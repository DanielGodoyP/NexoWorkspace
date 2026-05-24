package com.example.nexocitas_tfg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminReservasActivity : AppCompatActivity() {

    private lateinit var contenedor: LinearLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reservas)

        db = FirebaseFirestore.getInstance()
        contenedor = findViewById(R.id.contenedorTodasLasReservas)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAdmin)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGestionarCatalogo)
            .setOnClickListener { startActivity(Intent(this, GestionCatalogoActivity::class.java)) }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEnviarNotificacionAdmin)
            .setOnClickListener { startActivity(Intent(this, AdminMensajesActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        auditarReservasExpiradas()
    }

    private fun auditarReservasExpiradas() {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("reservas").get().addOnSuccessListener { documentos ->
            for (doc in documentos) {
                val fechaStr    = doc.getString("fecha") ?: ""
                val estadoActual = doc.getString("estado") ?: "Pendiente"
                if (fechaStr.isNotEmpty()
                    && !estadoActual.contains("Check-in")
                    && !estadoActual.contains("Liberada")
                    && !estadoActual.contains("Cancelada")) {
                    try {
                        val fechaReserva = sdf.parse(fechaStr)
                        if (fechaReserva != null && fechaReserva.before(hoy)) {
                            db.collection("reservas").document(doc.id)
                                .update("estado", "Liberada (Inasistencia)")
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
            cargarTodasLasReservas()
        }
    }

    private fun cargarTodasLasReservas() {
        db.collection("reservas").get().addOnSuccessListener { documentos ->
            contenedor.removeAllViews()

            if (documentos.isEmpty) {
                contenedor.addView(TextView(this).apply {
                    text = "No hay reservas registradas en el sistema."
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                })
                return@addOnSuccessListener
            }

            val sdfCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val listaOrdenada = documentos.documents.sortedWith { d1, d2 ->
                val t1 = obtenerTimestamp(d1.getString("fecha"), d1.getString("hora") ?: d1.getString("horaInicio"), sdfCompleto)
                val t2 = obtenerTimestamp(d2.getString("fecha"), d2.getString("hora") ?: d2.getString("horaInicio"), sdfCompleto)
                t1.compareTo(t2)
            }

            val pendientes  = mutableListOf<DocumentSnapshot>()
            val completadas = mutableListOf<DocumentSnapshot>()
            val canceladas  = mutableListOf<DocumentSnapshot>()

            for (doc in listaOrdenada) {
                val estado = (doc.getString("estado") ?: "Pendiente").lowercase()
                when {
                    estado.contains("completado") || estado.contains("asistida") -> completadas.add(doc)
                    estado.contains("liberada")   || estado.contains("cancelada") -> canceladas.add(doc)
                    else -> pendientes.add(doc)
                }
            }

            if (pendientes.isNotEmpty()) {
                val cont = crearDesplegable("⏳ Pendientes de llegada (${pendientes.size})", contenedor, true)
                pendientes.forEach { dibujarDoc(it, cont) }
            }
            if (completadas.isNotEmpty()) {
                val cont = crearDesplegable("✅ Check-in Completado (${completadas.size})", contenedor, false)
                completadas.forEach { dibujarDoc(it, cont) }
            }
            if (canceladas.isNotEmpty()) {
                val cont = crearDesplegable("🚫 Canceladas / Liberadas (${canceladas.size})", contenedor, false)
                canceladas.forEach { dibujarDoc(it, cont) }
            }
        }
    }

    private fun crearDesplegable(titulo: String, padre: LinearLayout, expandido: Boolean): LinearLayout {
        val headerCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 16f; cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#E8F0FE"))
        }

        val layoutInterno = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 32, 40, 32)
        }

        val tvTitulo = TextView(this).apply {
            text = titulo; textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvFlecha = TextView(this).apply {
            text = if (expandido) "▼" else "▶"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
        }

        layoutInterno.addView(tvTitulo); layoutInterno.addView(tvFlecha)
        headerCard.addView(layoutInterno)

        val layoutContenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 0, 16, 24)
            }
            visibility = if (expandido) View.VISIBLE else View.GONE
        }

        headerCard.setOnClickListener {
            if (layoutContenido.visibility == View.VISIBLE) {
                layoutContenido.visibility = View.GONE; tvFlecha.text = "▶"
            } else {
                layoutContenido.visibility = View.VISIBLE; tvFlecha.text = "▼"
            }
        }

        padre.addView(headerCard); padre.addView(layoutContenido)
        return layoutContenido
    }

    private fun dibujarDoc(doc: DocumentSnapshot, destino: LinearLayout) {
        val idReserva  = doc.id
        val estado     = doc.getString("estado") ?: "Pendiente"
        val sala       = doc.getString("sala") ?: doc.getString("nombreSala") ?: "Sala"
        val fecha      = doc.getString("fecha") ?: "Sin fecha"
        val horaInicio = doc.getString("horaInicio") ?: ""
        val horaFinDoc = doc.getString("horaFin") ?: ""
        val accesorios = doc.getString("accesorios") ?: "Sin extras"
        val email      = doc.getString("usuarioEmail") ?: doc.getString("email") ?: "Desconocido"
        val nombre     = doc.getString("nombreUsuario") ?: doc.getString("nombre") ?: email

        var hora = doc.getString("hora") ?: doc.getString("horaInicio") ?: "Sin hora"
        if (horaInicio.isNotEmpty() && horaFinDoc.isNotEmpty()) hora = "$horaInicio a $horaFinDoc"

        // ── Leer precio real de Firestore, igual que en MisReservasActivity ──
        val precioTotal: Double = when {
            doc.getDouble("total") != null        -> doc.getDouble("total")!!
            doc.getDouble("precioTotal") != null  -> doc.getDouble("precioTotal")!!
            doc.getLong("total") != null          -> doc.getLong("total")!!.toDouble()
            doc.getLong("precioTotal") != null    -> doc.getLong("precioTotal")!!.toDouble()
            else                                  -> 0.0
        }

        val costeExtras: Double = when {
            doc.getDouble("costeExtras") != null -> doc.getDouble("costeExtras")!!
            doc.getLong("costeExtras") != null   -> doc.getLong("costeExtras")!!.toDouble()
            else                                 -> 0.0
        }

        disenarTarjeta(
            idReserva, sala, fecha, hora, horaInicio, horaFinDoc,
            nombre, estado, accesorios, costeExtras, precioTotal, destino
        )
    }

    private fun obtenerTimestamp(fecha: String?, horaCompleta: String?, sdf: SimpleDateFormat): Long {
        if (fecha.isNullOrEmpty()) return 0L
        val horaLimpia = try { (horaCompleta ?: "00:00").split("a", "-")[0].trim() } catch (e: Exception) { "00:00" }
        return try { sdf.parse("$fecha $horaLimpia")?.time ?: 0L } catch (e: Exception) { 0L }
    }

    private fun disenarTarjeta(
        idReserva: String, sala: String, fecha: String, hora: String,
        horaInicio: String, horaFin: String, usuario: String, estado: String,
        accesorios: String, costeExtras: Double, precioTotal: Double,
        contenedorTarget: LinearLayout
    ) {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 24f; cardElevation = 4f
            setCardBackgroundColor(Color.WHITE)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Usuario
        layout.addView(TextView(this).apply {
            text = "👤 Usuario: $usuario"
            textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 12)
        })

        // Sala
        layout.addView(TextView(this).apply {
            text = "📖 Sala: $sala"
            textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 12)
        })

        // Fecha
        layout.addView(TextView(this).apply {
            text = "📅 Fecha: $fecha"
            textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 12)
        })

        // Horario
        layout.addView(TextView(this).apply {
            text = "⏰ Horario: $hora"
            textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 12)
        })

        // Extras
        layout.addView(TextView(this).apply {
            text = "🛎️ Extras: $accesorios"
            textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 12)
        })

        // Estado
        layout.addView(TextView(this).apply {
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
            when {
                estado.lowercase().contains("completado") || estado.lowercase() == "asistida" -> {
                    text = "✅ Estado: Asistió al espacio"
                    setTextColor(Color.parseColor("#4CAF50"))
                }
                estado.contains("Liberada") || estado.contains("Cancelada") -> {
                    text = "🚫 Estado: $estado"
                    setTextColor(Color.GRAY)
                }
                else -> {
                    text = "⏳ Estado: Pendiente de llegada"
                    setTextColor(Color.parseColor("#FF9800"))
                }
            }
        })

        // Divisor
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(Color.parseColor("#E0E0E0"))
        })

        // Título desglose
        layout.addView(TextView(this).apply {
            text = "🧾 COBROS CALCULADOS:"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.GRAY); setPadding(0, 0, 0, 8)
        })

        // Desglose base (precioTotal - costeExtras)
        val precioBase = precioTotal - costeExtras
        layout.addView(TextView(this).apply {
            text = "• Alquiler de espacio: ${String.format("%.2f", precioBase)}€"
            textSize = 14f; setTextColor(Color.parseColor("#333333")); setPadding(0, 0, 0, 4)
        })

        layout.addView(TextView(this).apply {
            text = "• Extras ($accesorios): ${String.format("%.2f", costeExtras)}€"
            textSize = 14f; setTextColor(Color.parseColor("#333333")); setPadding(0, 4, 0, 12)
        })

        // Etiqueta total
        layout.addView(TextView(this).apply {
            text = "TOTAL RECAUDADO:"; textSize = 12f; setTextColor(Color.GRAY)
        })

        // Precio total — leído de Firestore, siempre correcto
        layout.addView(TextView(this).apply {
            text = "${String.format("%.2f", precioTotal)} €"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8")); setPadding(0, 0, 0, 24)
        })

        // Botones Editar / Borrar (solo si no está cancelada ni liberada)
        if (!estado.contains("Liberada") && !estado.contains("Cancelada")) {
            val layoutBotones = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                weightSum = 2f
            }

            layoutBotones.addView(com.google.android.material.button.MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 16, 0)
                }
                text = "Editar"; isAllCaps = false; cornerRadius = 20
                setBackgroundColor(Color.parseColor("#FF9800")); setTextColor(Color.WHITE)
                setOnClickListener {
                    startActivity(Intent(this@AdminReservasActivity, EditarReservaActivity::class.java).apply {
                        putExtra("RESERVA_ID", idReserva)
                        putExtra("SALA_NOMBRE", sala)
                    })
                }
            })

            layoutBotones.addView(com.google.android.material.button.MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(16, 0, 0, 0)
                }
                text = "Borrar"; isAllCaps = false; cornerRadius = 20
                setBackgroundColor(Color.parseColor("#F44336")); setTextColor(Color.WHITE)
                setOnClickListener {
                    db.collection("reservas").document(idReserva).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@AdminReservasActivity, "Reserva eliminada", Toast.LENGTH_SHORT).show()
                            cargarTodasLasReservas()
                        }
                }
            })

            layout.addView(layoutBotones)
        }

        cardView.addView(layout)
        contenedorTarget.addView(cardView)
    }
}