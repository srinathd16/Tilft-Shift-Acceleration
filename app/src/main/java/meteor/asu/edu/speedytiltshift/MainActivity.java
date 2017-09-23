package meteor.asu.edu.speedytiltshift;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private Bitmap bmpIn;
    private Bitmap bmpOut;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888; // Each pixel is 4 bytes: Alpha, Red, Green, Blue
        bmpIn = BitmapFactory.decodeResource(getResources(), R.drawable.input, opts);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void tiltshiftjava(View view){
        //Defining a TextView object and capturing current time to calculate elapsed time
        TextView elapsed_time_text;
        elapsed_time_text = (TextView) findViewById(R.id.textView2);
        long current_time = System.currentTimeMillis();

        //Calling tiltshift_java
        bmpOut = SpeedyTiltShift.tiltshift_java(bmpIn, 100, 400, 750, 1350, 5.0f, 5.0f);

        //Computing elapsed time
        long elapsed_time = (System.currentTimeMillis() - current_time)/1000;
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_text.append(elapsed_time_string+"s");

        //Pushing the output image into the ImageView object
        imageView.setImageBitmap(bmpOut);
        Log.d("TILTSHIFT_JAVA","time:"+elapsed_time);
    }
    public void tiltshiftcpp(View view){
        bmpOut = SpeedyTiltShift.tiltshift_cpp(bmpIn, 100, 200, 300, 400, 0.5f, 2.1f);
        imageView.setImageBitmap(bmpOut);
    }
    public void tiltshiftneon(View view){
        bmpOut = SpeedyTiltShift.tiltshift_neon(bmpIn, 100, 200, 300, 400, 0.5f, 2.1f);
        imageView.setImageBitmap(bmpOut);
    }
}

