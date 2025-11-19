package activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import daniel.chatapp.R;
import models.User;


public class LoginActivity extends AppCompatActivity {

    // Variables de la UI
    private MaterialButton btnGoogle;
    private ProgressBar progressBar;


    // Variables  de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;


    // para permitir el Sign-In de Google

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher signInLauncher; //Para recibir el resultado del login de Google


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        bd = FirebaseFirestore.getInstance();

        configurarGoogleSignIn();

        btnGoogle = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);

        // Recibe el estado del login
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d("LOGIN", "ResultCode es OK");
                        Intent data = result.getData();
                        manejarResultadoGoogleSignIn(data);
                    } else {

                        Toast.makeText(this, "Login cancelado o falló", Toast.LENGTH_LONG).show();
                        ocultarProgreso();
                    }
                }
        );

        // Listener para el boton
        btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());

        verificarSesion();
    }

    private void configurarGoogleSignIn() {

        String clientId = "673180168558-c4fdvo0slui60b4mhjcr8vou1itrdmau.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void iniciarSesionGoogle() {
        // Muestra el progressBar y deshabilita el botón para evitar clicks múltiples
        progressBar.setVisibility(View.VISIBLE);
        btnGoogle.setEnabled(false);

        // Obtiene (la pantalla) de Google para iniciar sesión
        Intent signInIntent = googleSignInClient.getSignInIntent();

        // Lanzar la actividad de Google Sign-In
        signInLauncher.launch(signInIntent);
    }

    // recibe la respuesta de Google y autentica en Firebase.
    private void manejarResultadoGoogleSignIn(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            autenticarConFirebase(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Error al iniciar Sesion" + e.getMessage(), Toast.LENGTH_LONG).show();
            ocultarProgreso();
        }

    }

    private void autenticarConFirebase(GoogleSignInAccount account) {


        // Crear credencial de Firebase con el token de Google
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        Log.d("LOGIN", "Credencial creada correctamente");

        // Autenticar en Firebase
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            Log.d("LOGIN", "signInWithCredential completado");

            if (task.isSuccessful()) {
                Log.d("LOGIN", "Autenticación Firebase EXITOSA");
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                Log.d("LOGIN", "FirebaseUser UID: " + firebaseUser.getUid());
                guardarUsuarioEnFirestore(firebaseUser, account);
            } else {
                Log.e("LOGIN", "✗ Error en autenticación Firebase");
                Log.e("LOGIN", "Error: " + task.getException());
                Toast.makeText(this, "Error de autenticacion en FireBase: " + task.getException(),
                        Toast.LENGTH_LONG).show();
                ocultarProgreso();
            }
        });
    }

    private void guardarUsuarioEnFirestore(FirebaseUser firebaseUser, GoogleSignInAccount account) {


        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                account.getDisplayName(),
                account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : ""
        );

        Handler timeoutHandler = new Handler();
        final boolean[] operacionCompletada = {false};

        Runnable timeoutRunnable = () -> {
            if (!operacionCompletada[0]) {
                Log.e("LOGIN", "TIMEOUT: La operación de Firestore tardó más de 15s");
                Toast.makeText(this, "La operación está tardando demasiado. Verifica tu conexión.",
                        Toast.LENGTH_LONG).show();
                ocultarProgreso();
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, 15000);

        // Guardar en Firestore
        bd.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    ocultarProgreso();
                    irAHomeActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("LOGIN", "ERROR al guardar en Firestore");
                    Log.e("LOGIN", "Error: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar usuario: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    ocultarProgreso();
                });
    }
    private void verificarSesion() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Usuario ya está logueado, ir directamente a MainActivity
            irAHomeActivity();
        }
    }

    private void irAHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void ocultarProgreso() {
        progressBar.setVisibility(View.GONE);
        btnGoogle.setEnabled(true);
    }



}