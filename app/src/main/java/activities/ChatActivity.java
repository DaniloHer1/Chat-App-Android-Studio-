package activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import adapters.MessageAdapter;
import daniel.chatapp.R;
import models.Message;
import com.bumptech.glide.Glide;


public class ChatActivity extends AppCompatActivity {

    // UI

    private TextView tvReceivedName;
    private ImageView ivReceiverPhoto;
    private ImageView btnBack;
    private ImageView btnSend;
    private EditText etMessage;
    private RecyclerView recyclerViewMessages;

    //FireBase

    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;

    //Datos

    private String currentUserId;
    private String receiverUserId;
    private String receiverName;
    private String chatId;
    private String receiverPhotoUrl;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        bd = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getStringExtra("receiverUserId");
        receiverName = getIntent().getStringExtra("receiverName");
        receiverPhotoUrl = getIntent().getStringExtra("receiverPhotoUrl");

        if (receiverUserId == null || receiverName == null) {
            Toast.makeText(this, "Error al cargar el chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVista();

        chatId = generarChatId(currentUserId, receiverUserId);

        iniciarRecyclerView();

        cargarMensajes();

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> enviarMensaje());


    }


    private void inicializarVista() {

        tvReceivedName = findViewById(R.id.tvReceiverName);
        ivReceiverPhoto = findViewById(R.id.ivReceiverPhoto);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // Mostrar Nombre Receptor
        tvReceivedName.setText(receiverName);
        if (receiverPhotoUrl != null && !receiverPhotoUrl.isEmpty()) {
            Glide.with(this)
                    .load(receiverPhotoUrl)
                    .placeholder(R.drawable.ic_chat_logo)
                    .error(R.drawable.ic_chat_logo)
                    .into(ivReceiverPhoto);
        }
    }

    private void iniciarRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

    }

    private String generarChatId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public void enviarMensaje() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }
        String messageId = bd.collection("chats")
                .document(chatId)
                .collection("messages")
                .document()
                .getId();
        Message message = new Message(
                messageId,
                currentUserId,
                receiverUserId,
                messageText,
                System.currentTimeMillis()
        );
        bd.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CHAT", "MENSAJE EVIADO CORRECTAMENTE");
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("CHAT", "ERROR AL ENVIAR MENSAJE" + e.getMessage());
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarMensajes() {
        messageListener = bd.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CHAT", "Error al escuchar mensajes: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {

                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                Log.d("CHAT", "✓ Mensaje agregado:");
                                Log.d("CHAT", "  messageId: " + message.getMessageId());
                                Log.d("CHAT", "  senderId: " + message.getSenderId());
                                Log.d("CHAT", "  message: " + message.getMessage());
                                Log.d("CHAT", "  timestamp: " + message.getTimestamp());
                                messageList.add(message);

                                int position = messageList.size() - 1;
                                messageAdapter.notifyItemInserted(position);
                                recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el listener cuando se cierra la actividad
        if (messageListener != null) {
            messageListener.remove();

        }
    }
}