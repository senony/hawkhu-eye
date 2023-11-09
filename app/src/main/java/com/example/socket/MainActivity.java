package com.example.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socket.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.GzipSink;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap bmImg = null;
    ArrayList<String> ImgUrls = new ArrayList<>();
    ArrayList<String> dates = new ArrayList<>();
    CLoadImage task;

    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imgView);
        task = new CLoadImage();
    }

    public void onClickForLoad(View v)
    {
        task.execute();
        Toast.makeText(getApplicationContext(), "Waiting for bird...", Toast.LENGTH_LONG).show();

    }

    public void onClickForNext(View v)
    {
        Toast.makeText(getApplicationContext(), "Shooting ultrasonic waves!", Toast.LENGTH_LONG).show();
    }

    public void getImgUrl(ArrayList<String> arr) {
        try {
            String url = "https://seungho403.pythonanywhere.com/api_root/Post/";

            OkHttpClient client = new OkHttpClient();

            Request.Builder builder = new Request.Builder().url(url).get();
            Request req = builder.build();

            Response response = client.newCall(req).execute();
            if(response.isSuccessful()) {
                String userString = response.body().string();
                JSONArray BirdArray = new JSONArray(userString);
                for(int i = 0; i < BirdArray.length(); i++) {
                    if(BirdArray.getJSONObject(i).get("title").toString().equals("bird")) {
                        arr.add(BirdArray.getJSONObject(i).get("image").toString());
                        dates.add(BirdArray.getJSONObject(i).get("published_date").toString());
                    }
                }
                System.out.println(dates);
            }
            else
                System.out.println("Error Occurred!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savaBitmapToJpeg(Bitmap bitmap, String folder, String name) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder_name = "/" + folder + "/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + folder_name;
        Log.d("경로", string_path);

        File file_path;
        file_path = new File(string_path);

        if(!file_path.exists()) {
            file_path.mkdirs();
        }

        try {
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch(FileNotFoundException e) {
            Log.e("FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }
    }


    private class CLoadImage extends AsyncTask<String, Integer, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... urls) {
            task = new CLoadImage();
            try
            {
                getImgUrl(ImgUrls);

                URL myFileUrl = new URL(ImgUrls.get(count));
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();

                bmImg = BitmapFactory.decodeStream(is);
            } catch(IOException e)
            {
                e.printStackTrace();
            }
            return bmImg;
        }

        protected void onPostExecute(Bitmap img)
        {
            imageView.setImageBitmap(bmImg);
            TextView textView1 = (TextView) findViewById(R.id.text1) ;
            textView1.setText("Bird detected Date: " + dates.get(count++));
        }
    }
}