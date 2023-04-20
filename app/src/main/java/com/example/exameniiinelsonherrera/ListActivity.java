package com.example.exameniiinelsonherrera;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        listView = (ListView) findViewById(R.id.listview);
        showUsers();
    }

    private void showUsers() {
        // Obtener todos los usuarios de la base de datos
        mDatabase.child("userss").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    users.add(user);
                }

                // Mostrar los usuarios en una lista utilizando el adapter UserAdapter
                ListView listView = findViewById(R.id.listview);
                UserAdapter adapter = new UserAdapter(getApplicationContext(), users);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = (User) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Seleccionar opción");
                builder.setMessage("¿Deseas modificar o ver el video?");

                builder.setPositiveButton("Modificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Realizar acción para modificar
                        Intent intent = new Intent(ListActivity.this, TransActivity.class);
                        intent.putExtra("selected_user_id", selectedUser.getIdSitio());
                        intent.putExtra("selected_user_descripcion", selectedUser.getDescripcion());
                        intent.putExtra("selected_user_fecha", selectedUser.getFecha());
                        intent.putExtra("selected_user_photo_url", selectedUser.getImagen());
                        intent.putExtra("selected_user_video_url", selectedUser.getVideo());
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("Ver video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Realizar acción para ver el video
                        Intent intent = new Intent(ListActivity.this, VideoActivity.class);
                        intent.putExtra("selected_user_id", selectedUser.getIdSitio());
                        intent.putExtra("selected_user_descripcion", selectedUser.getDescripcion());
                        intent.putExtra("selected_user_fecha", selectedUser.getFecha());
                        intent.putExtra("selected_user_photo_url", selectedUser.getImagen());
                        intent.putExtra("selected_user_video_url", selectedUser.getVideo());
                        startActivity(intent);
                    }
                });

                builder.show();
            }
        });



    }
}