package com.example.sharedocument;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageCropActivity extends AppCompatActivity {

    static int GALLERY_REQUEST_CODE = 101;
    ImageView imageView;
    Button cropImage;
    Button saveImageGallery, saveImageServer;
    static Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        imageView = findViewById(R.id.imageView);
        cropImage = findViewById(R.id.cropImage);
        saveImageGallery = findViewById(R.id.saveImageGallery);
        saveImageServer = findViewById(R.id.saveImageServer);

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        saveImageGallery.setOnClickListener(v -> saveImageInGallery());

        saveImageServer.setOnClickListener(v -> saveImageOnServer());
    }


    private void saveImageInGallery(){
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            File dir = new File(image.getAbsolutePath());
            dir.mkdirs();
            FileOutputStream outputStream = new FileOutputStream(dir);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(ImageCropActivity.this,"Saved!!!",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            System.out.println("Error:"+e);
        }
    }

    private void saveImageOnServer(){
        try {
            File file = new File(imageUri.getPath());
            Log.e("IMAGE:",imageUri.toString());
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part requestImage = MultipartBody.Part.createFormData("image",file.getName(), requestFile);

            Call<ResponsePOJO> call = RetrofitClient.getInstance().getApi().uploadNewImage(requestImage);

            call.enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                    Toast.makeText(ImageCropActivity.this, "DONE:", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                    Toast.makeText(ImageCropActivity.this, "FAILED:", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            System.out.println("error:"+e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                CropImage.activity(contentUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                imageUri = resultUri;

            }
        }
    }
}