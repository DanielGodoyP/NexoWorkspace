package com.example.nexocitas_tfg

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class EditarReservaActivity : AppCompatActivity() {

    private var fechaSeleccionada = ""
    private var horaInicio = ""
    private var horaFin = ""

    private var precioPorHoraSala = 0.0
    private var capacidadMaxima = 1
    private var numPersonas = 1

    private var nombresExtras = Array(7) { "" }
    private var preciosExtras = DoubleArray(7) { 0.0 }
    private var tiposExtras = IntArray(7) { 0 }

    private var nombreSala = "Sala"
    private var idReserva = ""

    private lateinit var db: FirebaseFirestore

    private lateinit var tvNombreSala: TextView
    private lateinit var tvInfoSala: TextView
    private lateinit var tvAforo: TextView
    private lateinit var tvNumPersonas: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHoras: TextView
    private lateinit var tvTotalReserva: TextView
    private lateinit var cbs: Array<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_reserva)

        db = FirebaseFirestore.getInstance()

        idReserva = intent.getStringExtra("RESERVA_ID") ?: ""
        nombreSala = intent.getStringExtra("SALA_NOMBRE") ?: "Sala"

        if (idReserva.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró la reserva", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.ivVolverEditar).setOnClickListener { finish() }

        tvNombreSala = findViewById(R.id.tvNombreSalaEditar)
        tvInfoSala = findViewById(R.id.tvInfoSalaEditar)
        tvAforo = findViewById(R.id.tvAforoMaximoEditar)
        tvNumPersonas = findViewById(R.id.tvNumPersonasEditar)
        tvFecha = findViewById(R.id.tvFechaSeleccionadaEditar)
        tvHoras = findViewById(R.id.tvHorasSeleccionadasEditar)
        tvTotalReserva = findViewById(R.id.tvTotalReservaEditar)

        val btnMenos = findViewById<TextView>(R.id.btnMenosPersonasEditar)
        val btnMas = findViewById<TextView>(R.id.btnMasPersonasEditar)
        val calendarView = findViewById<CalendarView>(R.id.calendarViewEditar)
        calendarView.minDate = System.currentTimeMillis()
        val btnInicio = findViewById<Button>(R.id.btnHoraInicioEditar)
        val btnFin = findViewById<Button>(R.id.btnHoraFinEditar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCambios)

        cbs = arrayOf(
            findViewById(R.id.cbExtra1Editar), findViewById(R.id.cbExtra2Editar),
            findViewById(R.id.cbExtra3Editar), findViewById(R.id.cbExtra4Editar),
            findViewById(R.id.cbExtra5Editar), findViewById(R.id.cbExtra6Editar),
            findViewById(R.id.cbExtra7Editar)
        )

        tvNombreSala.setOnClickListener {
            val salasDisponibles = arrayOf(
                "Escritorio Flex (Hot Desk)",
                "Escritorio Fijo Premium",
                "Cabina Privada (Phone Booth)",
                "Estudio de Grabación & Podcast",
                "Aula de Formación Nexo",
                "Sala de Reuniones Ejecutiva"
            )

            AlertDialog.Builder(this)
                .setTitle("Selecciona una nueva sala")
                .setItems(salasDisponibles) { _, which ->
                    val salaElegida = salasDisponibles[which]
                    if (salaElegida != nombreSala) {
                        nombreSala = salaElegida
                        for (cb in cbs) cb.isChecked = false
                        configurarDatosSala()
                        calcularTotal()
                        Toast.makeText(this, "Sala cambiada a: $nombreSala", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        db.collection("reservas").document(idReserva).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombreSala = doc.getString("sala") ?: nombreSala
                    fechaSeleccionada = doc.getString("fecha") ?: ""
                    horaInicio = doc.getString("horaInicio") ?: ""
                    horaFin = doc.getString("horaFin") ?: ""
                    numPersonas = (doc.getLong("numeroPersonas") ?: 1L).toInt()

                    val accesoriosPrevios = doc.getString("accesorios") ?: ""

                    tvFecha.text = if (fechaSeleccionada.isNotEmpty()) "Fecha: $fechaSeleccionada" else "Fecha: Sin seleccionar"
                    tvHoras.text = if (horaInicio.isNotEmpty() && horaFin.isNotEmpty()) "Horario: $horaInicio a $horaFin" else "Horario: Sin seleccionar"

                    if (fechaSeleccionada.isNotEmpty()) {
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val date = sdf.parse(fechaSeleccionada)
                            if (date != null) calendarView.date = date.time
                        } catch (e: Exception) { e.printStackTrace() }
                    }

                    configurarDatosSala()

                    for (i in 0..6) {
                        if (nombresExtras[i].isNotEmpty() && accesoriosPrevios.contains(nombresExtras[i])) {
                            cbs[i].isChecked = true
                        }
                    }
                    calcularTotal()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar la reserva", Toast.LENGTH_SHORT).show()
            }

        for (cb in cbs) cb.setOnCheckedChangeListener { _, _ -> calcularTotal() }

        btnMenos.setOnClickListener {
            if (numPersonas > 1) {
                numPersonas--
                tvNumPersonas.text = numPersonas.toString()
                calcularTotal()
            }
        }

        btnMas.setOnClickListener {
            if (numPersonas < capacidadMaxima) {
                numPersonas++
                tvNumPersonas.text = numPersonas.toString()
                calcularTotal()
            } else {
                Toast.makeText(this, "Límite: $capacidadMaxima personas en esta sala", Toast.LENGTH_SHORT).show()
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            fechaSeleccionada = "${String.format("%02d", dayOfMonth)}/${String.format("%02d", month + 1)}/$year"
            tvFecha.text = "Fecha: $fechaSeleccionada"
        }

        btnInicio.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                horaInicio = String.format("%02d:%02d", h, m)
                tvHoras.text = "Horario: $horaInicio a $horaFin"
                calcularTotal()
            }, 9, 0, true).show()
        }

        btnFin.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                horaFin = String.format("%02d:%02d", h, m)
                tvHoras.text = "Horario: $horaInicio a $horaFin"
                calcularTotal()
            }, 10, 0, true).show()
        }

        btnGuardar.setOnClickListener {
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

            btnGuardar.isEnabled = false

            db.collection("reservas")
                .whereEqualTo("sala", nombreSala)
                .whereEqualTo("fecha", fechaSeleccionada)
                .get()
                .addOnSuccessListener { documentos ->
                    var haySolapamiento = false

                    for (doc in documentos) {
                        if (doc.id == idReserva) continue

                        val estado = (doc.getString("estado") ?: "").lowercase()
                        if (estado.contains("cancelada") || estado.contains("liberada")) continue

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
                        Toast.makeText(this, "❌ HORARIO OCUPADO. Existe un conflicto con otra reserva.", Toast.LENGTH_LONG).show()
                        btnGuardar.isEnabled = true
                    } else {
                        var horas = 1
                        val diff = minFinNuevo - minInicioNuevo
                        if (diff > 0) horas = Math.ceil(diff / 60.0).toInt()

                        var costeExtras = 0.0
                        val accesoriosLista = mutableListOf<String>()

                        for (i in 0..6) {
                            if (cbs[i].isChecked) {
                                accesoriosLista.add(nombresExtras[i])
                                costeExtras += when (tiposExtras[i]) {
                                    1 -> preciosExtras[i] * horas
                                    2 -> preciosExtras[i] * numPersonas
                                    else -> preciosExtras[i]
                                }
                            }
                        }

                        val precioBase = horas * precioPorHoraSala * numPersonas
                        val precioTotal = precioBase + costeExtras
                        val extrasFinal = if (accesoriosLista.isEmpty()) "Sin extras" else accesoriosLista.joinToString(", ")

                        val actualizacion = mapOf(
                            "sala"           to nombreSala,
                            "fecha"          to fechaSeleccionada,
                            "horaInicio"     to horaInicio,
                            "horaFin"        to horaFin,
                            "hora"           to "$horaInicio - $horaFin",
                            "accesorios"     to extrasFinal,
                            "numeroPersonas" to numPersonas,
                            "precioTotal"    to precioTotal,
                            "total"          to precioTotal
                        )

                        db.collection("reservas").document(idReserva).update(actualizacion)
                            .addOnSuccessListener {
                                Toast.makeText(this, "✅ Reserva modificada con éxito", Toast.LENGTH_LONG).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                btnGuardar.isEnabled = true
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al comprobar horarios.", Toast.LENGTH_SHORT).show()
                    btnGuardar.isEnabled = true
                }
        }
    }

    private fun configurarDatosSala() {
        tvNombreSala.text = "Modificar: $nombreSala ✏️ (Tocar para cambiar)"

        for (i in 0..6) {
            nombresExtras[i] = ""
            preciosExtras[i] = 0.0
            tiposExtras[i] = 0
        }

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

        if (numPersonas > capacidadMaxima) {
            numPersonas = capacidadMaxima
        }
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
    }

    private fun calcularTotal() {
        var horas = 1
        try {
            if (horaInicio.isNotEmpty() && horaFin.isNotEmpty()) {
                val p1 = horaInicio.split(":")
                val p2 = horaFin.split(":")
                val m1 = p1[0].toInt() * 60 + p1[1].toInt()
                val m2 = p2[0].toInt() * 60 + p2[1].toInt()
                val diff = m2 - m1
                horas = if (diff > 0) Math.ceil(diff / 60.0).toInt() else 0
            }
        } catch (e: Exception) { horas = 1 }

        var costeExtras = 0.0
        for (i in 0..6) {
            if (cbs[i].isChecked) {
                costeExtras += when (tiposExtras[i]) {
                    1 -> preciosExtras[i] * horas
                    2 -> preciosExtras[i] * numPersonas
                    else -> preciosExtras[i]
                }
            }
        }

        val total = (horas * precioPorHoraSala * numPersonas) + costeExtras
        tvTotalReserva.text = String.format("%.2f €", total)
    }

    private fun configurarExtra(index: Int, nombre: String, precio: Double, tipo: Int) {
        nombresExtras[index] = nombre
        preciosExtras[index] = precio
        tiposExtras[index] = tipo
    }

    private fun convertirHoraAMinutos(hora: String): Int {
        return try {
            val partes = hora.split(":")
            partes[0].toInt() * 60 + partes[1].toInt()
        } catch (e: Exception) { 0 }
    }
}