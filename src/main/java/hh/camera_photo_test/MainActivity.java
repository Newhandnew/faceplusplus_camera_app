package hh.camera_photo_test;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int CAMERA_REQUEST = 1888;
    ImageView imageView;
    FaceDetecter detecter = null;
//    HandlerThread detectThread = null;
//    Handler detectHandler = null;
    HttpRequests request = null;// 在线api
    private Bitmap curBitmap;
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
//        detectThread = new HandlerThread("detect");
//        detectThread.start();
//        detectHandler = new Handler(detectThread.getLooper());

        detecter = new FaceDetecter();
        detecter.init(this, "af3f15e56f64b3ac869d93f15e71db93");

        //FIXME 替换成申请的key
        request = new HttpRequests("af3f15e56f64b3ac869d93f15e71db93",
                "7vXdEeWsOdZbLOJdVdl1JKmh6BPgjMNC");

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            final Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

//            final String str;
//            Uri localUri = data.getData();
//            String[] arrayOfString = new String[1];
//            arrayOfString[0] = "_data";
//            Cursor localCursor = getContentResolver().query(localUri,
//                    arrayOfString, null, null, null);
//            if (localCursor == null)
//                return;
//            localCursor.moveToFirst();
//            str = localCursor.getString(localCursor
//                    .getColumnIndex(arrayOfString[0]));
//            localCursor.close();
//            curBitmap = getScaledBitmap(str, 600);
//            detectHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
                    Face[] faceinfo = detecter.findFaces(photo);// 进行人脸检测
                    if (faceinfo == null) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "未发现人脸信息", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                        return;
                    } else {
                        connect();
//                        new Connection().execute();
                    }
//                }
//            });
        }
    }

//    private class Connection extends AsyncTask {
//
//        @Override
//        protected Object doInBackground(Object... arg0) {
//            connect();
//            return null;
//        }
//
//    }

    private void connect() {
        //在线api交互
        try {
            JSONObject jsonObject = request.offlineDetect(detecter.getImageByteArray(),detecter.getResultJsonString(), new PostParameters());
            try {
                final int age = jsonObject.getJSONArray("face")
                        .getJSONObject(0)
                        .getJSONObject("attribute")
                        .getJSONObject("age").getInt("value");
                Log.d("age", "age: " + age);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "age: " + age, Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FaceppParseException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

//    public static Bitmap getScaledBitmap(String fileName, int dstWidth)
//    {
//        BitmapFactory.Options localOptions = new BitmapFactory.Options();
//        localOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(fileName, localOptions);
//        int originWidth = localOptions.outWidth;
//        int originHeight = localOptions.outHeight;
//
//        localOptions.inSampleSize = originWidth > originHeight ? originWidth / dstWidth
//                : originHeight / dstWidth;
//        localOptions.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(fileName, localOptions);
//    }

}
