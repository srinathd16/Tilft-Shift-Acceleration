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

public class MainActivity extends AppCompatActivity {


    private Bitmap bmpIn;
    private Bitmap bmpOut;
    private ImageView imageView;
    private Button button;
    private Button button3;
    private Button button2;
    private Button button5;
    private Button button6;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = intent.getData();
                //Cursor returnCursor = getContentResolver().query(imageUri, null, null, null, null);
                //int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                //String fname = returnCursor.getString(nameIndex);
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);


                //Log.d("INPUT_FILE_PATH", "path= "+fname);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedImage,650,650,false);

                ///Following code assigns the image obtained from Gallery into "bmpIn"
                ///Comment the following lines, if using the below segment to load the image from /drawable
                ///Configuration of the following Bitmap object bmpIn is retained to be ARGB_8888
                bmpIn = selectedImage;
                imageView = (ImageView) findViewById(R.id.imageView);
                Log.d("BMPCONFIG", "bmpin.config: "+bmpIn.getConfig());

                ///Uncomment the following to load images from /drawable
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

    //This function is invoked upon the button click of ORIGINAL in the MainActivity
    public void displayoriginalimage(View view){
        //BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inPreferredConfig = Bitmap.Config.ARGB_8888; // Each pixel is 4 bytes: Alpha, Red, Green, Blue
        //bmpIn = BitmapFactory.decodeResource(getResources(), R.drawable.input, opts);
        //imageView = (ImageView) findViewById(R.id.imageView);
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

    public void loadimage(View view){
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

    public void saveimage(View view) throws IOException {
        Log.d("ExternalStorageWritable", ""+isExternalStorageWritable());
        //getAlbumStorageDir("TiltShift");
        //Disabling SAVE
        createOutputImage(bmpOut);
        button6 = (Button) findViewById(R.id.button6);
        button6.setAlpha(0.5f);
        button6.setClickable(false);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void createOutputImage(Bitmap image) throws IOException {
        File imageOut = getOutputImage();
        if (imageOut == null) {
            return;
        }
        FileOutputStream fo = new FileOutputStream(imageOut);
        image.compress(Bitmap.CompressFormat.PNG, 90, fo);
        fo.close();
        
    }

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
        return tempOut;
    }

    //This function is invoked upon the button click of TILT SHIFT (JAVA) in the MainActivity
    public void tiltshiftjava(View view){
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

    //This function is invoked upon the button click of TILT SHIFT (C++) in the MainActivity
    public void tiltshiftcpp(View view){
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

        //Calling tiltshift_java
        bmpOut = SpeedyTiltShift.tiltshift_cpp(bmpIn, 100, 150, 280, 370, 5.0f, 5.0f);

        //Computing elapsed time
        long elapsed_time = (System.currentTimeMillis() - current_time);
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_text.setText("C++ Elapsed Time: "+elapsed_time_string+"ms");

        //Pushing the output image into the ImageView object
        imageView.setImageBitmap(bmpOut);
    }

    //This function is invoked upon the button click of TILT SHIFT (NEON) in the MainActivity
    public void tiltshiftneon(View view){
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

