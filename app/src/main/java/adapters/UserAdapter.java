package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import daniel.chatapp.R;
import models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserClickListener listener;

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {

        User user=userList.get(position);
        holder.bind(user,listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static  class UserViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivUserPhoto;
        private TextView tvUserName;
        private TextView tvUserEmail;

        public UserViewHolder(View view) {

            super(view);

            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        }

        public void bind(User user, OnUserClickListener listener) {
            tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
            tvUserEmail.setText(user.getEmail());

            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {

                // Verificar que el contexto es válido
                Context context = itemView.getContext();
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        Glide.with(context)
                                .load(user.getPhotoUrl())
                                .placeholder(R.drawable.ic_chat_logo)
                                .error(R.drawable.ic_chat_logo)
                                .into(ivUserPhoto);
                    }
                } else {
                    // Si no es una Activity, usar el contexto directamente
                    Glide.with(context.getApplicationContext())
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_chat_logo)
                            .error(R.drawable.ic_chat_logo)
                            .into(ivUserPhoto);
                }
            } else {
                ivUserPhoto.setImageResource(R.drawable.ic_launcher_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}
