package com.example.nexocitas_tfg

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReservaActivity : AppCompatActivity() {

    private var fechaSeleccionada = ""
    private var horaInicio = ""
    private var horaFin = ""

    private var precioPorHoraSala = 0.0
    private var capacidadMaxima = 1
    private var numPersonas = 1
    private var granTotalGlobal = 0.0

    private var nombresExtras = Array(7) { "" }
    private var preciosExtras = DoubleArray(7) { 0.0 }
    private var tiposExtras = IntArray(7) { 0 }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var reservaPendiente: HashMap<String, Any>? = null

    private val launcherPago = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarReserva)
        if (result.resultCode == Activity.RESULT_OK) {
            reservaPendiente?.let { datos ->
                db.collection("reservas").add(datos).addOnSuccessListener {
                    Toast.makeText(this, "¡Reserva y pago procesados con éxito!", Toast.LENGTH_LONG).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al registrar la reserva", Toast.LENGTH_SHORT).show()
                    btnConfirmar.isEnabled = true
                }
            }
        } else {
            Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
            btnConfirmar.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_reserva)
        verificarRolUsuario(bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_mis_reservas -> { startActivity(Intent(this, MisReservasActivity::class.java)); finish(); true }
                R.id.nav_perfil -> { startActivity(Intent(this, PerfilActivity::class.java)); finish(); true }
                R.id.nav_admin -> { startActivity(Intent(this, AdminReservasActivity::class.java)); finish(); true }
                else -> false
            }
        }

        findViewById<ImageView>(R.id.ivVolver).setOnClickListener { finish() }

        val tvNombreSala = findViewById<TextView>(R.id.tvNombreSalaReserva)
        val tvInfoSala = findViewById<TextView>(R.id.tvInfoSala)
        val tvAforo = findViewById<TextView>(R.id.tvAforoMaximo)
        val tvNumPersonas = findViewById<TextView>(R.id.tvNumPersonas)
        val btnMenos = findViewById<TextView>(R.id.btnMenosPersonas)
        val btnMas = findViewById<TextView>(R.id.btnMasPersonas)
        val tvTotalReserva = findViewById<TextView>(R.id.tvTotalReserva)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.minDate = System.currentTimeMillis()
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHoras = findViewById<TextView>(R.id.tvHorasSeleccionadas)

        val cbs = arrayOf(
            findViewById<CheckBox>(R.id.cbExtra1), findViewById<CheckBox>(R.id.cbExtra2),
            findViewById<CheckBox>(R.id.cbExtra3), findViewById<CheckBox>(R.id.cbExtra4),
            findViewById<CheckBox>(R.id.cbExtra5), findViewById<CheckBox>(R.id.cbExtra6),
            findViewById<CheckBox>(R.id.cbExtra7)
        )

        val nombreSala = intent.getStringExtra("SALA_SELECCIONADA") ?: intent.getStringExtra("NOMBRE_SALA") ?: "Sala"
        tvNombreSala.text = "Reservar: $nombreSala"

        var descripcionSala = ""

        when (nombreSala) {
            "Escritorio Flex (Hot Desk)" -> {
                precioPorHoraSala = 3.50; capacidadMaxima = 1
                descripcionSala = "Puesto de trabajo en zona compartida. Llegas, te sientas y te pones a trabajar con tu portátil sin complicaciones.<br><br>📏 <b>Dimensiones de la sala:</b> 12m de largo x 8m de ancho.<br><br><b>¿Qué incluye?</b><br>• WiFi de alta velocidad<br>• Acceso a la zona de descanso y cafetería<br>• Enchufes normales y USB en cada mesa<br>• Aire acondicionado y buena iluminación<br>• Taquillas gratuitas para guardar tus cosas"
                configurarExtra(0, "☕ Barra libre de café", 3.0, 1)
                configurarExtra(1, "🖥️ Pantalla extra para tu portátil", 4.0, 1)
                configurarExtra(2, "🖨️ Imprimir hasta 20 hojas", 2.0, 0)
                configurarExtra(3, "🔒 Taquilla con llave todo el día", 2.0, 0)
                configurarExtra(4, "🎧 Cascos que bloquean el ruido", 3.0, 1)
                configurarExtra(5, "🔐 Conexión a internet segura (VPN)", 10.0, 0)
            }
            "Escritorio Fijo Premium" -> {
                precioPorHoraSala = 6.50; capacidadMaxima = 1
                descripcionSala = "Tu propia mesa reservada. Un rincón tranquilo y cómodo donde puedes trabajar concentrado y dejar tus cosas de forma segura.<br><br>📏 <b>Dimensiones de la sala:</b> 15m de largo x 10m de ancho.<br><br><b>¿Qué incluye?</b><br>• WiFi privado y conexión a internet por cable<br>• Mesa grande con cajones para tus cosas<br>• Silla de oficina muy cómoda<br>• Prioridad si quieres reservar otras salas<br>• Recogemos tus paquetes en recepción"
                configurarExtra(0, "📷 Cámara web de buena calidad + aro de luz", 5.0, 1)
                configurarExtra(1, "🖥️ Pantalla gigante para ver todo mejor", 12.0, 1)
                configurarExtra(2, "💻 Informático de ayuda por si algo falla", 15.0, 1)
                configurarExtra(3, "🤵 Te llevamos el café a la mesa", 15.0, 0)
                configurarExtra(4, "📬 Usar nuestra dirección para tus cartas", 20.0, 0)
                configurarExtra(5, "🪑 Silla de gama alta para la espalda", 4.0, 0)
            }
            "Cabina Privada (Phone Booth)" -> {
                precioPorHoraSala = 12.0; capacidadMaxima = 1
                descripcionSala = "Pequeña cabina individual cerrada donde no entra ni sale el ruido. Perfecta para hacer videollamadas o llamadas telefónicas importantes sin que nadie te moleste.<br><br>📏 <b>Dimensiones de la cabina:</b> 1.2m de largo x 1.2m de ancho.<br><br><b>¿Qué incluye?</b><br>• Paredes que bloquean el ruido exterior<br>• WiFi privado muy rápido<br>• Pantalla lista para enchufar tu portátil<br>• Luz regulable y buena ventilación<br>• Enchufes para cargar el móvil o portátil"
                configurarExtra(0, "📹 Equipo para hacer videollamadas", 6.0, 1)
                configurarExtra(1, "☕ Café y botella de agua", 3.0, 1)
                configurarExtra(2, "🎙️ Micrófono de mesa para que te escuchen bien", 4.0, 1)
                configurarExtra(3, "📱 Teléfono fijo disponible", 5.0, 1)
                configurarExtra(4, "🔐 Conexión a internet segura (VPN)", 10.0, 0)
                configurarExtra(5, "📲 Tablet de apoyo", 4.0, 1)
            }
            "Estudio de Grabación & Podcast" -> {
                precioPorHoraSala = 22.0; capacidadMaxima = 3
                descripcionSala = "Sala preparada para grabar vídeos, entrevistas o podcasts. Todo está listo para que el sonido y la imagen sean perfectos, aunque no seas un experto.<br><br>📏 <b>Dimensiones del estudio:</b> 4m de largo x 3m de ancho.<br><br><b>¿Qué incluye?</b><br>• Paredes preparadas para que no haya eco<br>• Micrófonos profesionales y fáciles de usar<br>• Cascos para todos los participantes<br>• Mesa redonda para 3 personas<br>• Internet por cable para que no se corte"
                configurarExtra(0, "🎬 Grabación en vídeo con 2 cámaras", 12.0, 1)
                configurarExtra(1, "🎚️ Una persona que te ayuda con el sonido", 18.0, 1)
                configurarExtra(2, "📡 Emitir en directo (Twitch/YouTube)", 10.0, 1)
                configurarExtra(3, "✂️ Nosotros te editamos el vídeo al terminar", 35.0, 0)
                configurarExtra(4, "🎨 Decoración de fondo a tu gusto", 8.0, 0)
                configurarExtra(5, "☁️ Te pasamos los vídeos por internet", 4.0, 0)
            }
            "Aula de Formación Nexo" -> {
                precioPorHoraSala = 28.0; capacidadMaxima = 20
                descripcionSala = "Sala grande y espaciosa ideal para dar clases, charlas o talleres a grupos. Puedes mover las mesas y las sillas como mejor te venga.<br><br>📏 <b>Dimensiones del aula:</b> 10m de largo x 7m de ancho.<br><br><b>¿Qué incluye?</b><br>• Espacio suficiente para 20 personas<br>• Proyector y pantalla grande de pared<br>• Altavoces y micrófono sin cables<br>• Pizarra blanca grande con rotuladores<br>• Recibimos a tus invitados en la entrada"
                configurarExtra(0, "🍽️ Comida y bebida para todos (Catering)", 12.0, 2)
                configurarExtra(1, "📡 Emitir la clase por internet en directo", 18.0, 0)
                configurarExtra(2, "🎥 Grabar toda la clase en vídeo", 20.0, 0)
                configurarExtra(3, "🖥️ Pantalla extra grande para presentaciones", 12.0, 1)
                configurarExtra(4, "🧑‍💼 Una persona para ayudar a organizar", 12.0, 1)
                configurarExtra(5, "💻 Informático de ayuda por si falla el proyector", 15.0, 1)
                configurarExtra(6, "🖨️ Imprimir apuntes para los alumnos", 8.0, 0)
            }
            "Sala de Reuniones Ejecutiva",
            "Sala de Reuniones Executive" -> {
                precioPorHoraSala = 15.0; capacidadMaxima = 10
                descripcionSala = "Sala privada, elegante y sin ruidos. El lugar perfecto para tener reuniones importantes con clientes o juntarte con tu equipo de trabajo.<br><br>📏 <b>Dimensiones de la sala:</b> 5m de largo x 4m de ancho.<br><br><b>¿Qué incluye?</b><br>• Paredes que bloquean el ruido<br>• Mesa grande con enchufes escondidos<br>• Sillas de oficina muy cómodas<br>• Televisión grande para poner presentaciones"
                configurarExtra(0, "🖥️ Televisión para hacer videollamadas", 15.0, 1)
                configurarExtra(1, "🔒 Informático de ayuda e internet privado", 25.0, 0)
                configurarExtra(2, "☕ Café y pastas especiales para la reunión", 20.0, 2)
            }
            else -> {
                precioPorHoraSala = 10.0; capacidadMaxima = 5
                descripcionSala = "Espacio de trabajo cómodo y bien iluminado.<br><br>📏 <b>Dimensiones:</b> 4m de largo x 3m de ancho."
            }
        }

        tvInfoSala.text = HtmlCompat.fromHtml(descripcionSala, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvAforo.text = "Aforo límite: $capacidadMaxima personas"
        tvNumPersonas.text = numPersonas.toString()

        for (i in 0..6) {
            if (nombresExtras[i].isEmpty()) {
                cbs[i].visibility = View.GONE
            } else {
                cbs[i].visibility = View.VISIBLE
                val sufijo = when (tiposExtras[i]) {
                    1 -> "€/h"
                    2 -> "€/persona"
                    else -> "€"
                }
                cbs[i].text = "${nombresExtras[i]} (+${preciosExtras[i]}$sufijo)"
            }
        }

        val calcularTotalAlVuelo = {
            var horasDiferencia = 1
            if (horaInicio.isNotEmpty() && horaFin.isNotEmpty()) {
                try {
                    val p1 = horaInicio.split(":")
                    val p2 = horaFin.split(":")
                    val m1 = p1[0].toInt() * 60 + p1[1].toInt()
                    val m2 = p2[0].toInt() * 60 + p2[1].toInt()
                    val diff = m2 - m1
                    horasDiferencia = if (diff > 0) Math.ceil(diff / 60.0).toInt() else 0
                } catch (e: Exception) { horasDiferencia = 1 }
            }

            var costeExtras = 0.0
            for (i in 0..6) {
                if (cbs[i].isChecked) {
                    when (tiposExtras[i]) {
                        0 -> costeExtras += preciosExtras[i]
                        1 -> costeExtras += (preciosExtras[i] * horasDiferencia)
                        2 -> costeExtras += (preciosExtras[i] * numPersonas)
                    }
                }
            }

            val precioBase = horasDiferencia * precioPorHoraSala * numPersonas
            granTotalGlobal = precioBase + costeExtras
            tvTotalReserva.text = String.format("%.2f €", granTotalGlobal)
        }

        calcularTotalAlVuelo()

        btnMenos.setOnClickListener {
            if (numPersonas > 1) { numPersonas--; tvNumPersonas.text = numPersonas.toString(); calcularTotalAlVuelo() }
        }

        btnMas.setOnClickListener {
            if (numPersonas < capacidadMaxima) { numPersonas++; tvNumPersonas.text = numPersonas.toString(); calcularTotalAlVuelo() }
            else { Toast.makeText(this, "Límite: $capacidadMaxima personas", Toast.LENGTH_SHORT).show() }
        }

        for (cb in cbs) cb.setOnCheckedChangeListener { _, _ -> calcularTotalAlVuelo() }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            fechaSeleccionada = "${String.format("%02d", dayOfMonth)}/${String.format("%02d", month + 1)}/$year"
            tvFecha.text = "Fecha: $fechaSeleccionada"
        }

        findViewById<Button>(R.id.btnHoraInicio).setOnClickListener {
            TimePickerDialog(this, { _, h, m -> horaInicio = String.format("%02d:%02d", h, m); tvHoras.text = "Horario: $horaInicio a $horaFin"; calcularTotalAlVuelo() }, 9, 0, true).show()
        }

        findViewById<Button>(R.id.btnHoraFin).setOnClickListener {
            TimePickerDialog(this, { _, h, m -> horaFin = String.format("%02d:%02d", h, m); tvHoras.text = "Horario: $horaInicio a $horaFin"; calcularTotalAlVuelo() }, 10, 0, true).show()
        }

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarReserva)
        btnConfirmar.setOnClickListener {
            if (fechaSeleccionada.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
                Toast.makeText(this, "Completa fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minInicioNuevo = convertirHoraAMinutos(horaInicio)
            val minFinNuevo = convertirHoraAMinutos(horaFin)

            if (minInicioNuevo >= minFinNuevo) {
                Toast.makeText(this, "La hora de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- VALIDACIÓN: HORARIO DE APERTURA (08:00 a 22:00) ---
            if (minInicioNuevo < 480 || minFinNuevo > 1320) {
                Toast.makeText(this, "⏳ El horario del coworking es de 08:00 a 22:00. Ajusta tus horas.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // --- VALIDACIÓN: NO RESERVAR EN EL PASADO (Si es el mismo día) ---
            val zonaEspana = TimeZone.getTimeZone("Europe/Madrid")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = zonaEspana
            val hoyStr = sdf.format(Calendar.getInstance(zonaEspana).time)

            if (fechaSeleccionada == hoyStr) {
                val calAhora = Calendar.getInstance(zonaEspana)
                val minAhora = calAhora.get(Calendar.HOUR_OF_DAY) * 60 + calAhora.get(Calendar.MINUTE)
                if (minInicioNuevo <= minAhora) {
                    Toast.makeText(this, "⏳ No puedes reservar en una hora que ya ha pasado", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Deshabilitar botón temporalmente
            btnConfirmar.isEnabled = false

            // MOTOR DE PREVENCIÓN DE SOLAPAMIENTOS
            db.collection("reservas")
                .whereEqualTo("sala", nombreSala)
                .whereEqualTo("fecha", fechaSeleccionada)
                .get()
                .addOnSuccessListener { documentos ->
                    var haySolapamiento = false

                    for (doc in documentos) {
                        val estado = (doc.getString("estado") ?: "").lowercase()
                        if (estado.contains("cancelada") || estado.contains("liberada")) {
                            continue
                        }

                        val hInicioExistente = doc.getString("horaInicio") ?: ""
                        val hFinExistente = doc.getString("horaFin") ?: ""

                        if (hInicioExistente.isNotEmpty() && hFinExistente.isNotEmpty()) {
                            val minInicioExistente = convertirHoraAMinutos(hInicioExistente)
                            val minFinExistente = convertirHoraAMinutos(hFinExistente)

                            if (minInicioNuevo < minFinExistente && minFinNuevo > minInicioExistente) {
                                haySolapamiento = true
                                break
                            }
                        }
                    }

                    if (haySolapamiento) {
                        Toast.makeText(this, "❌ HORARIO NO DISPONIBLE. Ya existe una reserva en estas horas.", Toast.LENGTH_LONG).show()
                        btnConfirmar.isEnabled = true
                    } else {
                        // PROCEDEMOS AL PAGO
                        val extrasFinales = mutableListOf<String>()
                        for (i in 0..6) { if (cbs[i].isChecked) extrasFinales.add(nombresExtras[i]) }
                        val textoExtras = if (extrasFinales.isEmpty()) "Sin extras" else extrasFinales.joinToString(", ")

                        var costeExtras = 0
                        var horasDiferencia = 1
                        val diff = minFinNuevo - minInicioNuevo
                        if (diff > 0) horasDiferencia = Math.ceil(diff / 60.0).toInt() else 0

                        for (i in 0..6) {
                            if (cbs[i].isChecked) {
                                when (tiposExtras[i]) {
                                    0 -> costeExtras += preciosExtras[i].toInt()
                                    1 -> costeExtras += (preciosExtras[i] * horasDiferencia).toInt()
                                    2 -> costeExtras += (preciosExtras[i] * numPersonas).toInt()
                                }
                            }
                        }

                        reservaPendiente = hashMapOf(
                            "sala" to nombreSala,
                            "fecha" to fechaSeleccionada,
                            "hora" to "$horaInicio - $horaFin",
                            "horaInicio" to horaInicio,
                            "horaFin" to horaFin,
                            "estado" to "Confirmada",
                            "accesorios" to textoExtras,
                            "numeroPersonas" to numPersonas,
                            "total" to granTotalGlobal,
                            "costeExtras" to costeExtras,
                            "usuarioEmail" to (auth.currentUser?.email ?: ""),
                            "usuarioId" to (auth.currentUser?.uid ?: "")
                        )

                        val intentPago = Intent(this, PagoActivity::class.java)
                        intentPago.putExtra("PRECIO_TOTAL", granTotalGlobal.toString())
                        launcherPago.launch(intentPago)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al comprobar disponibilidad.", Toast.LENGTH_SHORT).show()
                    btnConfirmar.isEnabled = true
                }
        }
    }

    private fun convertirHoraAMinutos(hora: String): Int {
        return try {
            val partes = hora.split(":")
            partes[0].toInt() * 60 + partes[1].toInt()
        } catch (e: Exception) { 0 }
    }

    private fun configurarExtra(index: Int, nombre: String, precio: Double, tipo: Int) {
        nombresExtras[index] = nombre
        preciosExtras[index] = precio
        tiposExtras[index] = tipo
    }

    private fun verificarRolUsuario(bottomNavigation: BottomNavigationView) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).get().addOnSuccessListener {
                val rol = it.getString("rol")?.lowercase() ?: it.getString("role")?.lowercase()
                if (rol == "admin" || rol == "administrador") {
                    bottomNavigation.menu.findItem(R.id.nav_admin)?.isVisible = true
                }
            }
        }
    }
}