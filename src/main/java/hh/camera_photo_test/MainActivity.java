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
import android.widget.EditText;
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
    String faceID = null;
    FaceDetecter detecter = null;
    HttpRequests request = null;// 在线api
    EditText faceName;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        final Button addFace = (Button) this.findViewById(R.id.btn_add);
        Button recognitionFace = (Button) this.findViewById(R.id.btn_recognition);
        faceName = (EditText) this.findViewById(R.id.text_name);


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
        addFace.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectAddFaceAPI();
                    }
                }).start();

            }
        });
        recognitionFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectRecognitionAPI();
                    }
                }).start();
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
                        connectAttributeAPI();
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

    private void connectAttributeAPI() {
        //在线api交互
        try {
            JSONObject jsonObject = request.offlineDetect(detecter.getImageByteArray(),detecter.getResultJsonString(), new PostParameters());
            try {
                faceID = jsonObject.getJSONArray("face")
                        .getJSONObject(0)
                        .getString("face_id");
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
                        faceName.setText("");
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

    private void connectAddFaceAPI() {
        try {
//            request.groupCreate(new PostParameters().setGroupName("group_test"));
            request.personCreate(new PostParameters().setGroupName("group_test").setPersonName(faceName.getText().toString()).setFaceId(faceID));
            request.trainIdentify(new PostParameters().setGroupName("group_test"));
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "create person successfully", Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
        catch (FaceppParseException e) {
            // if add person fail, add face
            try {
                request.personAddFace(new PostParameters().setGroupName("group_test").setPersonName(faceName.getText().toString()).setFaceId(faceID));
                request.trainIdentify(new PostParameters().setGroupName("group_test"));
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "add face successfully", Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            catch (FaceppParseException err) {
                err.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void connectRecognitionAPI() {
        try {
            JSONObject jsonObject = request.recognitionIdentify(new PostParameters().setGroupName("group_test").setKeyFaceId(faceID));
            try {
                String matchedConfidence = jsonObject.getJSONArray("face")
                        .getJSONObject(0)
                        .getJSONArray("candidate")
                        .getJSONObject(0)
                        .getString("confidence");
                float fConfidence = Float.parseFloat(matchedConfidence);
                Log.d("matched confidence", "confidence: " + fConfidence);
                final String matchedName;
                int threshold = 20;
                if (fConfidence > threshold) {
                    matchedName = jsonObject.getJSONArray("face")
                            .getJSONObject(0)
                            .getJSONArray("candidate")
                            .getJSONObject(0)
                            .getString("person_name");
                }
                else {
                    matchedName = "No matched name";
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, matchedName, Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (FaceppParseException e) {
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
