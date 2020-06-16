package com.example.carwashtogo.Adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashtogo.ChatActivity;
import com.example.carwashtogo.R;
import com.example.carwashtogo.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;


public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUser> userList;//get user info
    private HashMap<String, String> lastMessageMap;


    public AdapterChatList(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        //obtener datos
        final String hisUid = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String lastMessage = lastMessageMap.get(hisUid);


        holder.nameTv.setText(userName);
        if(lastMessage==null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);

        }
        try{
            Picasso.get().load(userImage).fit().placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).fit().into(holder.profileIv);
        }
        //set online status of the other user
        if (userList.get(i).getOnlineStatus().equals("online")){
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
    /*    holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Borrar");
                builder.setMessage("Eliminar mensaje");
                builder.setPositiveButton("ingresar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUid);
                        context.startActivity(intent);
                    }
                });
                builder.setNegativeButton("borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                      //  dialog.dismiss();
                        userList.remove(i);
                        notifyItemRemoved(i);
                        FirebaseDatabase.getInstance().getReference("Chatlist").child(myUID).child(hisUid).removeValue();
                    }
                });
                builder.create().show();
            }
        });*/
    }
    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
