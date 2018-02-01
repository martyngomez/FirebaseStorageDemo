package com.martyngomez.firebasedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int CHOOSER_IMAGES = 1;
    private static final String TAG = "MainActivity";
    private Button btnDownload;
    private Button btnUpload;
    private ImageView imvImage;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference(); // Inicializa la referencia

        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imvImage = (ImageView) findViewById(R.id.imvImage);

        imvImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*"); //Config tipo de archivo
                i.setAction(Intent.ACTION_GET_CONTENT); // Abre seleccionador de archivos
                startActivityForResult(Intent.createChooser(i, "Selecciona una imagen"), CHOOSER_IMAGES);
            }
        });


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageReference astronautaRef = storageReference.child("archivo.png"); // Crea referencia

                imvImage.setDrawingCacheEnabled(true); // Permanece en cache
                imvImage.buildDrawingCache();

                Bitmap bitmap = imvImage.getDrawingCache(); //Devuelve Bitmap y asigna a variable Bitmap
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // Comprime bitmap y pasa los datos a baos

                byte[] archivoByte = baos.toByteArray(); // Pasa bitmap a un array de bytes

                UploadTask uploadTask = astronautaRef.putBytes(archivoByte); // Sube el array de bytes
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { // Listener de exito o falla
                        Log.e(TAG, "Ocurrió un error en la subida");
                        e.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // taskSnapshot es la imagen subida
                        Toast.makeText(MainActivity.this, "Subida con Éxito", Toast.LENGTH_SHORT).show();
                        String downloadUri = taskSnapshot.getDownloadUrl().getPath(); //Obtiene URI de imagen
                        Log.w(TAG, "image URL: " + downloadUri);

                    }
                });

            }
        });


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final File file;
                try {
                    file = File.createTempFile("robot", "jpg"); // Crea un archivo temporal
                    storageReference.child("robot.jpg").getFile(file) // Accede al archivo
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath()); // Convierte a bitmap la imagen
                                    imvImage.setImageBitmap(bitmap); // Asignamos bitmap a imagen
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Ocurrio un error al mostrar la imagen");
                                    e.printStackTrace();
                                }
                            });
                }catch (Exception e){
                    Log.e(TAG, "Ocurrió un error en la descarga de imágenes");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSER_IMAGES){
            Uri imageUri = data.getData();
            if (imageUri != null){
                imvImage.destroyDrawingCache();
                imvImage.setImageURI(imageUri); //Obtiene URI de la imagen seleccionada
            }
        }
    }
}
