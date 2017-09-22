package meteor.asu.edu.speedytiltshift;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        TextView elapsed_time_tv;
        elapsed_time_tv = (TextView) findViewById(R.id.textView2);
        //elapsed_time_tv.append("working...");
        long current_time = System.currentTimeMillis();
        bmpOut = SpeedyTiltShift.tiltshift_java(bmpIn, 100, 400, 750, 1350, 0.5f, 0.5f);
        long elapsed_time = (System.currentTimeMillis() - current_time)/1000;
        String elapsed_time_string = Objects.toString(elapsed_time);
        elapsed_time_tv.append(elapsed_time_string+"s");
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

