package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private TextView tvUserName;
    private TextView tvNoUsers;
    private ImageView btnLogout;
    private RecyclerView recyclerViewUsers;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;
    private FirebaseUser currentUser;

    private List<User> userList;

    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        bd = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvUserName = findViewById(R.id.tvUserName);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        cargarDatosUsuarioActual();
        cargarListaUsuario();

        btnLogout.setOnClickListener(v -> cerrarSesion());


    }


    private void cargarDatosUsuarioActual() {
        bd.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String diplayname = documentSnapshot.getString("displayName");
                        tvUserName.setText(diplayname != null ? diplayname : "usuario");

                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText(currentUser.getEmail());
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

        // 2. Cerrar sesión en Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("673180168558-c4fdvo0slui60b4mhjcr8vou1itrdmau.apps.googleusercontent.com")
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Sesión de Google cerrada
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
        Toast.makeText(this, "Selecionaste " + user.getDisplayName(), Toast.LENGTH_SHORT);

    }
}