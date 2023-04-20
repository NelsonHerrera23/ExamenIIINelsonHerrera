package com.example.exameniiinelsonherrera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class TransActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    ImageView ivPhoto;
    VideoView videoViewVideo;
    Button btnActualizar,btnEliminar,btnFoto, btnVideo;
    EditText txtDescripcion, etFecha;
    String selectedUserId;
    String selectedUserPhotoUrl,selectedUserVideoUrl;
    int lastUserId = 0; // variable que guarda el último ID generado
    Uri mediaUri;

    static final int REQUEST_IMAGE = 101;
    static final int REQUEST_GALLERY = 102;
    static final int REQUEST_VIDEO = 103;

    Uri imageUri;
    Uri videoUri;

    static final int PETICION_ACCESS_CAM = 201;
    String currentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        ControlsSet();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        DatabaseReference usersRef = mDatabase.child("userss");

        // Obtener los datos del usuario seleccionado enviados desde ListActivity
        Intent intent = getIntent();
        selectedUserId = intent.getStringExtra("selected_user_id");
        String selected_user_descripcion = intent.getStringExtra("selected_user_descripcion");
        String selected_user_fecha = intent.getStringExtra("selected_user_fecha");
        selectedUserPhotoUrl = intent.getStringExtra("selected_user_photo_url");
        selectedUserVideoUrl = intent.getStringExtra("selected_user_video_url");

        txtDescripcion.setText(selected_user_descripcion);
        etFecha.setText(selected_user_fecha);

        // cargar la imagen desde la URL usando una biblioteca como Glide o Picasso
        Glide.with(this).load(selectedUserPhotoUrl).into(ivPhoto);

        // cargar el video desde la URL usando el método setVideoURI()
        videoViewVideo.setVideoURI(Uri.parse(selectedUserVideoUrl));
        videoViewVideo.requestFocus();
        videoViewVideo.start();

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una referencia a la ubicación del usuario seleccionado en la base de datos
                DatabaseReference userRef = mDatabase.child("userss").child(selectedUserId);

                // Obtener los valores de los campos de entrada
                String descripcion = txtDescripcion.getText().toString();
                String fecha = etFecha.getText().toString();

                // Crear un objeto User con los valores de entrada
                User user = new User(selectedUserId, descripcion, fecha, selectedUserPhotoUrl, selectedUserVideoUrl);

                // Actualizar los valores del objeto User con la imagen o el video seleccionado, si es que se cargó uno nuevo
                if (imageUri != null) {
                    user.setImagen(null);
                }
                if (videoUri != null) {
                    user.setVideo(null);
                }

                // Subir la nueva imagen o el nuevo video a Firebase Storage y actualizar la URL en el objeto User
                if (imageUri != null || videoUri != null) {
                    if (imageUri != null) {
                        StorageReference imageRef = mStorage.child("users").child(selectedUserId + ".jpg");
                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String imageUrl = uri.toString();
                                                user.setImagen(imageUrl);
                                                // Actualizar los valores del usuario en la base de datos
                                                userRef.setValue(user);
                                                Toast.makeText(TransActivity.this, "La imagen se ha actualizado correctamente.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TransActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    if (videoUri != null) {
                        StorageReference videoRef = mStorage.child("users").child(selectedUserId + ".mp4");
                        videoRef.putFile(videoUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String videoUrl = uri.toString();
                                                user.setVideo(videoUrl);
                                                // Actualizar los valores del usuario en la base de datos
                                                userRef.setValue(user);
                                                Toast.makeText(TransActivity.this, "El video se ha actualizado correctamente.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TransActivity.this, "Error al subir el video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Si no se cargó una nueva imagen o video, actualizar solo los valores de texto en la base de datos
                    userRef.setValue(user);
                    Toast.makeText(TransActivity.this, "Los valores del usuario se han actualizado correctamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_VIDEO);
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(selectedUserId).removeValue();
                finish();
            }
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            // Se ha seleccionado una imagen
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(ivPhoto);
        } else if (requestCode == REQUEST_VIDEO && resultCode == RESULT_OK && data != null) {
            // Se ha seleccionado un video
            videoUri = data.getData();
            videoViewVideo.setVideoURI(videoUri);
            videoViewVideo.start();
        }
    }
    private void ControlsSet() {
        ivPhoto = findViewById(R.id.ivPhoto);
        videoViewVideo = findViewById(R.id.videoViewVideo);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnFoto = findViewById(R.id.btnFoto);
        btnVideo = findViewById(R.id.btnVideo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        etFecha = findViewById(R.id.etFecha);
    }
}