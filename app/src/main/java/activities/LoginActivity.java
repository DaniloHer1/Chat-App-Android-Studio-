package activities;

import android.content.Intent;
import android.os.Bundle;
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
                    Log.d("LOGIN", "📥 Launcher ejecutado! ResultCode: " + result.getResultCode());
                    Toast.makeText(this, "📥 Resultado recibido: " + result.getResultCode(), Toast.LENGTH_LONG).show();

                    if (result.getResultCode() == RESULT_OK) {
                        Log.d("LOGIN", "✅ ResultCode es OK");
                        Toast.makeText(this, "✅ Result OK, procesando...", Toast.LENGTH_LONG).show();
                        Intent data = result.getData();
                        manejarResultadoGoogleSignIn(data);
                    } else {

                        Log.e("LOGIN", "❌ ResultCode NO es OK: " + result.getResultCode());
                        Toast.makeText(this, "❌ Login cancelado o falló", Toast.LENGTH_LONG).show();
                        ocultarProgreso();
                    }
                }
        );

        // Listener para el boton
        btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());

        verificarSesion();
    }

    private void configurarGoogleSignIn() {

        String clientId = "420892615485-vco03p9fm9uuj0bsquo5divrbj173npp.apps.googleusercontent.com";

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

        // Autenticar en Firebase
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Login exitoso
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                guardarUsuarioEnFirestore(firebaseUser, account);
            } else {
                // Login no exitoso
                Toast.makeText(this, "Error de autenticacion en FireBase", Toast.LENGTH_LONG).show();
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

        // Guardar en Firestore (colección "users", documento con el UID del usuario)
        bd.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(voihd->{
                   // Usuario guardado correctamente
                    Toast.makeText(this, "¡Bienvenido " + user.getDisplayName() + "!",
                            Toast.LENGTH_SHORT).show();
                    ocultarProgreso();
                    irAHomeActivity();
                })
                .addOnFailureListener(e->{
                    // Error al guardar
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
        Intent intent =new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void ocultarProgreso() {
        progressBar.setVisibility(View.GONE);
        btnGoogle.setEnabled(true);
    }



}