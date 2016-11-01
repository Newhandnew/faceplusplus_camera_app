package hh.camera_photo_test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
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
    HttpRequests request = null;// 在线api
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectAPI();
                    }
                }).start();
                final Bitmap bit = getFaceInfoBitmap(faceinfo, photo);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        imageView.setImageBitmap(bit);
                        System.gc();
                    }
                });
            }
        }
    }

    private void connectAPI() {
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

    public static Bitmap getFaceInfoBitmap(Face[] faceinfos,
                                           Bitmap oribitmap) {
        Bitmap tmp;
        tmp = oribitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas localCanvas = new Canvas(tmp);
        Paint localPaint = new Paint();
        localPaint.setColor(0xff0000ff);
        localPaint.setStyle(Paint.Style.STROKE);
        for (Face localFaceInfo : faceinfos) {
            RectF rect = new RectF(oribitmap.getWidth() * localFaceInfo.left, oribitmap.getHeight()
                    * localFaceInfo.top, oribitmap.getWidth() * localFaceInfo.right,
                    oribitmap.getHeight()
                            * localFaceInfo.bottom);
            localCanvas.drawRect(rect, localPaint);
        }
        return tmp;
    }
}
