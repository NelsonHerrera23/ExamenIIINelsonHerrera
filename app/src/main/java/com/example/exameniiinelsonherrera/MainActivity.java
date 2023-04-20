package com.example.exameniiinelsonherrera;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    ImageView ivPhoto;
    VideoView videoViewVideo;
    Button btnGuardar, btnLista, btnFoto, btnVideo;
    EditText txtDescripcion, etFecha;

    static final int REQUEST_IMAGE = 101;
    static final int REQUEST_GALLERY = 102;
    static final int REQUEST_VIDEO = 103;

    int lastUserId = 0; // variable que guarda el último ID generado
    Uri mediaUri;

    Uri imageUri;
    Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ControlsSet();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        mDatabase.child("lastUserId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    lastUserId = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving lastUserId", error.toException());
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los campos de entrada
                String descripcion = txtDescripcion.getText().toString();
                String fecha = etFecha.getText().toString();

                // Crear un objeto User con los valores de entrada
                User user = new User(Integer.toString(lastUserId + 1), descripcion, fecha, null, null);

                // Incrementar el último ID generado para los usuarios
                lastUserId++;

                // Subir la imagen o el video a Firebase Storage y guardar la URL en la base de datos
                if (imageUri != null || videoUri != null) {
                    if (imageUri != null) {
                        // Subir la imagen a Firebase Storage
                        StorageReference imageRef = mStorage.child("users").child(user.getIdSitio() + ".jpg");
                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Obtener la URL de la imagen subida
                                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                // Guardar la URL de la imagen en el objeto User
                                                user.setImagen(uri.toString());

                                                // Si también se ha seleccionado un video, subirlo a Firebase Storage
                                                if (videoUri != null) {
                                                    StorageReference videoRef = mStorage.child("users").child(user.getIdSitio() + ".mp4");
                                                    videoRef.putFile(videoUri)
                                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    // Obtener la URL del video subido
                                                                    videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            // Guardar la URL del video en el objeto User
                                                                            user.setVideo(uri.toString());

                                                                            // Guardar los datos del usuario en la base de datos
                                                                            addUser(user);
                                                                        }
                                                                    });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Mostrar un mensaje de error
                                                                    Toast.makeText(MainActivity.this, "Error al subir el video", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    // Si solo se ha seleccionado una imagen, guardar los datos del usuario en la base de datos con la URL de la imagen
                                                    addUser(user);
                                                }
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Mostrar un mensaje de error
                                        Toast.makeText(MainActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Si solo se ha seleccionado un video, subirlo a Firebase Storage y guardar la URL en la base de datos
                        StorageReference videoRef = mStorage.child("users").child(user.getIdSitio() + ".mp4");
                        videoRef.putFile(videoUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Obtener la URL del video subido
                                        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                // Guardar la URL del video en el objeto User
                                                user.setVideo(uri.toString());
                                                // Guardar los datos del usuario en la base de datos
                                                addUser(user);
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Mostrar un mensaje de error
                                        Toast.makeText(MainActivity.this, "Error al subir el video", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Si no se ha seleccionado ni imagen ni video, guardar los datos del usuario en la base de datos sin URL de imagen ni video
                    addUser(user);
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

        btnLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
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
    private void addUser(User user) {
        // Guardar el último ID generado para los usuarios
        mDatabase.child("lastUserId").setValue(lastUserId);

        // Guardar los datos del usuario en la base de datos
        mDatabase.child("userss").child(user.getIdSitio()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Mostrar un mensaje de éxito
                        Toast.makeText(MainActivity.this, "Usuario agregado correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Mostrar un mensaje de error
                        Toast.makeText(MainActivity.this, "Error al agregar el usuario", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error adding user to database", e);
                    }
                });
    }
    private void ControlsSet() {
        ivPhoto = findViewById(R.id.ivPhoto);
        videoViewVideo = findViewById(R.id.videoViewVideo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLista = findViewById(R.id.btnLista);
        btnFoto = findViewById(R.id.btnFoto);
        btnVideo = findViewById(R.id.btnVideo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        etFecha = findViewById(R.id.etFecha);
    }
}