package com.example.nexocitas_tfg

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val toolbar = findViewById<Toolbar>(R.id.toolbarPerfil)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvNombre = findViewById<TextView>(R.id.tvNombrePerfil)
        val tvEmail = findViewById<TextView>(R.id.tvEmailPerfil)
        val tvTelefono = findViewById<TextView>(R.id.tvTelefonoPerfil)
        val tvInicial = findViewById<TextView>(R.id.tvInicialAvatar)
        val btnLogout = findViewById<Button>(R.id.btnLogoutPerfil)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombreReal = document.getString("nombre") ?: "Usuario Nexo"
                        val emailReal = document.getString("email") ?: auth.currentUser?.email ?: "Sin email"
                        val telefonoReal = document.getString("telefono") ?: "No proporcionado"

                        tvNombre.text = nombreReal
                        tvEmail.text = emailReal
                        tvTelefono.text = telefonoReal

                        if (nombreReal.isNotEmpty()) {
                            tvInicial.text = nombreReal.substring(0, 1).uppercase()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar los datos del perfil", Toast.LENGTH_SHORT).show()
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_perfil
        verificarRolUsuario(bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mis_reservas -> {
                    startActivity(Intent(this, MisReservasActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_admin -> {
                    startActivity(Intent(this, AdminReservasActivity::class.java))
                    true
                }
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_perfil, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_contacto -> {
                startActivity(Intent(this, ContactoActivity::class.java))
                true
            }
            R.id.menu_sobre_nosotros -> {
                startActivity(Intent(this, SobreNosotrosActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun verificarRolUsuario(bottomNavigation: BottomNavigationView) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val rol = document.getString("rol")?.lowercase()
                        if (rol == "admin" || rol == "administrador") {
                            bottomNavigation.menu.findItem(R.id.nav_admin)?.isVisible = true
                        }
                    }
                }
        }
    }
}