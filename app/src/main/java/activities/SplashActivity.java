package activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import daniel.chatapp.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView ivSplashAnimation;
    private FirebaseAuth mAuth;
    private Handler handler;
    private boolean hasNavigated = false;
    private static final int MIN_SPLASH_TIME = 2000; // Mínimo 2 segundos
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d("SPLASH", "=== SplashActivity iniciado ===");

        startTime = System.currentTimeMillis();
        mAuth = FirebaseAuth.getInstance();
        ivSplashAnimation = findViewById(R.id.ivSplashAnimation);
        handler = new Handler();

        cargarAnimacion();
    }

    private void cargarAnimacion() {
        try {
            // Cargar GIF con Glide
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.splash_logo)
                    .listener(new RequestListener<GifDrawable>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<GifDrawable> target, boolean isFirstResource) {
                            Log.e("SPLASH", "═══════════════════════════════════════");
                            Log.e("SPLASH", "✗ ERROR AL CARGAR GIF");
                            Log.e("SPLASH", "  Mensaje: " + (e != null ? e.getMessage() : "null"));
                            Log.e("SPLASH", "═══════════════════════════════════════");

                            if (e != null) {
                                e.printStackTrace();
                            }

                            Toast.makeText(SplashActivity.this,
                                    "Error: splash_animation.gif no encontrado en drawable/",
                                    Toast.LENGTH_LONG).show();

                            // Navegar después de tiempo mínimo
                            navegarDespuesDeDelay();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model,
                                                       Target<GifDrawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d("SPLASH", "═══════════════════════════════════════");
                            Log.d("SPLASH", "✓ GIF CARGADO CORRECTAMENTE");
                            Log.d("SPLASH", "  Frames: " + resource.getFrameCount());
                            Log.d("SPLASH", "  Duración: " +
                                    (resource.getFrameCount() * 33) + "ms aprox");
                            Log.d("SPLASH", "═══════════════════════════════════════");

                            // Configurar para que se reproduzca solo una vez
                            resource.setLoopCount(1);

                            // Calcular duración total del GIF
                            int frameCount = resource.getFrameCount();
                            int gifDuration = frameCount * 33; // Aprox 30fps = 33ms por frame

                            // Esperar a que termine el GIF
                            handler.postDelayed(() -> {
                                if (!hasNavigated && !isFinishing()) {
                                    verificarSesionYNavegar();
                                }
                            }, gifDuration);

                            return false;
                        }
                    })
                    .into(ivSplashAnimation);

        } catch (Exception e) {
            Log.e("SPLASH", "✗ EXCEPCIÓN al cargar GIF: " + e.getMessage());
            e.printStackTrace();
            navegarDespuesDeDelay();
        }
    }

    private void navegarDespuesDeDelay() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = Math.max(0, MIN_SPLASH_TIME - elapsedTime);

        handler.postDelayed(() -> {
            if (!hasNavigated && !isFinishing()) {
                verificarSesionYNavegar();
            }
        }, remainingTime);
    }

    private void verificarSesionYNavegar() {
        if (isFinishing() || hasNavigated) {
            return;
        }

        hasNavigated = true;
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent;
        if (currentUser != null) {
            Log.d("SPLASH", "✓ Usuario autenticado: " + currentUser.getEmail());
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            Log.d("SPLASH", "✓ Usuario no autenticado");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancelar cualquier handler pendiente
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Glide se limpiará automáticamente cuando la actividad sea destruida
        // No necesitamos limpiarlo manualmente aquí
    }
}