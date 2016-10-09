package uc3mprojects.pablo.ex1aliamate;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Pablo Alías Mateos on 02/10/2016.
 * Description: This is the first Android exercise
 */

public class fragment_camara_time_save extends Fragment {

    public static final int REQUEST_CAMERA = 10;                 // without extends Fragment, it would be just a void java class
    public static final int IMAGE_PERMISSION_REQUEST_CODE = 1;

    private View fragment_view;
    private ImageView imageView_survey_picture;                  // Now all the methods can access to this View
    private String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download"; // getExternalStorageDirectory does not refer to SD card! it refers to the root of the internal storage outside the app
    private Uri imageURI;                                        // Uri of the last captured image
    private String imageName = "";
    private File directory ;
    private File imageFile ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragment_view = inflater.inflate(R.layout.fragment_camera_time_save, container);     // to inflate fragment xml code

        dataInitialization ();

        // Button b = view.findViewById(); // to access fragments views, it is needed to use view previous object

       // All methods can access to imageView_survey_picture view
        imageView_survey_picture =  (ImageView) fragment_view.findViewById(R.id.imageView_survey_picture);

        // Calling camera from fragment
        fragment_view.findViewById(R.id.imageButton_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCamera ();
            }
        });

        return fragment_view;
    }


    // Method to store automatically the result when the camera native activity is called
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);      // data content the image in this case. This method wil receive a request code, in order to filter who has invoked it

        if (requestCode == REQUEST_CAMERA) {
            // We are hearing from the camera (this method only is valid if we do not specify the pat to store the image)
            //Bitmap cameraImage = (Bitmap) data.getExtras().get("data"); // accessing to the image
            //imageView_survey_picture.setImageBitmap(cameraImage);       // The image that has been captured will be shown in the fragment
            // Permission management for android 6.0: READ_STORAGE
            // ActivityCompat to be able to check permissions from a fragment
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageCaptured ();
            }
            else{
                //permission failed, request
                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions (permissionRequest, IMAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Method to handle run-time permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String imagePath = storagePath + "/" + imageName ;
        if (requestCode == IMAGE_PERMISSION_REQUEST_CODE) {

            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showImageCaptured ();
            }
            else {
                Toast.makeText(getActivity(), "PERMISSION DENIED: Can not save the image. ", Toast.LENGTH_LONG ).show();
            }

        }
    }

    // ========================================================================================================================================
    // NON-OVERRIDE METHODS
    // ========================================================================================================================================

    /**
     * To invoke the camera through intent
     */

    private void invokeCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // To specify a folder to store the images => putExtra
        //File directory = new File (storagePath);                    // Directory of the file
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);                    // Directory of the file
        imageName = getImageName ();                         // Name of the file
        imageFile = new File (directory, imageName);           // File
        imageURI = Uri.fromFile(imageFile);                     // Uri of the file. It is necessary for putExtra

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);   // Now we are telling that we want to store the image into this specific folder
        // startActivityForResult -> to get something in return (in this case a picture)
        startActivityForResult(cameraIntent,REQUEST_CAMERA);     // The value of the request code (REQUEST_CAMERA) is irrelevant, just must be unique

    }

    /**
     * To calculate image name
     * @return
     */

    private String getImageName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmss");
        String imageName = "img" + sdf.format(new Date()) +".jpg";
        return imageName;
    }

    /**
     *  To show the image in imageView. It is necessary to provide run-time permissions and manifest permissions for android 6.0
     */

    private void showImageCaptured() {

        String imagePath = storagePath + "/" + imageName ;
        Bitmap myImg = BitmapFactory.decodeFile(imagePath);
        imageView_survey_picture.setImageBitmap(rotateImage(myImg, 90));
        TextView textView_imageName =  (TextView) fragment_view.findViewById(R.id.textView_value_imgGalleryName);
        textView_imageName.setText(imageName);

    }

    private static Bitmap rotateImage(Bitmap src, float degree)
    {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }

    private void dataInitialization() {

        // DATE
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String date = sdf.format(new Date());
        TextView textView_date =  (TextView) fragment_view.findViewById(R.id.textView_value_date);
        textView_date.setText(date);

        // STARTING TIME
        sdf = new SimpleDateFormat("HH:mm:ss");
        String startingTime = sdf.format(new Date());
        TextView textView_startingTime =  (TextView) fragment_view.findViewById(R.id.textView_value_starting_time);
        textView_startingTime.setText(startingTime);

        // TASTING TIME
        String tastingTime = "00:00:00";
        TextView textView_tastingTime =  (TextView) fragment_view.findViewById(R.id.textView_value_tasting_time);
        textView_tastingTime.setText(tastingTime);

    }
}
