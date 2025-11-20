package activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.UserAdapter;
import daniel.chatapp.R;
import models.User;
import utils.LightSensorManager;

public class HomeActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private TextView tvUserName;
    private TextView tvNoUsers;
    private ImageView btnLogout;
    private ImageView ivProfilePic;
    private RecyclerView recyclerViewUsers;

    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;
    private FirebaseUser currentUser;

    private List<User> userList;
    private UserAdapter userAdapter;

    private LightSensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRÍTICO: Aplicar el tema usando AppCompatDelegate
        aplicarTema();

        setContentView(R.layout.activity_home);

        Log.d("HOME", "=== onCreate iniciado ===");

        mAuth = FirebaseAuth.getInstance();
        bd = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvUserName = findViewById(R.id.tvUserName);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        ivProfilePic = findViewById(R.id.ivProfilePic);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        cargarDatosUsuarioActual();
        cargarListaUsuario();

        btnLogout.setOnClickListener(v -> cerrarSesion());

        // Código temporal de prueba - toca la foto para cambiar tema
        ivProfilePic.setOnClickListener(v -> {
            Log.d("HOME", "=== CLICK EN FOTO DE PERFIL ===");
            SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
            boolean currentTheme = prefs.getBoolean("is_dark_mode", false);
            boolean newTheme = !currentTheme;

            Log.d("HOME", "Tema actual: " + (currentTheme ? "OSCURO" : "CLARO"));
            Log.d("HOME", "Nuevo tema: " + (newTheme ? "OSCURO" : "CLARO"));

            prefs.edit().putBoolean("is_dark_mode", newTheme).apply();

            // Aplicar el tema usando AppCompatDelegate
            aplicarTema();
        });

        inicializarSensor();

        Log.d("HOME", "=== onCreate completado ===");
    }

    private void aplicarTema() {
        boolean isDark = LightSensorManager.getSavedTheme(this);
        Log.d("HOME", "Aplicando tema con AppCompatDelegate: " + (isDark ? "OSCURO" : "CLARO"));

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void cargarDatosUsuarioActual() {
        bd.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isFinishing() || isDestroyed()) {
                        Log.w("HOME", "Actividad destruida, cancelando carga de imagen");
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        String displayName = documentSnapshot.getString("displayName");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        tvUserName.setText(displayName != null ? displayName : "usuario");

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            if (!isFinishing() && !isDestroyed()) {
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_chat_logo)
                                        .error(R.drawable.ic_chat_logo)
                                        .into(ivProfilePic);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        tvUserName.setText(currentUser.getEmail());
                    }
                });
    }

    private void cargarListaUsuario() {
        bd.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);

                        if (!user.getUid().equals(currentUser.getUid())) {
                            userList.add(user);
                        }
                    }

                    if (userList.isEmpty()) {
                        tvNoUsers.setVisibility(View.VISIBLE);
                        recyclerViewUsers.setVisibility(View.GONE);
                    } else {
                        tvNoUsers.setVisibility(View.GONE);
                        recyclerViewUsers.setVisibility(View.VISIBLE);
                    }

                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
    }

    private void cerrarSesion() {
        mAuth.signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("673180168558-c4fdvo0slui60b4mhjcr8vou1itrdmau.apps.googleusercontent.com")
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irALogin();
        });
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUserClick(User user) {
        if (user == null || user.getUid() == null || user.getDisplayName() == null) {
            Toast.makeText(this, "Error: datos de usuario incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        intent.putExtra("receiverUserId", user.getUid());
        intent.putExtra("receiverName", user.getDisplayName());
        intent.putExtra("receiverPhotoUrl", user.getPhotoUrl());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("HOME", "onResume - Iniciando escucha del sensor");

        if (sensorManager != null) {
            sensorManager.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("HOME", "onPause - Deteniendo escucha del sensor");

        if (sensorManager != null) {
            sensorManager.stopListening();
        }
    }

    private void inicializarSensor() {
        Log.d("HOME", "Inicializando sensor de luz...");

        sensorManager = new LightSensorManager(this, isDarkMode -> {
            Log.d("HOME", "Callback de cambio de tema recibido: isDarkMode=" + isDarkMode);

            runOnUiThread(() -> {
                Log.d("HOME", "Aplicando nuevo tema desde sensor...");
                aplicarTema();
            });
        });

    }
}