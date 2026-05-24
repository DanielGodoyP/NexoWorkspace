package com.example.nexocitas_tfg

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var contenedorNuevas: LinearLayout
    private lateinit var contenedorVistas: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarNotificaciones)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        contenedorNuevas = findViewById(R.id.layoutNuevas)
        contenedorVistas = findViewById(R.id.layoutVistas)

        cargarNotificaciones()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notificaciones, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        when (item.itemId) {
            R.id.action_marcar_todas_leidas -> {
                db.collection("notificaciones").get().addOnSuccessListener { docs ->
                    for (doc in docs) {
                        val leidaPor = doc.get("leidaPor") as? List<String> ?: emptyList()
                        if (!leidaPor.contains(userId)) {
                            doc.reference.update("leidaPor", FieldValue.arrayUnion(userId))
                        }
                    }
                    cargarNotificaciones()
                }
                return true
            }
            R.id.action_borrar_todas -> {
                // Borrado Lógico Masivo: Añadimos el UID a la lista borradaPor
                db.collection("notificaciones").get().addOnSuccessListener { docs ->
                    for (doc in docs) {
                        doc.reference.update("borradaPor", FieldValue.arrayUnion(userId))
                    }
                    cargarNotificaciones()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarNotificaciones() {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: return

        db.collection("notificaciones").orderBy("fecha", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documentos ->
                contenedorNuevas.removeAllViews()
                contenedorVistas.removeAllViews()

                for (doc in documentos) {
                    // Comprobamos si el usuario actual ha "borrado" esta notificación
                    val borradaPor = doc.get("borradaPor") as? List<String> ?: emptyList()
                    if (borradaPor.contains(userId)) {
                        continue // Si está borrada por este usuario, no la mostramos en su pantalla
                    }

                    // FILTRO DE DESTINATARIO
                    val destinatario = doc.getString("destinatario") ?: "todos"

                    if (destinatario == "todos" || destinatario.lowercase() == userEmail.lowercase()) {
                        val yaLeida = (doc.get("leidaPor") as? List<String>)?.contains(userId) == true

                        var tituloVisual = doc.getString("titulo") ?: ""

                        // TEXTO PROFESIONAL PARA MENSAJES DIRECTOS
                        if (destinatario != "todos") {
                            tituloVisual = "🏢 [Mensaje de NexoWorkspace] $tituloVisual"
                        }

                        val mensaje = doc.getString("mensaje") ?: ""
                        val fecha = doc.getString("fecha") ?: ""

                        dibujarTarjeta(doc.id, tituloVisual, "$fecha\n\n$mensaje", yaLeida)
                    }
                }
            }
    }

    private fun dibujarTarjeta(idDoc: String, titulo: String, mensaje: String, esVista: Boolean) {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 24) }
            radius = 16f
            setCardBackgroundColor(if (esVista) Color.parseColor("#F5F5F5") else Color.WHITE)
        }

        cardView.setOnClickListener { view ->
            val popup = PopupMenu(this, view)

            if (!esVista) {
                popup.menu.add("Marcar como leída")
            }
            popup.menu.add("Borrar notificación")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Marcar como leída" -> {
                        db.collection("notificaciones").document(idDoc)
                            .update("leidaPor", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Marcada como leída", Toast.LENGTH_SHORT).show()
                                cargarNotificaciones()
                            }
                    }
                    "Borrar notificación" -> {
                        // Borrado Lógico Individual
                        db.collection("notificaciones").document(idDoc)
                            .update("borradaPor", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Notificación borrada", Toast.LENGTH_SHORT).show()
                                cargarNotificaciones()
                            }
                    }
                }
                true
            }
            popup.show()
        }

        val layoutInterno = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
        val tvTitulo = TextView(this).apply {
            text = titulo
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (esVista) Color.GRAY else Color.parseColor("#1A73E8"))
        }
        val tvMensaje = TextView(this).apply { text = mensaje; textSize = 14f; setPadding(0, 16, 0, 0) }

        layoutInterno.addView(tvTitulo); layoutInterno.addView(tvMensaje)
        cardView.addView(layoutInterno)

        if (esVista) contenedorVistas.addView(cardView) else contenedorNuevas.addView(cardView)
    }
}