package com.example.carwashtogo.Adaptadores;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashtogo.ChatActivity;
import com.example.carwashtogo.R;
import com.example.carwashtogo.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.senab.photoview.PhotoViewAttacher;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;
    PhotoViewAttacher photo;

    String myUid;

    public AdapterPost(Context context, List<ModelPost> postList){
        this.context = context;
        this.postList = postList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder((view));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int i) {
        final String hisUID = postList.get(i).getUid();

        final String uid = postList.get(i).getUid();
        String uEmail = postList.get(i).getuEmail();
        final String uName = postList.get(i).getuName();
        final String uDp = postList.get(i).getuDp();
        final String uId = postList.get(i).getpId();
        String uTitle = postList.get(i).getpTitle();
        String uDescription = postList.get(i).getpDescription();
        final String uImage = postList.get(i).getpImage();
        String uTimestamp = postList.get(i).getpTime();
        String pmunicipio = postList.get(i).getpMunicipio();

        //convert timestamp to dd/m/year
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(uTimestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(uTitle);
        holder.pDescriptionTv.setText(uDescription);
        holder.pMunicipio.setText(pmunicipio);

        if (uid.equals(hisUID)){
            holder.moreBtn.setVisibility(View.INVISIBLE);
        }

        try{
            Picasso.get().load(uDp).fit().placeholder(R.drawable.ic_default_img).into(holder.uPictureTv);
        }catch (Exception e){

        }



        //si no existe imagen "no imagen"

        if(uImage.equals("noImagen")){
            //hide imageview
            holder.pImageTv.setVisibility(View.GONE);
        }else{
                //show image view
            holder.pImageTv.setVisibility(View.VISIBLE);
            try{

                Picasso.get().load(uImage).fit().centerCrop().into(holder.pImageTv);
                //Picasso.get().load(uImage).fit().centerCrop().into(holder.pImageTv);
            }catch (Exception e){

            }
        }

        if(uid.equals(myUid)){
            holder.moreBtn.setVisibility(View.VISIBLE);
            holder.btn.setVisibility(View.GONE);
        }
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, uId, uImage);
            }
        });
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder  = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Desea comenzar un chat de lavado al usuario: "+ uName);
                builder.setTitle("Lavar");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, ""+uName, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                /*//crear el chat con la persona que publico el anuncio para lavarle el coche
                Toast.makeText(context, ""+uName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUID);
                context.startActivity(intent);*/
            }
        });

    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String uId, final String uImage) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //CREAR MENU PARA OPCION BORRAR
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if(uid.equals(myUid)){
            //añadiendo item al menu
            popupMenu.getMenu().add(Menu.NONE, 0,0, "Borrar");
        }
        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    beginDelete(uId, uImage);

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String uId, String uImage) {
        if(uImage.equals("noImagen")){
            deleteWithoutImage(uId);
        }else{
            deleteWithImage(uId, uImage);
        }
    }

    private void deleteWithImage(final String uId, String uImage) {
        //progrss bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Borrando...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(uImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(uId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ds.getRef().removeValue();//remover valores de firebase
                        }
                        Toast.makeText(context, "Se ha borrado exitosamente" , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context, ""+ e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String uId) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Borrando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(uId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();//remover valores de firebase
                }
                Toast.makeText(context, "Se ha borrado exitosamente" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //view for row
        ImageView uPictureTv, pImageTv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pMunicipio;
        ImageButton moreBtn;
        Button btn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            uPictureTv = itemView.findViewById(R.id.uPictureIv);
            pImageTv = itemView.findViewById(R.id.pImageTv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            btn = itemView.findViewById(R.id.lavar);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            pMunicipio = itemView.findViewById(R.id.pMunicipio);
            profileLayout = itemView.findViewById(R.id.profileLayout);
           // photo = new PhotoViewAttacher(pImageTv);

        }
    }
}
