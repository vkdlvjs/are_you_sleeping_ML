package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.FaceDetector;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.BitmapUtil;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MyFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private Face mDFace;
    public static Bitmap tensorBit;

    public  List<Classifier.Recognition> results;

    private Classifier classifier;


    public MyFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
        mDFace = null;
    }

    @Override
    public SparseArray<Face> detect(Frame frame) {
        if(mDFace != null){
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            Bitmap TempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            Bitmap rotateBitmap = Bitmap.createBitmap(TempBitmap,0,0,TempBitmap.getWidth()-1,TempBitmap.getHeight()-1,matrix,false);
//                    matrix.setScale(-1,1); // 좌우반전
//                    Bitmap reverseBitmap = Bitmap.createBitmap(rotateBitmap,0,0,rotateBitmap.getWidth(),rotateBitmap.getHeight(),matrix,false);

            Log.d("Capture", "bitmap size : "+rotateBitmap.getWidth() + ","+ rotateBitmap.getHeight());
            Log.d("Capture", "face data : "+(int)mDFace.getPosition().x + ",  "+ (int)mDFace.getPosition().y+"    "+ (int)mDFace.getWidth()+"   "+ (int)mDFace.getHeight());

            // 사진 자르게 준비
            int partX = (int) (mDFace.getPosition().x * rotateBitmap.getWidth() / 480);
            int partY = (int) ((mDFace.getPosition().y + mDFace.getHeight() / 2.5) * rotateBitmap.getHeight() / 640);
            int partWidth = (int) (mDFace.getWidth() * rotateBitmap.getWidth() / 480);
            int partHeight = (int) (mDFace.getHeight() * rotateBitmap.getHeight() / 640 / 3.5 );
//            int partX = (int) (mDFace.getPosition().x * rotateBitmap.getWidth() / 480);
//            int partY = (int) (mDFace.getPosition().y * rotateBitmap.getHeight() / 640);
//            int partWidth = (int) (mDFace.getWidth() * rotateBitmap.getWidth() / 480);
//            int partHeight = (int) (mDFace.getHeight() * rotateBitmap.getHeight() / 640);
            if (partX>0 && partY>0 && partWidth + partX<rotateBitmap.getWidth() && partHeight+partY <rotateBitmap.getHeight()){
                Bitmap partBit = Bitmap.createBitmap(rotateBitmap, partX, partY, partWidth, partHeight);
                matrix.setScale(-1,1);
                Bitmap partRotate = Bitmap.createBitmap(partBit,0,0,partBit.getWidth(),partBit.getHeight(),matrix,false);
                classifier.setInput(partRotate.getWidth(), partRotate.getHeight());

                results = classifier.recognizeImage(partRotate);
                tensorBit = partBit;
            }

        }


        //TempBitmap is a Bitmap version of a frame which is currently captured by your CameraSource in real-time
        //So you can process this TempBitmap in your own purposes adding extra code here

        return mDelegate.detect(frame);
    }
    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public void setFace(Face mFace){
        mDFace = mFace;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    public List<Classifier.Recognition> getResults() {
        return results;
    }
}
