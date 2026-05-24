package com.example.nexocitas_tfg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val ivVolverLogin = findViewById<ImageView>(R.id.ivVolverLogin)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        // ✨ AHORA LA FLECHA FUERZA IR AL LOGIN ✨
        ivVolverLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val email = etEmail.text.toString().trim().lowercase()
            val telefono = etTelefono.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty()) {

                btnRegistrar.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            val datosUsuario = hashMapOf(
                                "nombre" to nombre,
                                "email" to email,
                                "telefono" to telefono,
                                "rol" to "cliente"
                            )

                            if (userId != null) {
                                db.collection("usuarios").document(userId)
                                    .set(datosUsuario)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()

                                        auth.signOut()

                                        val intent = Intent(this, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                                        btnRegistrar.isEnabled = true
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            btnRegistrar.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}