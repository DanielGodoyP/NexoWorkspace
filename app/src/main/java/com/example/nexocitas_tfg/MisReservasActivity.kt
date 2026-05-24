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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MisReservasActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var contenedorFuturas: LinearLayout
    private lateinit var contenedorPasadas: LinearLayout

    private val zonaHorariaEspana = TimeZone.getTimeZone("Europe/Madrid")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_reservas)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        contenedorFuturas = findViewById(R.id.contenedorFuturas)
        contenedorPasadas = findViewById(R.id.contenedorPasadas)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMisReservas)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_mis_reservas
        verificarRolUsuario(bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_admin -> {
                    startActivity(Intent(this, AdminReservasActivity::class.java))
                    true
                }
                R.id.nav_mis_reservas -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMisReservas()
    }

    private fun cargarMisReservas() {
        val emailUsuario = auth.currentUser?.email ?: ""
        if (emailUsuario.isEmpty()) return

        db.collection("reservas").get()
            .addOnSuccessListener { documentos ->
                contenedorFuturas.removeAllViews()
                contenedorPasadas.removeAllViews()

                val hoy = Calendar.getInstance(zonaHorariaEspana).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.timeZone = zonaHorariaEspana
                val sdfCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                    timeZone = zonaHorariaEspana
                }

                val listaOrdenada = documentos.documents.sortedWith { d1, d2 ->
                    val t1 = obtenerTimestampReserva(d1.getString("fecha"), d1.getString("hora") ?: d1.getString("horaInicio"), sdfCompleto)
                    val t2 = obtenerTimestampReserva(d2.getString("fecha"), d2.getString("hora") ?: d2.getString("horaInicio"), sdfCompleto)
                    t1.compareTo(t2)
                }

                val futuras    = mutableListOf<DocumentSnapshot>()
                val completadas = mutableListOf<DocumentSnapshot>()
                val canceladas  = mutableListOf<DocumentSnapshot>()

                for (doc in listaOrdenada) {
                    val userReserva = doc.getString("usuarioEmail")
                        ?: doc.getString("usuario")
                        ?: doc.getString("email")
                        ?: ""
                    if (userReserva.lowercase() != emailUsuario.lowercase()) continue

                    val idReserva   = doc.id
                    val fechaStr    = doc.getString("fecha") ?: "Sin fecha"
                    val estadoActual = doc.getString("estado") ?: "Confirmada"

                    var hora = doc.getString("hora") ?: doc.getString("horas") ?: "Sin hora"
                    val horaInicio  = doc.getString("horaInicio")
                    val horaFinDoc  = doc.getString("horaFin")
                    if (!horaInicio.isNullOrEmpty() && !horaFinDoc.isNullOrEmpty()) {
                        hora = "$horaInicio a $horaFinDoc"
                    }

                    var estadoFinal = estadoActual
                    var esFutura = true

                    try {
                        if (fechaStr.isNotEmpty() && fechaStr != "Sin fecha") {
                            val fechaReserva = sdf.parse(fechaStr)
                            if (fechaReserva != null) {
                                if (fechaReserva.before(hoy)) {
                                    esFutura = false
                                    if (!estadoActual.contains("Check-in") &&
                                        !estadoActual.contains("Liberada") &&
                                        !estadoActual.contains("Cancelada")) {
                                        estadoFinal = "Liberada (Inasistencia)"
                                        db.collection("reservas").document(idReserva).update("estado", estadoFinal)
                                    }
                                } else if (fechaStr == sdf.format(Calendar.getInstance(zonaHorariaEspana).time)) {
                                    try {
                                        val partes = hora.split("a", "-")
                                        val horaFinStr = if (partes.size > 1) partes[1].trim() else partes[0].trim()
                                        val horaFinParts = horaFinStr.split(":")
                                        if (horaFinParts.size >= 2) {
                                            val hFin = horaFinParts[0].trim().toInt()
                                            val mFin = horaFinParts[1].trim().toInt()
                                            val calAhora = Calendar.getInstance(zonaHorariaEspana)
                                            if (calAhora.get(Calendar.HOUR_OF_DAY) > hFin ||
                                                (calAhora.get(Calendar.HOUR_OF_DAY) == hFin && calAhora.get(Calendar.MINUTE) >= mFin)) {
                                                esFutura = false
                                                if (!estadoActual.contains("Check-in") &&
                                                    !estadoActual.contains("Liberada") &&
                                                    !estadoActual.contains("Cancelada")) {
                                                    estadoFinal = "Liberada (Inasistencia)"
                                                    db.collection("reservas").document(idReserva).update("estado", estadoFinal)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {}
                                }
                            }
                        }
                    } catch (e: Exception) {}

                    if (estadoFinal.lowercase().contains("cancelada") ||
                        estadoFinal.lowercase().contains("liberada") ||
                        estadoFinal.lowercase().contains("completado") ||
                        estadoFinal.lowercase().contains("asistida")) {
                        esFutura = false
                    }

                    if (esFutura) {
                        futuras.add(doc)
                    } else {
                        if (estadoFinal.lowercase().contains("completado") || estadoFinal.lowercase().contains("asistida")) {
                            completadas.add(0, doc)
                        } else {
                            canceladas.add(0, doc)
                        }
                    }
                }

                if (futuras.isEmpty()) {
                    val tv = TextView(this)
                    tv.text = "No tienes próximas citas reservadas."
                    tv.setTextColor(Color.GRAY)
                    tv.setPadding(16, 16, 16, 16)
                    contenedorFuturas.addView(tv)
                } else {
                    futuras.forEach { dibujarDoc(it, contenedorFuturas, true) }
                }

                if (completadas.isEmpty() && canceladas.isEmpty()) {
                    val tv = TextView(this)
                    tv.text = "Tu historial de reservas está vacío."
                    tv.setTextColor(Color.GRAY)
                    tv.setPadding(16, 16, 16, 16)
                    contenedorPasadas.addView(tv)
                } else {
                    if (completadas.isNotEmpty()) {
                        val cont = crearDesplegable("✅ Historial Asistido (${completadas.size})", contenedorPasadas, false)
                        completadas.forEach { dibujarDoc(it, cont, false) }
                    }
                    if (canceladas.isNotEmpty()) {
                        val cont = crearDesplegable("🚫 Cancelaciones / Inasistencias (${canceladas.size})", contenedorPasadas, false)
                        canceladas.forEach { dibujarDoc(it, cont, false) }
                    }
                }
            }
    }

    private fun crearDesplegable(titulo: String, padre: LinearLayout, expandido: Boolean): LinearLayout {
        val headerCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 16f
            cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#E8F0FE"))
        }

        val layoutInterno = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 32, 40, 32)
        }

        val tvTitulo = TextView(this).apply {
            text = titulo
            textSize = 15f
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

        layoutInterno.addView(tvTitulo)
        layoutInterno.addView(tvFlecha)
        headerCard.addView(layoutInterno)

        val layoutContenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 0, 16, 16)
            }
            visibility = if (expandido) View.VISIBLE else View.GONE
        }

        headerCard.setOnClickListener {
            if (layoutContenido.visibility == View.VISIBLE) {
                layoutContenido.visibility = View.GONE
                tvFlecha.text = "▶"
            } else {
                layoutContenido.visibility = View.VISIBLE
                tvFlecha.text = "▼"
            }
        }

        padre.addView(headerCard)
        padre.addView(layoutContenido)
        return layoutContenido
    }

    private fun dibujarDoc(doc: DocumentSnapshot, destino: LinearLayout, esFutura: Boolean) {
        val idReserva  = doc.id
        val estado     = doc.getString("estado") ?: "Confirmada"
        val sala       = doc.getString("sala") ?: doc.getString("nombreSala") ?: "Sala"
        val fechaStr   = doc.getString("fecha") ?: "Sin fecha"
        val horaInicio = doc.getString("horaInicio") ?: ""
        val horaFinDoc = doc.getString("horaFin") ?: ""
        val accesorios = doc.getString("accesorios") ?: "Sin extras"

        var hora = doc.getString("hora") ?: doc.getString("horas") ?: "Sin hora"
        if (horaInicio.isNotEmpty() && horaFinDoc.isNotEmpty()) {
            hora = "$horaInicio a $horaFinDoc"
        }

        // ── PRECIO: leer directamente de Firestore, sin recalcular ────────
        // Puede estar guardado como Double (total) o Long (precioTotal)
        val precioTotal: Double = when {
            doc.getDouble("total") != null           -> doc.getDouble("total")!!
            doc.getDouble("precioTotal") != null     -> doc.getDouble("precioTotal")!!
            doc.getLong("total") != null             -> doc.getLong("total")!!.toDouble()
            doc.getLong("precioTotal") != null       -> doc.getLong("precioTotal")!!.toDouble()
            else                                     -> 0.0
        }

        // Coste de extras (solo para el desglose visual)
        val costeExtras: Double = when {
            doc.getDouble("costeExtras") != null -> doc.getDouble("costeExtras")!!
            doc.getLong("costeExtras") != null   -> doc.getLong("costeExtras")!!.toDouble()
            else                                 -> 0.0
        }

        disenarTarjeta(
            idReserva, sala, fechaStr, hora, horaInicio, horaFinDoc,
            estado, accesorios, costeExtras, precioTotal, destino, esFutura
        )
    }

    private fun obtenerTimestampReserva(fecha: String?, horaCompleta: String?, sdf: SimpleDateFormat): Long {
        if (fecha.isNullOrEmpty()) return 0L
        val horaLimpia = try {
            val h = horaCompleta ?: "00:00"
            h.split("a", "-")[0].trim()
        } catch (e: Exception) { "00:00" }
        return try { sdf.parse("$fecha $horaLimpia")?.time ?: 0L } catch (e: Exception) { 0L }
    }

    private fun disenarTarjeta(
        idReserva: String, sala: String, fecha: String, hora: String,
        horaInicio: String, horaFin: String, estado: String,
        accesorios: String, costeExtras: Double, precioTotal: Double,
        contenedorTarget: LinearLayout, esFutura: Boolean
    ) {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 32)
            }
            radius = 24f
            cardElevation = 6f
            setCardBackgroundColor(Color.WHITE)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Sala
        layout.addView(TextView(this).apply {
            text = "📖 Espacio: $sala"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 12)
        })

        // Fecha
        layout.addView(TextView(this).apply {
            text = "📅 Fecha: $fecha"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 12)
        })

        // Horario
        layout.addView(TextView(this).apply {
            text = "⏰ Horario: $hora"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 12)
        })

        // Estado
        layout.addView(TextView(this).apply {
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
            when {
                estado.lowercase().contains("completado") || estado.lowercase() == "asistida" -> {
                    text = "✅ Estado: Check-in Completado"
                    setTextColor(Color.parseColor("#4CAF50"))
                }
                estado.lowercase().contains("liberada") || estado.lowercase().contains("cancelada") -> {
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
            text = "🧾 DESGLOSE DE PAGO:"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.GRAY)
            setPadding(0, 0, 0, 8)
        })

        // Coste extras (informativo)
        val precioBase = precioTotal - costeExtras
        layout.addView(TextView(this).apply {
            text = "• Alquiler de espacio: ${String.format("%.2f", precioBase)}€"
            textSize = 14f
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, 0, 0, 4)
        })

        layout.addView(TextView(this).apply {
            text = "• Extras ($accesorios): ${String.format("%.2f", costeExtras)}€"
            textSize = 14f
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, 4, 0, 12)
        })

        // Etiqueta precio total
        layout.addView(TextView(this).apply {
            text = "PRECIO TOTAL:"
            textSize = 12f
            setTextColor(Color.GRAY)
        })

        // Precio total — leído de Firestore, siempre correcto
        layout.addView(TextView(this).apply {
            text = "${String.format("%.2f", precioTotal)} €"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
            setPadding(0, 0, 0, 24)
        })

        // Botones solo en reservas futuras no canceladas
        if (esFutura && !estado.lowercase().contains("cancelada") && !estado.lowercase().contains("liberada")) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = zonaHorariaEspana
            val hoyStr = sdf.format(Calendar.getInstance(zonaHorariaEspana).time)
            val esHoy = (fecha == hoyStr)

            var mostrarCheckIn = false
            var mensajeCandado = "🔒 El botón de Check-in (QR) se activará el día de tu reserva."

            if (esHoy) {
                try {
                    val partes = hora.split("a", "-")
                    val inicioParts = partes[0].trim().split(":")
                    if (inicioParts.size >= 2) {
                        val inicioMin = inicioParts[0].trim().toInt() * 60 + inicioParts[1].trim().toInt()
                        val ahoraMin  = Calendar.getInstance(zonaHorariaEspana).let {
                            it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
                        }
                        if (ahoraMin >= inicioMin - 10) {
                            mostrarCheckIn = true
                        } else {
                            mensajeCandado = "🔒 El check-in se habilitará 10 min antes de tu reserva."
                        }
                    }
                } catch (e: Exception) { mostrarCheckIn = true }
            }

            if (mostrarCheckIn) {
                layout.addView(com.google.android.material.button.MaterialButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    text = "📱 Validar llegada (Escanear QR)"
                    isAllCaps = false
                    cornerRadius = 20
                    setBackgroundColor(Color.parseColor("#1A73E8"))
                    setTextColor(Color.WHITE)
                    setOnClickListener {
                        startActivity(Intent(this@MisReservasActivity, PaseQrActivity::class.java).apply {
                            putExtra("RESERVA_ID", idReserva)
                            putExtra("SALA_NOMBRE", sala)
                            putExtra("FECHA", fecha)
                            putExtra("HORARIO", hora)
                        })
                    }
                })
            } else {
                layout.addView(TextView(this).apply {
                    text = mensajeCandado
                    setTextColor(Color.GRAY)
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.ITALIC)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(0, 0, 0, 24)
                    }
                })
            }

            // Botones Editar / Borrar
            val layoutBotones = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                weightSum = 2f
            }

            layoutBotones.addView(com.google.android.material.button.MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 16, 0)
                }
                text = "Editar"
                isAllCaps = false
                cornerRadius = 20
                setBackgroundColor(Color.parseColor("#FF9800"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    startActivity(Intent(this@MisReservasActivity, EditarReservaActivity::class.java).apply {
                        putExtra("RESERVA_ID", idReserva)
                        putExtra("SALA_NOMBRE", sala)
                    })
                }
            })

            layoutBotones.addView(com.google.android.material.button.MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(16, 0, 0, 0)
                }
                text = "Borrar"
                isAllCaps = false
                cornerRadius = 20
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    db.collection("reservas").document(idReserva)
                        .update("estado", "Cancelada por usuario")
                        .addOnSuccessListener {
                            Toast.makeText(this@MisReservasActivity, "Reserva cancelada", Toast.LENGTH_SHORT).show()
                            cargarMisReservas()
                        }
                }
            })

            layout.addView(layoutBotones)
        }

        cardView.addView(layout)
        contenedorTarget.addView(cardView)
    }

    private fun verificarRolUsuario(bottomNavigation: BottomNavigationView) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val rol = doc.getString("role") ?: doc.getString("rol") ?: ""
                    if (rol.lowercase() == "admin" || rol.lowercase() == "administrador") {
                        bottomNavigation.menu.findItem(R.id.nav_admin)?.isVisible = true
                    }
                }
        }
    }
}