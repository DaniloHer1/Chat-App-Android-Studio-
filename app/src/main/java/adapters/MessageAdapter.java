package adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import daniel.chatapp.R;
import models.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    // Tipos de vista
    private static final int VIEW_SENT = 1;
    private static final int VIEW_RECEIVED = 2;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        // Si el senderId coincide con el usuario actual, es un mensaje enviado
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_SENT;
        } else {
            return VIEW_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        Log.d("ADAPTER", "onBindViewHolder llamado para posición: " + position);
        Log.d("ADAPTER", "Mensaje: " + message.getMessage());
        Log.d("ADAPTER", "Tipo: " + (holder.getItemViewType() == VIEW_SENT ? "SENT" : "RECEIVED"));

        if (holder.getItemViewType() == VIEW_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder para mensajes enviados
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;
        TextView tvMessageTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(Message message) {
            tvMessageText.setText(message.getMessage());
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // ViewHolder para mensajes recibidos
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;
        TextView tvMessageTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(Message message) {
            tvMessageText.setText(message.getMessage());
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // Metodo para formatear el timestamp a hora legible
    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}