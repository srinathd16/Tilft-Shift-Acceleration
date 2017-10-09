package meteor.asu.edu.speedytiltshift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**This file in top of the project hierarchy
 * Includes the class MainActivity containing calls to overridden functions, to on-click methods, and to Speedy Tilt Shift  methods
 *
 */

/**MainActivity class is the interface for the first and only interface of the SpeedyTiltShift application
 * It contains calls to overridden methods,
 *              -onCreate - invoked every time the application is launched
 *              -onActivityResult - invoked after an input image has been selected (by clicking the button LOAD)
 * Calls to methods that are required to execute upon a button click are also included,
 *              -displayOriginalImage - invoked upon clicking ORIGINAL button
 *              -loadImage - invoked upon clicking LOAD button
 *              -saveImage - invoked upon clicking SAVE button
 *                           This method calls a few other methods, needed to complete saving the output image into the device.
 *                           The output image gets saved in the application directory, typically at,
 *                             /storage/emulated/0/Android/data/meteor.asu.edu.speedytiltshift/Files.
 *                  -isExternalStorageWritable - checks if permissions are available to write to external storage.
 *                  -createOutputImage - creates a format for output image and sets up file streaming to it.
 *                          This method calls getOutputImage.
 *                          getOutputImage - provides a name to the output image file and saves it on external storage.
 */
public class MainActivity extends AppCompatActivity {

    /**Declaring global objects
     * "bmpIn" and "bmpOut" will be required throughout the class to perform operations on input and output images
     * "imageView" object gives access to the area of the app as displayed on the screen that display the image
     * All Button objects are made global to provide ease of access to activation and deactivation of the buttons from each method
     * */
    private Bitmap bmpIn;
    private Bitmap bmpOut;
    private ImageView imageView;
    private Button button;
    private Button button3;
    private Button button2;
    private Button button5;
    private Button button6;

    /**This method is called every time the application is launched
     * Layout of the activity as defined in activity_main.xml is loaded here; imageView being a part of it
     * ORIGINAL buttons, all the TILT SHIFT buttons, and the SAVE button are disabled, as they do not have any
     *  meaningful functionality at this point.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        //Disabling ORIGINAL
        button6 = (Button) findViewById(R.id.button);
        button6.setAlpha(0.5f);
        button6.setClickable(false);

        //Disabling TILT SHIFT (JAVA)
        button3 = (Button) findViewById(R.id.button3);
        button3.setAlpha(0.5f);
        button3.setClickable(false);
        //Disabling TILT SHIFT (C++)
        button2 = (Button) findViewById(R.id.button2);
        button2.setAlpha(0.5f);
        button2.setClickable(false);
        //Disabling TILT SHIFT (NEON)
        button5 = (Button) findViewById(R.id.button5);
        button5.setAlpha(0.5f);
        button5.setClickable(false);

        //Disabling SAVE
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(0.5f);
        button6.setClickable(false);
    }

    /**This method is called every time an image is loaded into the imageView using the LOAD button
     * It is precisely invoked after the image is selected and the context switches back to the main activity of the application
     * Logic to convert the loaded image into a Bitmap object is also handled here
     *      A Uri object is created to get the image from the Intent object and is then fed into the InputStream object
     *      A Bitmap object is finally created by using decodeStream (a method of BitmapFactory) from "imageStream"
     *      This Bitmap object is displayed on the screen by using the setImageBitmap function of "imageView"
     *      Logic to scale the input image is also included in this method, using createScaledBitmap function of Bitmap
     *      This scaled bitmap is assigned to the object ""bmpIn", which is later used by all the Tilt Shift algorithms
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = intent.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedImage,650,650,false);

                /**Following code assigns the image obtained from Gallery into "bmpIn"
                 * Comment the following lines, if using the below segment to load the image from /drawable
                 * Configuration of the following Bitmap object bmpIn is retained to be ARGB_8888
                 * uncomment the selectedImage option if you don't wish to scale the image
                 */
                bmpIn = scaledBitmap;
                //bmpIn = selectedImage;
                imageView = (ImageView) findViewById(R.id.imageView);
                Log.d("BMPCONFIG", "bmpin.config: "+bmpIn.getConfig());

