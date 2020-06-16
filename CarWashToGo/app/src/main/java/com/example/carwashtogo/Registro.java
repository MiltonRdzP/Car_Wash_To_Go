package com.example.carwashtogo;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Registro extends AppCompatActivity {
    public static final String TAG = "TAG";
    TextView Login, verTerminos;
    EditText email, password, telefono, edad, name_user;
    EditText confPass;
    Button btn_registrar;
    FirebaseAuth fAuth;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        email = findViewById(R.id.email_user);
        password = findViewById(R.id.password);
        btn_registrar = findViewById(R.id.btn_registrar);
        progressBar = findViewById(R.id.prog_bar);
        Login = findViewById(R.id.YaTengo_user);
        telefono = findViewById(R.id.telefono_user);
        edad = findViewById(R.id.edad_user);
        name_user = findViewById(R.id.Usuario);
        confPass = findViewById(R.id.password2);
        fAuth = FirebaseAuth.getInstance();
        verTerminos = findViewById(R.id.AbrrTerminos);


        verTerminos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), terminos.class);
                startActivity(intent);
                return;
            }
        });
        btn_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nombre = name_user.getText().toString().trim();
                final String email_ = email.getText().toString().trim();
                final String pass = password.getText().toString().trim();
                final String age = edad.getText().toString().trim();
                final String phone = telefono.getText().toString().trim();
                final String pass2 = confPass.getText().toString().trim();

                if (!pass.equals(pass2)){
                    password.setError(getText(R.string.contranocoinci));
                    return;
                }
                if(pass.length() <= 5){
                    password.setError(getText(R.string.min5));
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email_, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser fuser = fAuth.getCurrentUser();
                            String email = fuser.getEmail();
                            String uid = fuser.getUid();
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("name", nombre);
                            hashMap.put("uid", uid);
                            hashMap.put("email", email);
                            hashMap.put("edad", age);
                            hashMap.put("image", "");
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("telefono", phone);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);
                            DatabaseReference reference2 = database.getReference("Users").child("CarWashMan");
                            reference2.child(uid).setValue(hashMap);
                            DatabaseReference reference3 = database.getReference("Users").child("Customers");
                            reference3.child(uid).setValue(hashMap);
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Registro.this, R.string.veremailenviada, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, ""+ R.string.error +e.getMessage());
                                }
                            });

                            Toast.makeText(Registro.this, R.string.ingresocompletado, Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), Login.class));
                        }else{
                        Toast.makeText(Registro.this, "Error! ", Toast.LENGTH_SHORT);
                    }
                }
            });
        }

        });
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        name_user.addTextChangedListener(loginTextWatcher);
        email.addTextChangedListener(loginTextWatcher);
        edad.addTextChangedListener(loginTextWatcher);
        telefono.addTextChangedListener(loginTextWatcher);
        confPass.addTextChangedListener(loginTextWatcher);
        password.addTextChangedListener(loginTextWatcher);
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String username = name_user.getText().toString().trim();
            String useremail = email.getText().toString().trim();
            String useredad = edad.getText().toString().trim();
            String usertel = telefono.getText().toString().trim();
            String userconpass = confPass.getText().toString().trim();
            String userpass = password.getText().toString().trim();
            btn_registrar.setEnabled(!username.isEmpty() && !useremail.isEmpty() && !useredad.isEmpty() && !usertel.isEmpty() && !userpass.isEmpty() && !userconpass.isEmpty());

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}
