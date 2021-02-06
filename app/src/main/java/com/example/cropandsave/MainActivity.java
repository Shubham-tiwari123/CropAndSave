package com.example.cropandsave;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int GALLERY_REQUEST_CODE = 105;
    static int CAMERA_REQUEST_CODE = 102;
    static int CAMERA_PERM_CODE = 101;

    ImageView selectedImage;
    Button cameraBtn;
    Button galleryBtn;
    Button saveGallery;
    Button saveServer;
    String currentPhotoPath;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedImage = findViewById(R.id.image);
        cameraBtn = findViewById(R.id.camera);
        galleryBtn = findViewById(R.id.gallery);
        saveGallery = findViewById(R.id.saveGallery);
        saveServer = findViewById(R.id.saveServer);

        cameraBtn.setOnClickListener(v -> askCameraPermission());

        galleryBtn.setOnClickListener(v -> captureImageGallery());

        saveGallery.setOnClickListener(v -> saveImageGallery());

        saveServer.setOnClickListener(v -> saveImageServer());
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else{
            captureImageCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImageCamera();
            } else {
                Toast.makeText(MainActivity.this, "Camera Permission is Required!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void captureImageGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void captureImageCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Exception", ex.getMessage() );
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.cropandsave.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void saveImageGallery(){
        try {
            BitmapDrawable drawable = (BitmapDrawable) selectedImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            File dir = new File(image.getAbsolutePath());
            dir.mkdirs();
            FileOutputStream outputStream = new FileOutputStream(dir);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(MainActivity.this,"Saved!!!",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            System.out.println("Error:"+e);
        }
    }

    private void saveImageServer(){
        try {
            File file = new File(imageUri.getPath());
            Log.e("IMAGE:",imageUri.toString());
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part requestImage = MultipartBody.Part.createFormData("image",file.getName(), requestFile);

            Call<ResponsePOJO> call = RetrofitClient.getInstance().getApi().uploadNewImage(requestImage);

            call.enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                    Toast.makeText(MainActivity.this, "DONE:", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "FAILED:", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            System.out.println("error:"+e);
        }
    }

    private void cropImage(){
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK){
                File file = new File(currentPhotoPath);
                imageUri = Uri.fromFile(file);
                cropImage();
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK){
                imageUri = data.getData();
                cropImage();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                selectedImage.setImageURI(resultUri);
                imageUri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                System.out.println("Error:"+error);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


}