                /**Uncomment the following to load images from /drawable*/
                //BitmapFactory.Options opts = new BitmapFactory.Options();
                //opts.inPreferredConfig = Bitmap.Config.ARGB_8888; // Each pixel is 4 bytes: Alpha, Red, Green, Blue
                //bmpIn = BitmapFactory.decodeResource(getResources(), R.drawable.input, opts);
                //imageView = (ImageView) findViewById(R.id.imageView);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed!", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "Image not found",Toast.LENGTH_LONG).show();
        }
    }

    /**This function is invoked upon clicking ORIGINAL in the MainActivity
     * The image selected after clicking LOAD and after it has been scaled in onActivityResult is displayed here using imageView
     * After the click, the button is disabled
     * Also, all TILT SHIFT buttons are enabled at this point
     **/
    public void displayOriginalImage(View view){
        ///And when clicked, the input image is displayed
        imageView.setImageBitmap(bmpIn);
        //Greying out ORIGINAL button after click
        button = (Button) findViewById(R.id.button);
        button.setAlpha(0.5f);
        button.setClickable(false);

        //Getting back TILT SHIFT (JAVA) button live
        button3 = (Button) findViewById(R.id.button3);
        if(button3.getAlpha()==0.5f){
            button3.setAlpha(1.0f);
            button3.setClickable(true);
        }
        //Getting back TILT SHIFT (C++) button live
        button2 = (Button) findViewById(R.id.button2);
        if(button2.getAlpha()==0.5f){
            button2.setAlpha(1.0f);
            button2.setClickable(true);
        }
        //Getting back TILT SHIFT (NEON) button live
        button5 = (Button) findViewById(R.id.button5);
        if(button5.getAlpha()==0.5f){
            button5.setAlpha(1.0f);
            button5.setClickable(true);
        }
    }

    /**This function is invoked upon clicking the LOAD button
     * An Intent object "galleryIntent" is created to handle the process of selecting (pick) an image
     * ALL TILT SHIFT buttons are enabled at this point
     * */
    public void loadImage(View view){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1);

        //Getting back TILT SHIFT (JAVA) button live
        button3 = (Button) findViewById(R.id.button3);
        if(button3.getAlpha()==0.5f){
            button3.setAlpha(1.0f);
            button3.setClickable(true);
        }
        //Getting back TILT SHIFT (C++) button live
        button2 = (Button) findViewById(R.id.button2);
        if(button2.getAlpha()==0.5f){
            button2.setAlpha(1.0f);
            button2.setClickable(true);
        }
        //Getting back TILT SHIFT (NEON) button live
        button5 = (Button) findViewById(R.id.button5);
        if(button5.getAlpha()==0.5f){
            button5.setAlpha(1.0f);
            button5.setClickable(true);
        }
    }

    /**This method is invoked upon clicking the SAVE button
     * The isExternalStorageWritable method is called
     * "createOutputImage" method is called from here and "bmpOut" is passed along
     * SAVE button is disabled after it as been clicked to avoid duplicate saving of the image
     * */
    public void saveImage(View view) throws IOException {
        Log.d("ExternalStorageWritable", ""+isExternalStorageWritable());
        //getAlbumStorageDir("TiltShift");
        //Disabling SAVE
        createOutputImage(bmpOut);
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(0.5f);
        button6.setClickable(false);
    }

    /**This method is called by saveImage
     * It checks if permissions have been granted to write onto external storage by
     *   checking if the value of getExternalStorageState method is true
     * */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**This method is called by saveImage
     * A File object is created by calling the getOutputImage method
     * The obtained image in the imageOut object is compressed to JPEG format from Bitmap
     * */
    private void createOutputImage(Bitmap image) throws IOException {
        File imageOut = getOutputImage();
        if (imageOut == null) {
            return;
        }
        FileOutputStream fo = new FileOutputStream(imageOut);
        image.compress(Bitmap.CompressFormat.JPEG, 90, fo);
        fo.close();

    }

    /**This method is called by createOutputImage
     * Directory where the image has to be saved, which is hardcoded here to be the application directory,
     *      is bought into a File object outImageDir
     *      - Typical path is: /storage/emulated/0/Android/data/meteor.asu.edu.speedytiltshift/Files
     * Also to avoid mismatch in the names of the output images, timestamp is appended to the name
     * */
    private  File getOutputImage(){
        File outImageDir = new File(Environment.getExternalStorageDirectory()+ "/Android/data/"+ getApplicationContext().getPackageName()+ "/Files");
        if (! outImageDir.exists()){
            if (! outImageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File tempOut;
        String outImageName="Blur"+ timeStamp +".jpg";
        tempOut = new File(outImageDir.getPath() + File.separator + outImageName);
        Log.d("FILEPATH","outputimagepath: "+outImageDir.getPath());
        return tempOut;
    }

    /**This method is invoked upon clicking the TILT SHIFT (JAVA) button in the MainActivity
     * Before calling the "SpeedyTiltShift.tiltshift_java" method, TILTSHIFT_JAVA is disabled (after the click),
     *      ORIGINAL and SAVE buttons are enabled
     * The method "tiltshift_java" defined in SpeedTiltShift.java is invoked, which implements the Tilt Shift algorithm in Java
     *      and returns a Bitmap object (containing the blurred image) which is captured in the object "bmpOut"
     * Logic to compute the time taken for the "tiltshift_java" method to execute is included in this method, using the method
     *      "System.currentTimeMillis()". This time is displayed using a TextView
     * */
    public void tiltShiftJava(View view){
        //Greying out TILT SHIFT (JAVA) button after click
        button3 = (Button) findViewById(R.id.button3);
        button3.setAlpha(0.5f);
        button3.setClickable(false);
        //Getting back ORIGINAL button live
        button = (Button) findViewById(R.id.button);
        if(button.getAlpha()==0.5f){
            button.setAlpha(1.0f);
            button.setClickable(true);
        }
        //Getting back SAVE button live
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(1f);
        button6.setClickable(true);
        //Defining a TextView object and capturing current time to calculate elapsed time
        TextView elapsed_time_text;
        elapsed_time_text = (TextView) findViewById(R.id.textView2);
        long current_time = System.currentTimeMillis();

        //Calling tiltshift_java
        bmpOut = SpeedyTiltShift.tiltshift_java(bmpIn, 100, 150, 280, 370, 5.0f, 5.0f);

        //Computing elapsed time
        long elapsed_time = (System.currentTimeMillis() - current_time);
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_text.setText("Java Elapsed Time: "+elapsed_time_string+"ms");

        //Pushing the output image into the ImageView object
        imageView.setImageBitmap(bmpOut);
        Log.d("TILTSHIFT_JAVA","time:"+elapsed_time);
    }

    /**This method is invoked upon clicking the TILT SHIFT (C++) button in the MainActivity
     * Before calling the "SpeedyTiltShift.tiltshift_cpp" method, TILT SHIFT (C++) button is disabled (after the click),
     *      ORIGINAL and SAVE buttons are enabled
     * The method "tiltshift_cpp" defined in SpeedTiltShift.java is invoked, which implements the Tilt Shift algorithm in C++
     *      and returns a Bitmap object (containing the blurred image) which is captured in the object "bmpOut"
     * Logic to compute the time taken for the "tiltshift_cpp" method to execute is included in this method, using the method
     *      "System.currentTimeMillis()". This time is displayed using a TextView
     * */
    public void tiltShiftCpp(View view){
        //Greying out TILT SHIFT (C++) button after click
        button2 = (Button) findViewById(R.id.button2);
        button2.setAlpha(0.5f);
        button2.setClickable(false);
        //Getting back ORIGINAL button live
        button = (Button) findViewById(R.id.button);
        if(button.getAlpha()==0.5f){
            button.setAlpha(1.0f);
            button.setClickable(true);
        }
        //Getting back SAVE button live
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(1f);
        button6.setClickable(true);
        //Defining a TextView object and capturing current time to calculate elapsed time
        TextView elapsed_time_text;
        elapsed_time_text = (TextView) findViewById(R.id.textView3);
        long current_time = System.currentTimeMillis();

        //Calling tiltshift_cpp
        bmpOut = SpeedyTiltShift.tiltshift_cpp(bmpIn, 100, 150, 280, 370, 5.0f, 5.0f);

        //Computing elapsed time
        long elapsed_time = (System.currentTimeMillis() - current_time);
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_text.setText("C++ Elapsed Time: "+elapsed_time_string+"ms");

        //Pushing the output image into the ImageView object
        imageView.setImageBitmap(bmpOut);
    }

    /**This method is invoked upon clicking the TILT SHIFT (NEON) button in the MainActivity
     * Before calling the "SpeedyTiltShift.tiltshift_neon" method, TILT SHIFT (NEON) button is disabled (after the click),
     *      ORIGINAL and SAVE buttons are enabled
     * The method "tiltshift_neon" defined in SpeedTiltShift.java is invoked, which implements the Tilt Shift algorithm in NEON
     *      and returns a Bitmap object (containing the blurred image) which is captured in the object "bmpOut"
     * Logic to compute the time taken for the "tiltshift_neon" method to execute is included in this method, using the method
     *      "System.currentTimeMillis()". This time is displayed using a TextView
     * */
    public void tiltShiftNeon(View view){
        //Greying out TILT SHIFT (NEON) button after click
        button5 = (Button) findViewById(R.id.button5);
        button5.setAlpha(0.5f);
        button5.setClickable(false);
        //Getting back ORIGINAL button live
        button = (Button) findViewById(R.id.button);
        if(button.getAlpha()==0.5f){
            button.setAlpha(1.0f);
            button.setClickable(true);
        }
        //Getting back SAVE button live
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(1f);
        button6.setClickable(true);
        //Defining a TextView object and capturing current time to calculate elapsed time
        TextView elapsed_time_text;
        elapsed_time_text = (TextView) findViewById(R.id.textView);
        long current_time = System.currentTimeMillis();

        //Calling tiltshift_neon
        bmpOut = SpeedyTiltShift.tiltshift_neon(bmpIn, 100, 150, 280, 370, 5.0f, 5.0f);

        //Computing elapsed time
        long elapsed_time = (System.currentTimeMillis() - current_time);
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_text.setText("Neon Elapsed Time: "+elapsed_time_string+"ms");

        //Pushing the output image into the ImageView object
        imageView.setImageBitmap(bmpOut);
    }
}

