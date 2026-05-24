package com.example.nexocitas_tfg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class HomeActivity : AppCompatActivity() {

    private lateinit var contenedorSalas: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        contenedorSalas = findViewById(R.id.contenedorSalas)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        verificarRolUsuario(bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_mis_reservas -> {
                    startActivity(Intent(this, MisReservasActivity::class.java))
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
                else -> false
            }
        }

        val fabChatbot = findViewById<FloatingActionButton>(R.id.fabChatbot)
        fabChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        val btnNotificaciones = findViewById<View>(R.id.btnNotificaciones)
        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCatalogoActualizado()
        actualizarContadorNotificaciones()
    }

    private fun actualizarContadorNotificaciones() {
        val userId = auth.currentUser?.uid ?: return
        val tvBadge = findViewById<TextView>(R.id.tvBadge) ?: return

        db.collection("notificaciones").get()
            .addOnSuccessListener { documentos ->
                var nuevas = 0
                for (doc in documentos) {
                    val leidaPor = doc.get("leidaPor") as? List<String> ?: emptyList()
                    if (!leidaPor.contains(userId)) nuevas++
                }

                runOnUiThread {
                    if (nuevas > 0) {
                        tvBadge.text = nuevas.toString()
                        tvBadge.visibility = View.VISIBLE
                    } else {
                        tvBadge.visibility = View.GONE
                    }
                }
            }
    }

    private fun cargarCatalogoActualizado() {
        db.collection("catalogo_estados").get()
            .addOnCompleteListener { task ->
                val salasInactivas = mutableListOf<String>()
                if (task.isSuccessful) {
                    for (doc in task.result) {
                        if (doc.getBoolean("activo") == false) salasInactivas.add(doc.id)
                    }
                }

                contenedorSalas.removeAllViews()

                // [0]Nombre, [1]Precio, [2]URL, [3]Capacidad, [4]Descripción Formal
                val listaSalas = listOf(
                    listOf("Sala de Reuniones Ejecutiva", "15", "https://images.unsplash.com/photo-1497366216548-37526070297c?w=500", "10 personas", "Espacio privado de alto nivel diseñado para reuniones, presentaciones y visitas corporativas."),
                    listOf("Aula de Formación Nexo", "28", "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=500", "20 personas", "Sala amplia y equipada tecnológicamente para impartir cursos, talleres y seminarios."),
                    listOf("Estudio de Grabación & Podcast", "22", "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=500", "3 personas", "Estudio profesional insonorizado para la producción y grabación de contenido audiovisual."),
                    listOf("Escritorio Flex (Hot Desk)", "3.50", "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=500", "1 persona", "Puesto de trabajo rotativo en zona compartida, ideal para profesionales independientes."),
                    listOf("Escritorio Fijo Premium", "6.50", "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=500", "1 persona", "Puesto de uso exclusivo y permanente con mobiliario ergonómico y espacio de almacenamiento."),
                    listOf("Cabina Privada (Phone Booth)", "12", "https://images.unsplash.com/photo-1535223289827-42f1e9919769?w=500", "1 persona", "Espacio individual insonorizado, óptimo para realizar videollamadas y trabajo de concentración.")
                )

                for (sala in listaSalas) {
                    if (!salasInactivas.contains(sala[0])) {
                        disenarTarjetaSala(
                            sala[0], // Nombre
                            sala[4], // Descripción formal
                            sala[1], // Precio
                            sala[3], // Capacidad
                            sala[2]  // URL Imagen
                        )
                    }
                }
            }
    }

    private fun verificarRolUsuario(bottomNavigation: BottomNavigationView) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && (doc.getString("rol")?.lowercase() == "admin" || doc.getString("rol")?.lowercase() == "administrador")) {
                    bottomNavigation.menu.findItem(R.id.nav_admin)?.isVisible = true
                }
            }
    }

    private fun disenarTarjetaSala(nombre: String, descripcion: String, precio: String, capacidad: String, urlImagen: String) {
        val cardView = CardView(this)
        val cardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cardParams.setMargins(0, 0, 0, 24)
        cardView.layoutParams = cardParams
        cardView.radius = 16f
        cardView.cardElevation = 4f
        cardView.setCardBackgroundColor(Color.WHITE)

        val layoutInterno = LinearLayout(this)
        layoutInterno.orientation = LinearLayout.VERTICAL

        val ivSala = ImageView(this)
        ivSala.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350)
        ivSala.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(urlImagen).placeholder(android.R.drawable.ic_menu_gallery).into(ivSala)
        layoutInterno.addView(ivSala)

        val layoutTextos = LinearLayout(this)
        layoutTextos.orientation = LinearLayout.VERTICAL
        layoutTextos.setPadding(24, 24, 24, 24)

        val tvTitulo = TextView(this)
        tvTitulo.text = nombre
        tvTitulo.textSize = 18f
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD)
        tvTitulo.setTextColor("#1A73E8".toColorInt())
        layoutTextos.addView(tvTitulo)

        val tvDetalles = TextView(this)
        tvDetalles.text = "Precio: $precio€ / persona / hora  •  Capacidad: $capacidad"
        tvDetalles.textSize = 14f
        tvDetalles.setPadding(0, 4, 0, 12)
        tvDetalles.setTextColor("#FF9800".toColorInt())
        tvDetalles.setTypeface(null, android.graphics.Typeface.BOLD)
        layoutTextos.addView(tvDetalles)

        val tvDesc = TextView(this)
        tvDesc.text = descripcion
        tvDesc.textSize = 14f
        tvDesc.setTextColor("#5F6368".toColorInt())
        layoutTextos.addView(tvDesc)

        layoutInterno.addView(layoutTextos)
        cardView.addView(layoutInterno)

        cardView.setOnClickListener {
            val intent = Intent(this, ReservaActivity::class.java)
            intent.putExtra("SALA_SELECCIONADA", nombre)
            startActivity(intent)
        }
        contenedorSalas.addView(cardView)
    }
}