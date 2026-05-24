package com.example.nexocitas_tfg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. Enlazamos las vistas
        val ivLogo = findViewById<ImageView>(R.id.ivLogoSplash)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloSplash)
        val tvSubtitulo = findViewById<TextView>(R.id.tvSubtituloSplash)
        val pbCarga = findViewById<ProgressBar>(R.id.pbSplash)

        // 2. Animación del Logo (Aparece creciendo y hace un pequeño rebote)
        ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(OvershootInterpolator())
            .start()

        // 3. Animación de los textos (Suben suavemente mientras aparecen con retraso)
        tvTitulo.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(300) // Empieza un poco después del logo
            .start()

        tvSubtitulo.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(500)
            .start()

        pbCarga.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(800)
            .start()

        // 4. Temporizador y lógica de redirección inteligente
        Handler(Looper.getMainLooper()).postDelayed({

            // MAGIA UX: Comprueba si el usuario ya tenía la sesión iniciada
            val usuarioActual = FirebaseAuth.getInstance().currentUser
            val intent = if (usuarioActual != null) {
                // Ya está logueado, va directo al catálogo
                Intent(this, HomeActivity::class.java)
            } else {
                // No está logueado, va a la pantalla de Login
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)

            // Transición suave de fundido al cambiar de pantalla
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            // Cerramos esta pantalla
            finish()

        }, 2500) // Hemos subido medio segundo el tiempo para disfrutar de la animación
    }
}