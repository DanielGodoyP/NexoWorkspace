package com.example.nexocitas_tfg

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.Normalizer

class ChatbotActivity : AppCompatActivity() {
    private lateinit var contenedor: LinearLayout
    private lateinit var scroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        contenedor = findViewById(R.id.contenedorChat)
        scroll = findViewById(R.id.scrollChat)
        val etMensaje = findViewById<EditText>(R.id.etMensajeChat)
        val btnEnviar = findViewById<MaterialButton>(R.id.btnEnviarMensaje)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarChat)
        toolbar.setNavigationOnClickListener { finish() }

        enviarMensajeBot("Hola, soy NexoBot (Soporte). Estoy aquí para ayudarte con cualquier duda sobre NexoWorkspace. ¿En qué puedo ayudarte?")

        btnEnviar.setOnClickListener {
            val mensaje = etMensaje.text.toString().trim()
            if (mensaje.isNotEmpty()) {
                enviarMensajeUsuario(mensaje)
                etMensaje.text.clear()
                procesarMensajeLibre(mensaje)
            }
        }
    }

    private fun procesarMensajeLibre(mensaje: String) {
        val input = Normalizer.normalize(mensaje.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        contenedor.postDelayed({
            val respuesta = when {
                // ========== CÓMO HACER UNA RESERVA ==========
                input.contains("como edito") || input.contains("como puedo editar") || input.contains("editar reserva") || input.contains("cambiar reserva") || input.contains("modificar reserva") ->
                    "✏️ CÓMO EDITAR UNA RESERVA - PASO A PASO:\n\n" +
                            "1️⃣ ABRE LA APP EN LA PESTAÑA \"MIS RESERVAS\"\n" +
                            "2️⃣ SELECCIONA LA RESERVA QUE QUIERES EDITAR\n" +
                            "3️⃣ PULSA EL BOTÓN \"EDITAR\" (lado izquierdo del botón borrar)\n" +
                            "4️⃣ CAMBIA LO QUE NECESITES:\n" +
                            "   • Fecha\n" +
                            "   • Hora\n" +
                            "   • Sala\n" +
                            "   • Extras\n" +
                            "5️⃣ REVISA EL NUEVO PRECIO\n" +
                            "6️⃣ CONFIRMA LOS CAMBIOS\n\n" +
                            "💡 IMPORTANTE: Solo puedes editar reservas futuras, no las pasadas."

                input.contains("como borro") || input.contains("borrar reserva") || input.contains("cancelar reserva") || input.contains("como cancelo") ->
                    "🗑️ CÓMO BORRAR/CANCELAR UNA RESERVA:\n\n" +
                            "1️⃣ VE A LA PESTAÑA \"MIS RESERVAS\"\n" +
                            "2️⃣ SELECCIONA LA RESERVA A CANCELAR\n" +
                            "3️⃣ PULSA EL BOTÓN \"BORRAR\" (rojo, lado derecho)\n" +
                            "4️⃣ CONFIRMA LA CANCELACIÓN\n\n" +
                            "📝 IMPORTANTE:\n" +
                            "La cancelación es la ÚNICA forma de no usar tu reserva. Si no cancelas y no validas el QR a tiempo, será declarado como 'no-show' (no asistencia) y se cobrará el 100% de la reserva.\n\n" +
                            "⏱️ TIEMPO: Se procesa inmediatamente.\n\n" +
                            "💡 No admitimos devoluciones. Usa esta opción si necesitas cancelar."

                input.contains("como hago una reserva") || input.contains("como reservo") || input.contains("paso a paso") || input.contains("pasos para reservar") ->
                    "📋 CÓMO HACER UNA RESERVA - PASO A PASO:\n\n" +
                            "1️⃣ ABRE LA APP EN LA PESTAÑA PRINCIPAL\n" +
                            "Verás todas las salas disponibles con sus tarjetas visuales.\n\n" +
                            "2️⃣ SELECCIONA LA SALA QUE NECESITAS\n" +
                            "Pincha en la sala de tu interés.\n\n" +
                            "3️⃣ VE LA INFORMACIÓN DE LA SALA\n" +
                            "Verás detalles sobre qué contiene: equipamiento, capacidad, etc.\n\n" +
                            "4️⃣ ELIGE FECHA Y HORA EN EL CALENDARIO\n" +
                            "Tiene que ser de lunes a viernes, entre 08:00 y 22:00.\n\n" +
                            "5️⃣ AÑADE EXTRAS (OPCIONAL)\n" +
                            "Selecciona servicios adicionales si los necesitas.\n\n" +
                            "6️⃣ REVISA EL PRECIO TOTAL\n" +
                            "Verás el desglose: sala base + extras.\n\n" +
                            "7️⃣ CONFIRMA Y PAGA\n" +
                            "Rellena los datos de pago (simulado).\n\n" +
                            "8️⃣ ¡LISTO!\n" +
                            "Tu reserva está confirmada y la verás en 'Mis Reservas'."

                input.contains("qr") || input.contains("codigo qr") || input.contains("validar llegada") || input.contains("como accedo") ->
                    "📱 CÓDIGO QR - VALIDAR TU CITA:\n\n" +
                            "🎯 ¿CÓMO FUNCIONA?\n" +
                            "Cuando haces una reserva, el sistema genera un código QR único para acceder.\n\n" +
                            "⏰ CUÁNDO APARECE:\n" +
                            "10 minutos ANTES de tu reserva programada.\n\n" +
                            "📍 DÓNDE ESTÁ:\n" +
                            "En la pestaña 'Mis Reservas', tu reserva mostrará el QR.\n\n" +
                            "✅ CÓMO VALIDAR:\n" +
                            "1. Abre tu reserva en 'Mis Reservas'\n" +
                            "2. Verás el código QR visible\n" +
                            "3. Presénta lo en la entrada/recepción\n" +
                            "4. Se escanea y ¡ACCESO GARANTIZADO!\n\n" +
                            "⚠️ IMPORTANTE:\n" +
                            "Si no validas el QR a tiempo, tu reserva se cancelará automáticamente y será marcada como 'no-show' (no asistencia). En ese caso, se cobrará el 100% de la reserva.\n\n" +
                            "💡 El QR es personal e intransferible."

                input.contains("no asisti") || input.contains("no-show") || input.contains("no fui") || input.contains("no me presento") ->
                    "⚠️ POLÍTICA DE NO-SHOW (NO ASISTENCIA):\n\n" +
                            "Si hiciste una reserva pero no asistes:\n\n" +
                            "❌ QUÉ OCURRE:\n" +
                            "1. Tu QR debe validarse 10 minutos antes de la hora\n" +
                            "2. Si no validas el QR a tiempo, tu reserva se cancela\n" +
                            "3. Se marca como 'no-show' en el sistema\n" +
                            "4. SE COBRA EL 100% de la reserva\n\n" +
                            "💰 NO HABRÁ REEMBOLSO\n\n" +
                            "🔄 PARA EVITARLO:\n" +
                            "• Valida tu QR en el horario correcto\n" +
                            "• Si no puedes asistir, CANCELA la reserva desde 'Mis Reservas'\n" +
                            "• La cancelación es la única forma de no ser cobrado\n\n" +
                            "💡 Recuerda: El QR aparece 10 minutos antes. Prepárate a tiempo."

                // ========== INFORMACIÓN DE SALAS ==========
                input.contains("que salas hay") || input.contains("que espacios") || input.contains("cuales son las salas") || input.contains("informacion salas") ->
                    "🏢 SALAS DISPONIBLES EN NEXOWORKSPACE:\n\n" +
                            "Tenemos varios espacios completamente equipados:\n\n" +
                            "📚 AULAS\n" +
                            "Espacios amplios para formaciones y talleres.\n" +
                            "Capacidad para múltiples personas.\n" +
                            "Equipadas con proyectores, pizarras y sistemas audiovisuales.\n\n" +
                            "🎓 ESTUDIOS\n" +
                            "Espacios medianos para pequeños grupos de trabajo colaborativo.\n" +
                            "Mesas amplias y buena iluminación.\n\n" +
                            "🏢 SALAS DE REUNIONES\n" +
                            "Espacios profesionales para presentaciones y reuniones con clientes.\n" +
                            "Equipadas con pantallas interactivas.\n\n" +
                            "📝 ESCRITORIOS\n" +
                            "Espacios individuales con toda la conexión para trabajo concentrado.\n\n" +
                            "✨ TODAS INCLUYEN:\n" +
                            "WiFi alta velocidad, enchufes, climatización y mobiliario ergonómico."

                input.contains("que incluye") || input.contains("que tiene la sala") || input.contains("equipamiento") || input.contains("que trae") ->
                    "✨ EQUIPAMIENTO INCLUIDO EN TODAS LAS SALAS:\n\n" +
                            "✅ WiFi DE ALTA VELOCIDAD\n" +
                            "Conexión ultrarrápida para videoconferencias, streaming y trabajo en la nube.\n\n" +
                            "✅ ILUMINACIÓN PROFESIONAL\n" +
                            "LED de alta calidad que no cansa la vista.\n\n" +
                            "✅ CLIMATIZACIÓN AUTOMÁTICA\n" +
                            "Temperatura controlada todo el año.\n\n" +
                            "✅ MOBILIARIO ERGONÓMICO\n" +
                            "Mesas y sillas diseñadas para máximo confort.\n\n" +
                            "✅ ENCHUFES Y PUERTOS USB\n" +
                            "Múltiples conexiones para tus dispositivos.\n\n" +
                            "✅ EQUIPO MULTIMEDIA\n" +
                            "Proyectores, pantallas y sistemas de audio en salas principales.\n\n" +
                            "✅ PIZARRAS Y ROTULADORES\n" +
                            "Para brainstorming y presentaciones."

                input.contains("extras") || input.contains("servicios adicionales") || input.contains("que puedo añadir") ->
                    "⭐ SERVICIOS EXTRAS DISPONIBLES:\n\n" +
                            "Al hacer tu reserva, puedes añadir servicios adicionales que sumarán al precio total:\n\n" +
                            "📱 OPCIÓN 1: Equipamiento multimedia extra\n" +
                            "📱 OPCIÓN 2: Servicios de catering/café\n" +
                            "📱 OPCIÓN 3: Asistencia técnica IT\n" +
                            "📱 OPCIÓN 4: Otros servicios personalizados\n\n" +
                            "💡 CÓMO FUNCIONAN:\n" +
                            "• Aparecen en la pantalla de reserva\n" +
                            "• Cada uno tiene un coste diferente\n" +
                            "• Se suman al precio de la sala\n" +
                            "• Verás el total ANTES de pagar\n\n" +
                            "📞 PARA MÁS OPCIONES:\n" +
                            "nexoworkspace@gmail.com o 690 773 398"

                // ========== HORARIO Y UBICACIÓN ==========
                input.contains("horario") || input.contains("que horas") || input.contains("cuando abren") || input.contains("cuando estan abiertos") || input.contains("de que hora a que hora") ->
                    "🕐 HORARIO DE OPERACIÓN:\n\n" +
                            "⏰ HORAS:\n" +
                            "• De lunes a viernes\n" +
                            "• 08:00 a 22:00 (hora española)\n\n" +
                            "❌ NO OPERAMOS:\n" +
                            "• Sábados\n" +
                            "• Domingos\n" +
                            "• Festivos nacionales\n\n" +
                            "📝 PUEDES RESERVAR:\n" +
                            "Cualquier hora dentro del horario operativo (8:00-22:00) de lunes a viernes.\n\n" +
                            "📞 Excepciones especiales: 690 773 398"

                input.contains("donde estais") || input.contains("ubicacion") || input.contains("donde os puedo encontrar") || input.contains("leganes") || input.contains("direccion") ->
                    "📍 UBICACIÓN:\n\n" +
                            "NexoWorkspace está ubicado en Leganés, Madrid.\n\n" +
                            "🚗 ACCESO:\n" +
                            "• Fácil acceso por carretera\n" +
                            "• Aparcamiento disponible\n" +
                            "• Zona de alto potencial empresarial\n" +
                            "• Infraestructura completa\n\n" +
                            "📞 PARA INFORMACIÓN EXACTA:\n" +
                            "Llama a: 690 773 398\n" +
                            "o escribe a: nexoworkspace@gmail.com\n\n" +
                            "Nuestro equipo te facilitará horarios exactos de visita y detalles de acceso."

                // ========== PAGOS Y PRECIOS ==========
                input.contains("cuanto cuesta") || input.contains("precio") || input.contains("tarifa") || input.contains("coste") || input.contains("cuanto vale") ->
                    "💰 PRECIOS EN NEXOWORKSPACE:\n\n" +
                            "El coste depende de dos factores:\n\n" +
                            "1️⃣ SALA SELECCIONADA\n" +
                            "Cada tipo de sala tiene un precio por hora diferente.\n\n" +
                            "2️⃣ NÚMERO DE HORAS\n" +
                            "Reserva solo el tiempo que necesites.\n\n" +
                            "3️⃣ EXTRAS AÑADIDOS\n" +
                            "Servicios adicionales se suman al total.\n\n" +
                            "📊 DESGLOSE DE COSTES:\n" +
                            "• Sala × Horas = Coste Base\n" +
                            "• + Extras = Coste Total\n\n" +
                            "💡 VERÁS EL TOTAL ANTES DE PAGAR:\n" +
                            "Antes de confirmar, el sistema te muestra el desglose completo.\n\n" +
                            "No hay sorpresas. Todo transparente."

                input.contains("como pago") || input.contains("pagar") || input.contains("metodo de pago") || input.contains("tarjeta") ->
                    "💳 CÓMO PAGAR TU RESERVA:\n\n" +
                            "1️⃣ REVISA EL RESUMEN\n" +
                            "Verifica todos los detalles: sala, fecha, hora, extras y precio total.\n\n" +
                            "2️⃣ PULSA \"CONFIRMAR\"\n" +
                            "Vas al proceso de pago.\n\n" +
                            "3️⃣ RELLENA TUS DATOS DE TARJETA\n" +
                            "En la simulación de pago introducirás:\n" +
                            "• Número de tarjeta\n" +
                            "• Nombre del titular\n" +
                            "• Fecha de caducidad\n" +
                            "• CVV (tres dígitos atrás)\n\n" +
                            "4️⃣ COMPLETA LA TRANSACCIÓN\n" +
                            "Pulsa confirmar pago.\n\n" +
                            "5️⃣ RECIBE CONFIRMACIÓN\n" +
                            "¡Listo! Tu reserva se procesa.\n\n" +
                            "🔒 En producción, todo cifrado y 100% seguro."

                input.contains("reembolso") || input.contains("devolucion") || input.contains("dinero devuelto") || input.contains("me devuelven") || input.contains("admitimos devoluciones") ->
                    "❌ POLÍTICA DE REEMBOLSOS/DEVOLUCIONES:\n\n" +
                            "NO ADMITIMOS DEVOLUCIONES.\n\n" +
                            "En su lugar, tienes la opción de CANCELAR tu reserva:\n\n" +
                            "🗑️ PARA CANCELAR:\n" +
                            "1. Ve a 'Mis Reservas'\n" +
                            "2. Selecciona tu reserva\n" +
                            "3. Pulsa botón 'BORRAR'\n" +
                            "4. Confirma cancelación\n\n" +
                            "✅ VENTAJAS DE CANCELAR:\n" +
                            "• Se procesa inmediatamente\n" +
                            "• No serás cobrado si lo haces a tiempo\n" +
                            "• No aparecerá como 'no-show'\n\n" +
                            "⚠️ IMPORTANTE:\n" +
                            "Si NO cancelas y NO validas el QR, se cobrará el 100% (no-show).\n\n" +
                            "💡 Cancela si no puedes ir. Es la opción más rápida."

                input.contains("factura") || input.contains("recibo") || input.contains("como descargo") ->
                    "📄 FACTURAS:\n\n" +
                            "Cada reserva genera automáticamente una factura:\n\n" +
                            "✅ QUÉ INCLUYE:\n" +
                            "• Número de factura único\n" +
                            "• Fecha y hora de la reserva\n" +
                            "• Sala utilizada\n" +
                            "• Duración\n" +
                            "• Extras incluidos\n" +
                            "• Precio desglosado\n" +
                            "• IVA\n" +
                            "• Total pagado\n\n" +
                            "📥 CÓMO DESCARGARLA:\n" +
                            "1. Ve a 'Mis Reservas'\n" +
                            "2. Selecciona la reserva\n" +
                            "3. Descarga la factura en PDF\n\n" +
                            "📧 TAMBIÉN LA RECIBES:\n" +
                            "Por correo electrónico registrado."

                // ========== PERFIL Y CUENTA ==========
                input.contains("cerrar sesion") || input.contains("logout") || input.contains("desconectar") || input.contains("como cierro sesion") ->
                    "🚪 CÓMO CERRAR SESIÓN:\n\n" +
                            "1️⃣ ABRE LA PESTAÑA \"PERFIL\"\n" +
                            "En el menú inferior.\n\n" +
                            "2️⃣ BUSCA EL BOTÓN ROJO\n" +
                            "Dice 'CERRAR SESIÓN' o similar.\n\n" +
                            "3️⃣ PULSA EL BOTÓN\n" +
                            "Confirma si te lo pide.\n\n" +
                            "4️⃣ ¡LISTO!\n" +
                            "Tu sesión se ha cerrado.\n\n" +
                            "✅ EFECTOS:\n" +
                            "• Tu sesión termina\n" +
                            "• Se cierra tu cuenta\n" +
                            "• Tendrás que iniciar sesión nuevamente\n" +
                            "• Tus datos se mantienen seguros\n\n" +
                            "🔒 POR SEGURIDAD:\n" +
                            "Siempre cierra sesión en dispositivos compartidos."

                input.contains("mi perfil") || input.contains("datos personales") || input.contains("numero de telefono") || input.contains("email") || input.contains("nombre de usuario") ->
                    "👤 TUS DATOS PERSONALES EN PERFIL:\n\n" +
                            "📝 INFORMACIÓN QUE TENEMOS:\n" +
                            "✅ Nombre de usuario\n" +
                            "✅ Correo electrónico\n" +
                            "✅ Número de teléfono\n" +
                            "✅ Historial de reservas\n" +
                            "✅ Información de pago\n\n" +
                            "✏️ CÓMO ACTUALIZAR:\n" +
                            "1. Ve a 'Perfil'\n" +
                            "2. Busca opción de editar datos\n" +
                            "3. Cambia lo que necesites\n" +
                            "4. Guarda cambios\n\n" +
                            "🔐 SEGURIDAD:\n" +
                            "• Tus datos están encriptados\n" +
                            "• No los compartimos sin tu consentimiento\n" +
                            "• Cumplimos RGPD completo"

                input.contains("contraseña") || input.contains("password") || input.contains("olvidé contraseña") || input.contains("recuperar contraseña") ->
                    "🔐 CONTRASEÑA:\n\n" +
                            "❌ OLVIDÉ MI CONTRASEÑA:\n\n" +
                            "1️⃣ En la pantalla de LOGIN, pulsa 'Recuperar contraseña'\n" +
                            "2️⃣ Introduce tu correo electrónico\n" +
                            "3️⃣ Recibirás un email (revisa SPAM también)\n" +
                            "4️⃣ Sigue el enlace del email\n" +
                            "5️⃣ Crea una nueva contraseña\n" +
                            "6️⃣ ¡Listo! Ya puedes iniciar sesión\n\n" +
                            "✏️ CAMBIAR CONTRASEÑA (conozco la actual):\n" +
                            "1. Ve a 'Perfil'\n" +
                            "2. Busca 'Cambiar contraseña'\n" +
                            "3. Ingresa contraseña actual\n" +
                            "4. Nueva contraseña\n" +
                            "5. Confirma\n" +
                            "6. Guarda\n\n" +
                            "⏱️ TIEMPO RECEPCIÓN EMAIL:\n" +
                            "Normalmente en menos de 5 minutos."

                input.contains("registro") || input.contains("crear cuenta") || input.contains("nueva cuenta") || input.contains("como registrarse") ->
                    "📝 CÓMO REGISTRARSE:\n\n" +
                            "Si NO tienes cuenta en NexoWorkspace:\n\n" +
                            "1️⃣ EN LA PANTALLA DE LOGIN\n" +
                            "Busca la opción 'Crear cuenta' o 'Registrarse'\n\n" +
                            "2️⃣ RELLENA TUS DATOS:\n" +
                            "• Nombre completo\n" +
                            "• Correo electrónico (debe ser válido)\n" +
                            "• Número de teléfono\n" +
                            "• Contraseña (fuerte)\n\n" +
                            "3️⃣ CONFIRMA TU EMAIL\n" +
                            "Recibirás un email de confirmación.\n" +
                            "Pulsa el enlace para verificar.\n\n" +
                            "4️⃣ ¡CUENTA CREADA!\n" +
                            "Ya puedes iniciar sesión.\n\n" +
                            "💡 IMPORTANTE:\n" +
                            "Usa un email válido y contraseña fuerte."

                // ========== NOTIFICACIONES ==========
                input.contains("notificacion") || input.contains("avisos") || input.contains("campana") || input.contains("amarilla") || input.contains("alertas") ->
                    "🔔 SISTEMA DE NOTIFICACIONES:\n\n" +
                            "📍 DÓNDE ESTÁ:\n" +
                            "Arriba a la derecha de la pantalla principal.\n" +
                            "Busca el símbolo de una CAMPANA DE COLOR AMARILLO.\n\n" +
                            "📊 QUÉ VES:\n" +
                            "✅ Lista de todos tus avisos\n" +
                            "✅ Mensajes del admin\n" +
                            "✅ Confirmaciones de reservas\n" +
                            "✅ Cambios importantes\n\n" +
                            "📢 TIPOS DE NOTIFICACIONES:\n" +
                            "1️⃣ AVISOS GLOBALES: Info para TODOS\n" +
                            "2️⃣ MENSAJES PERSONALES: Solo para ti\n" +
                            "3️⃣ CONFIRMACIONES: De tu reserva\n\n" +
                            "🎯 ACCIONES EN CADA NOTIFICACIÓN:\n" +
                            "• Pulsa para leerla\n" +
                            "• Desliza para borrarla\n" +
                            "O selecciona la notificación y verás:\n" +
                            "  - Opción 'Marcar como leída'\n" +
                            "  - Opción 'Borrar'\n\n" +
                            "⚙️ GESTIÓN GLOBAL (3 puntos arriba a la derecha):\n" +
                            "• Marcar TODAS como leídas\n" +
                            "• Borrar TODAS las notificaciones\n\n" +
                            "💡 El admin puede contactarte directamente."

                input.contains("marcar como leida") || input.contains("leidas") || input.contains("borrar notificacion") ->
                    "🔔 GESTIONAR NOTIFICACIONES:\n\n" +
                            "📌 PARA UNA NOTIFICACIÓN INDIVIDUAL:\n" +
                            "1. Pulsa sobre la notificación\n" +
                            "2. Verás dos opciones:\n" +
                            "   ✅ Marcar como leída\n" +
                            "   🗑️ Borrar\n" +
                            "3. Elige la acción que prefieras\n\n" +
                            "⚙️ PARA TODAS LAS NOTIFICACIONES:\n" +
                            "1. Ve al menú de notificaciones (campana amarilla)\n" +
                            "2. Pulsa los 3 PUNTOS VERTICALES (arriba a la derecha)\n" +
                            "3. Verás dos opciones:\n" +
                            "   ✅ Marcar TODAS como leídas\n" +
                            "   🗑️ Borrar TODAS\n" +
                            "4. Elige la acción\n\n" +
                            "💡 Perfecto para limpiar tu centro de notificaciones."

                // ========== CONTACTO ==========
                input.contains("como os contacto") || input.contains("contacto") || input.contains("telefono") || input.contains("email") || input.contains("llamar") || input.contains("escribir") ->
                    "☎️ FORMAS DE CONTACTO CON NEXOWORKSPACE:\n\n" +
                            "📞 TELÉFONO:\n" +
                            "690 773 398\n" +
                            "(De lunes a viernes, 08:00-22:00)\n\n" +
                            "📧 CORREO ELECTRÓNICO:\n" +
                            "nexoworkspace@gmail.com\n" +
                            "(Respuesta en 24-48 horas)\n\n" +
                            "💬 CHATBOT (yo, NexoBot):\n" +
                            "Disponible 24/7 en esta pantalla.\n" +
                            "Para dudas frecuentes e inmediatas.\n\n" +
                            "📝 FORMULARIO DE CONTACTO:\n" +
                            "En Perfil > Menú (3 puntos) > Contacto\n" +
                            "Envía correos directamente desde la app.\n\n" +
                            "⏱️ HORARIO ATENCIÓN:\n" +
                            "Lunes a viernes (08:00-22:00)"

                input.contains("menu") && input.contains("puntos") || input.contains("tres puntos") || input.contains("sobre nosotros") || input.contains("informacion sobre nosotros") ->
                    "⚙️ MENÚ DE 3 PUNTOS EN PERFIL:\n\n" +
                            "📍 DÓNDE ESTÁ:\n" +
                            "En la pestaña 'Perfil', arriba a la derecha.\n" +
                            "Verás 3 puntos verticales (⋮).\n\n" +
                            "🎯 QUÉ ENCONTRAS:\n\n" +
                            "1️⃣ CONTACTO\n" +
                            "Formulario para enviar correos\n" +
                            "Email: nexoworkspace@gmail.com\n" +
                            "Teléfono: 690 773 398\n\n" +
                            "2️⃣ SOBRE NOSOTROS\n" +
                            "Información completa sobre NexoWorkspace:\n" +
                            "• ¿Quiénes somos?\n" +
                            "• Nuestra misión\n" +
                            "• Por qué elegirnos\n" +
                            "• Valores de NexoWorkspace\n" +
                            "• Ubicación y detalles\n\n" +
                            "💡 Ambas opciones tienen información importante."

                input.contains("donde puedo contactar") || input.contains("donde os puedo escribir") || input.contains("desde la app") ->
                    "📞 CONTACTA CON NUESTRO EQUIPO:\n\n" +
                            "☎️ TELÉFONO:\n" +
                            "690 773 398\n" +
                            "(Lunes-viernes, 08:00-22:00)\n\n" +
                            "📧 EMAIL:\n" +
                            "nexoworkspace@gmail.com\n" +
                            "(Respuesta en 24-48 horas)\n\n" +
                            "📱 O DESDE LA APP:\n" +
                            "Perfil > Menú (3 puntos) > Contacto\n\n" +
                            "💡 TAMBIÉN PUEDO AYUDARTE CON:\n" +
                            "• Cómo reservar\n" +
                            "• Información de salas\n" +
                            "• Preguntas sobre pagos\n" +
                            "• Problemas técnicos\n" +
                            "• Ubicación\n\n" +
                            "¿Hay algo más que quieras saber?"

                input.contains("como busco informacion") || input.contains("donde busco informacion") || input.contains("donde encuentro la informacion") || input.contains("información sobre nosotros") ->
                    "🔍 CÓMO ENCONTRAR INFORMACIÓN SOBRE NEXOWORKSPACE:\n\n" +
                            "📍 UBICACIÓN:\n" +
                            "En la pestaña 'Perfil' > Menú (3 puntos) > 'Sobre Nosotros'\n\n" +
                            "📋 QUÉ ENCONTRARÁS:\n" +
                            "✅ ¿Quiénes somos?\n" +
                            "   Descripción de NexoWorkspace\n\n" +
                            "✅ Nuestra misión\n" +
                            "   Qué nos impulsa\n\n" +
                            "✅ Por qué elegirnos\n" +
                            "   Nuestras ventajas\n\n" +
                            "✅ Valores de la empresa\n" +
                            "   Lo que nos define\n\n" +
                            "✅ Ubicación e infraestructura\n" +
                            "   Dónde estamos y qué ofrecemos\n\n" +
                            "🎯 PASOS:\n" +
                            "1. Abre el menú inferior\n" +
                            "2. Pulsa 'Perfil'\n" +
                            "3. Arriba a la derecha, 3 puntos (⋮)\n" +
                            "4. Selecciona 'Sobre Nosotros'\n" +
                            "5. ¡Lee toda nuestra información!"

                // ========== MIS RESERVAS ==========
                input.contains("mis reservas") || input.contains("donde veo") || input.contains("historial") ->
                    "📋 PESTAÑA 'MIS RESERVAS':\n\n" +
                            "📍 CÓMO ACCEDER:\n" +
                            "1. En el menú inferior, pulsa 'Mis Reservas'\n" +
                            "2. Verás todas tus reservas\n\n" +
                            "📊 QUÉ VES:\n" +
                            "✅ Lista de TODAS tus reservas\n" +
                            "✅ Estado de cada una\n" +
                            "✅ Sala reservada\n" +
                            "✅ Fecha y hora\n" +
                            "✅ Precio\n" +
                            "✅ Extras añadidos\n\n" +
                            "🎯 BOTONES DISPONIBLES:\n" +
                            "✏️ EDITAR: Cambia fecha, hora, sala, extras\n" +
                            "🗑️ BORRAR: Cancela la reserva\n\n" +
                            "📱 CÓDIGO QR:\n" +
                            "10 minutos ANTES de tu reserva, aparecerá el código QR para validar tu cita.\n\n" +
                            "💡 INFORMACIÓN DETALLADA:\n" +
                            "• Pulsa en cualquier reserva para ver todos los detalles\n" +
                            "• Descarga factura desde aquí"

                // ========== INFORMACIÓN GENERAL ==========
                input.contains("quienes sois") || input.contains("que es nexo") || input.contains("sobre nosotros") || input.contains("quién eres") ->
                    "🏢 SOBRE NEXOWORKSPACE:\n\n" +
                            "NexoWorkspace es el epicentro de la productividad en la zona sur de Madrid. Ubicados en el corazón de Leganés, hemos diseñado cada rincón para que emprendedores, startups y equipos creativos encuentren su lugar de trabajo ideal.\n\n" +
                            "🎯 NUESTRA MISIÓN:\n" +
                            "Conectar personas, facilitar recursos tecnológicos de vanguardia y ofrecer un ecosistema dinámico donde las ideas se conviertan en proyectos reales.\n\n" +
                            "⭐ POR QUÉ ELEGIRNOS:\n" +
                            "✅ Flexibilidad total en reservas\n" +
                            "✅ Aulas y estudios completamente equipados\n" +
                            "✅ Conexión de alta velocidad\n" +
                            "✅ Comunidad activa y colaborativa\n" +
                            "✅ Ubicación estratégica en Leganés\n\n" +
                            "💡 Para más info: Perfil > Menú (3 puntos) > Sobre Nosotros"

                input.contains("que puedo hacer aqui") || input.contains("para que sirve") || input.contains("cual es el proposito") ->
                    "🎯 ¿QUÉ PUEDES HACER EN NEXOWORKSPACE?\n\n" +
                            "✅ TRABAJAR ENFOCADO\n" +
                            "Escritorios y cabinas para concentración máxima.\n\n" +
                            "✅ REUNIONES CON CLIENTES\n" +
                            "Salas de reuniones profesionales y equipadas.\n\n" +
                            "✅ TRABAJO EN EQUIPO\n" +
                            "Estudios y aulas para colaboración grupal.\n\n" +
                            "✅ FORMACIONES Y TALLERES\n" +
                            "Aulas con capacidad para múltiples personas.\n\n" +
                            "✅ NETWORKING\n" +
                            "Comunidad activa de profesionales y emprendedores.\n\n" +
                            "✅ VIDEOCONFERENCIAS\n" +
                            "WiFi ultrarrápido y equipo de audio profesional.\n\n" +
                            "Todo reservable por horas, 100% flexible."

                input.contains("hola") || input.contains("buenos") || input.contains("hi") ->
                    "👋 ¡Hola! Soy NexoBot, el asistente de soporte de NexoWorkspace.\n\n" +
                            "¿En qué puedo ayudarte?\n\n" +
                            "💡 PREGUNTAS MÁS FRECUENTES:\n" +
                            "• Cómo hacer una reserva\n" +
                            "• Información de salas\n" +
                            "• Precios y pagos\n" +
                            "• Gestión de perfil\n" +
                            "• Contacto y ubicación\n" +
                            "• Problemas técnicos"

                input.contains("ayuda") || input.contains("menu") || input.contains("opciones") || input.contains("que puedes hacer") ->
                    "📋 ¿EN QUÉ PUEDO AYUDARTE?\n\n" +
                            "🔹 RESERVAS & SALAS:\n" +
                            "• Cómo hacer una reserva\n" +
                            "• Qué salas tenemos\n" +
                            "• Equipamiento disponible\n" +
                            "• Editar o cancelar reservas\n" +
                            "• Código QR y validación\n\n" +
                            "💳 PAGOS & PRECIOS:\n" +
                            "• Cómo pagar\n" +
                            "• Cuánto cuesta\n" +
                            "• Cancelación (sin devoluciones)\n" +
                            "• Facturas\n\n" +
                            "👤 CUENTA & PERFIL:\n" +
                            "• Iniciar/cerrar sesión\n" +
                            "• Cambiar contraseña\n" +
                            "• Actualizar datos\n" +
                            "• Crear cuenta\n\n" +
                            "🔔 NOTIFICACIONES:\n" +
                            "• Cómo funcionan\n" +
                            "• Gestionar notificaciones\n" +
                            "• Mensajes del admin\n\n" +
                            "☎️ CONTACTO & UBICACIÓN:\n" +
                            "• Teléfono: 690 773 398\n" +
                            "• Email: nexoworkspace@gmail.com\n" +
                            "• Ubicación: Leganés, Madrid\n\n" +
                            "ℹ️ INFORMACIÓN:\n" +
                            "• Sobre NexoWorkspace\n" +
                            "• Cómo encontrar información\n\n" +
                            "¿Qué te interesa?"

                else -> "🤔 Entiendo tu pregunta, pero no tengo una respuesta específica para eso.\n\n" +
                        "Pero puedo ayudarte con:\n" +
                        "✅ Cómo hacer una reserva\n" +
                        "✅ Información de salas\n" +
                        "✅ Preguntas sobre pagos\n" +
                        "✅ Gestión de tu perfil\n" +
                        "✅ Problemas técnicos\n" +
                        "✅ Contacto y ubicación\n\n" +
                        "📞 CONTACTA CON NUESTRO EQUIPO:\n\n" +
                        "☎️ TELÉFONO:\n" +
                        "690 773 398\n" +
                        "(Lunes-viernes, 08:00-22:00)\n\n" +
                        "📧 EMAIL:\n" +
                        "nexoworkspace@gmail.com\n" +
                        "(Respuesta en 24-48 horas)\n\n" +
                        "📱 O DESDE LA APP:\n" +
                        "Perfil > Menú (3 puntos) > Contacto\n\n" +
                        "¿Hay algo más que quieras saber?"
            }
            enviarMensajeBot(respuesta)
        }, 500)
    }

    private fun enviarMensajeBot(texto: String) {
        val tv = TextView(this).apply {
            text = texto
            setBackgroundResource(R.drawable.shape_redondo)
            setPadding(40, 30, 40, 30)
            setTextColor(Color.DKGRAY)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(0, 16, 120, 16) }
        }
        contenedor.addView(tv)
        bajarScroll()
    }

    private fun enviarMensajeUsuario(texto: String) {
        val tv = TextView(this).apply {
            text = texto
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            setPadding(40, 30, 40, 30)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.END
                setMargins(120, 16, 0, 16)
            }
        }
        contenedor.addView(tv)
        bajarScroll()
    }

    private fun bajarScroll() {
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
