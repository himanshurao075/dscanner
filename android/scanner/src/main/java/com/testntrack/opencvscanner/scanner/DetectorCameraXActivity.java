
package com.testntrack.opencvscanner.scanner;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;

import androidx.camera.core.ImageCapture;

import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.testntrack.opencvscanner.DocumentDetectorView;
import com.testntrack.opencvscanner.ScanActivity;
import com.testntrack.opencvscanner.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.core.Core;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.testntrack.opencvscanner.utils.ImageUtils;

public class DetectorCameraXActivity extends AppCompatActivity implements View.OnClickListener {


    private int REQUEST_CODE_PERMISSIONS = 101;
    private Size resolution = new Size(700, 1200);
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    PreviewView textureView;
    private View loadingView;
    DocumentDetectorView detectorView;
    LinearLayout llBottom;

    int currentImageType = Imgproc.COLOR_RGB2GRAY;

    ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;
    ImageAnalysis imageAnalysis;
    Preview preview;

    FloatingActionButton btnCapture;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector_camera_xactivity);


        btnCapture = findViewById(R.id.btnCapture);

        loadingView = findViewById(R.id.loading_layout);

        llBottom = findViewById(R.id.llBottom);
        textureView = findViewById(R.id.textureView);
        detectorView = findViewById(R.id.detectorOutput);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void stopCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderListenableFuture.addListener(((Runnable) () -> {
            try {
                cameraProvider = cameraProviderListenableFuture.get();

                cameraProvider.unbindAll();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), ContextCompat.getMainExecutor(this));

    }

    private void startCamera() {


        preview = setPreview();
        imageCapture = setImageCapture();
        imageAnalysis = setImageAnalysis();

        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderListenableFuture.addListener(((Runnable) () -> {
            try {
                cameraProvider = cameraProviderListenableFuture.get();

                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }), ContextCompat.getMainExecutor(this));

        //bind to lifecycle:

    }


    private Preview setPreview() {


//        int aspectRatio = (int)(textureView.getWidth()/textureView.getHeight());
//        resolution = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen

        Preview preview = new Preview.Builder().
//                setTargetResolution(resolution)
//               .setTargetAspectRatio(aspectRatio).
        build();

        preview.setSurfaceProvider(textureView.getSurfaceProvider());

//        preview.getResolutionInfo().getCropRect();


//        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(resolution).build();
//        Preview preview = new Preview(pConfig);

//        preview.setSurfaceProvider(P);

//        preview.setOnPreviewOutputUpdateListener(
//                new Preview.OnPreviewOutputUpdateListener() {
//                    @Override
//                    public void onUpdated(Preview.PreviewOutput output) {
//                        ViewGroup parent = (ViewGroup) textureView.getParent();
//                        parent.removeView(textureView);
//                        parent.addView(textureView, 0);
//
//                        textureView.setSurfaceTexture(output.getSurfaceTexture());
//                        updateTransform();
//                    }
//                });

        return preview;
    }


    private ImageCapture setImageCapture() {
//        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
//                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
//        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);


        btnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                showLoadingDialog();
                cropImage(new CropCallback() {
                    @Override
                    public void onComplete(Bitmap bmp) {
                        runOnUiThread(DetectorCameraXActivity.this::cancelLoadingDialog);
                    }

                    @Override
                    public void onError() {

                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Please wait for omr detection", Toast.LENGTH_SHORT).show();
                            startCamera();
                            cancelLoadingDialog();
                        });
                    }
                });


            }
        });

        return null;
    }


    Boolean isCropping = false;

    private ImageAnalysis setImageAnalysis() {

        // Setup image analysis pipeline that computes average pixel luminance
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();


        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        // enable the following line if RGBA output is needed.
                        //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                //Analyzing live camera feed begins.


                /// will convert it into future
//                ImageUtils.Companion.proxytoBitmap(imageProxy)
                final Bitmap bitmap = textureView.getBitmap();


                if (bitmap == null)
                    return;
                detectAndDrawContours(bitmap);

                getPoints();

                imageProxy.close();
            }
        });


        return imageAnalysis;

    }

//    private void showAcceptedRejectedButton(boolean acceptedRejected) {
//
//        if (acceptedRejected) {
//
//            llBottom.setVisibility(View.VISIBLE);
//            btnCapture.hide();
//            textureView.setVisibility(View.GONE);
//        } else {
//
//            isCropping = false;
//            btnCapture.show();
//            llBottom.setVisibility(View.GONE);
//            textureView.setVisibility(View.VISIBLE);
//            textureView.post(new Runnable() {
//                @Override
//                public void run() {
//                    startCamera();
//                }
//            });
//        }
//    }


//    private void updateTransform() {
//        Matrix mx = new Matrix();
//        float w = textureView.getMeasuredWidth();
//        float h = textureView.getMeasuredHeight();
//
//        float cX = w / 2f;
//        float cY = h / 2f;
//
//        int rotationDgr;
//        int rotation = (int) textureView.getRotation();
//
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                rotationDgr = 0;
//                break;
//            case Surface.ROTATION_90:
//                rotationDgr = 90;
//                break;
//            case Surface.ROTATION_180:
//                rotationDgr = 180;
//                break;
//            case Surface.ROTATION_270:
//                rotationDgr = 270;
//                break;
//            default:
//                return;
//        }
//
//        mx.postRotate((float) rotationDgr, cX, cY);
//        textureView.setTransform(mx);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onClick(View v) {
    }


    Boolean detecting = false;
    List<MatOfPoint> rectangles = new ArrayList<>();


    private Mat detectAndDrawContours(Bitmap bmp) {

        Mat rgb = new Mat();
        Mat bilateral = new Mat();
        Utils.bitmapToMat(bmp, rgb);


        if (detecting) {
            return rgb;
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                detecting = true;
                Mat dest = new Mat(rgb.width(), rgb.height(), CvType.CV_8UC3);

                try {
                    Imgproc.cvtColor(rgb, dest, Imgproc.COLOR_RGBA2BGR);
                    Imgproc.bilateralFilter(dest, bilateral, 11, 40, 50, Core.BORDER_DEFAULT);
                   Imgproc.cvtColor(bilateral, dest, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.Canny(dest, dest, 75, 200, 5);
//                    Imgproc.Canny(dest, dest, 75, 200, 5);
                    List<MatOfPoint> contours = new ArrayList<>();
                    Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                    MatOfPoint2f temp = findLargestContour(contours);
                    //checking for the document is same as before
                    if (temp != null) {
                        Mat cur = new Mat();
                        MatOfPoint lar = new MatOfPoint();
                        temp.convertTo(lar, CvType.CV_32S);
                        rectangles.clear();
                        rectangles.add(lar);
                        largest = temp;
                        rgb.copyTo(cur);
                        currentImage = cur;
                    }
                    detecting = false;
                } catch (Exception e) {
                    detecting = false;

//                    Log.v("mat type", e.toString());
                }
            }
        });
        return rgb;
    }


    private MatOfPoint2f findLargestContour(List<MatOfPoint> contours) {
        // Get the 5 largest contours

        if (contours.isEmpty()) {
            return null;
        }
        // Get the 5 largest contours
        Collections.sort(contours, (o1, o2) -> {
            double area1 = Imgproc.contourArea(o1);
            double area2 = Imgproc.contourArea(o2);
            return Double.compare(area2, area1);
        });
        if (contours.size() > 4) contours.subList(4, contours.size() - 1).clear();
//        MatOfPoint2f largest = null;
//        if(contours.size()>2)
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f c = new MatOfPoint2f();
            contour.convertTo(c, CvType.CV_32FC2);
            Imgproc.approxPolyDP(c, approx, Imgproc.arcLength(c, true) * 0.02, true);
            if (approx.total() == 4 && Imgproc.contourArea(approx, true) > 50) {
//                Log.d("area",String.valueOf(Imgproc.contourArea(approx)));
                // the contour has 4 points, it's valid
                largest = approx;
                break;
            }
        }

        return largest;
    }


    MatOfPoint2f largest;
    Mat currentImage;


    private void getPoints() {
        List<Point> cropPoints = new ArrayList<Point>();
        runOnUiThread(()->{ detectorView.setPoints(cropPoints);});
        try {
            isCropping = true;
            org.opencv.core.Size imageSize = new org.opencv.core.Size(1240, 1754);
            if (currentImage == null) {
                return;
            }
            Moments moment = Imgproc.moments(largest);
            int x = (int) (moment.get_m10() / moment.get_m00());
            int y = (int) (moment.get_m01() / moment.get_m00());
//SORT POINTS RELATIVE TO CENTER OF MASS
            Point[] sortedPoints = new Point[4];
            double[] data;
//        int count = 0;
            for (int i = 0; i < largest.rows(); i++) {
                data = largest.get(i, 0);
                double datax = data[0];
                double datay = data[1];
                if (datax < x && datay < y) {
                    sortedPoints[0] = new Point(datax, datay);
//                count++;
                } else if (datax > x && datay < y) {
                    sortedPoints[1] = new Point(datax, datay);
//                count++;
                } else if (datax < x && datay > y) {
                    sortedPoints[2] = new Point(datax, datay);
//                count++;
                } else if (datax > x && datay > y) {
                    sortedPoints[3] = new Point(datax, datay);
//                count++;
                }
            }

            if (sortedPoints[0] != null && sortedPoints[1] != null && sortedPoints[2] != null && sortedPoints[3] != null) {

                cropPoints.add(sortedPoints[0]);
                cropPoints.add(sortedPoints[1]);
                cropPoints.add(sortedPoints[2]);
                cropPoints.add(sortedPoints[3]);


                runOnUiThread(()->{detectorView.setPoints(cropPoints);});
            }
        } catch (Exception e) {

            Log.d("cropPoints", "" + cropPoints);
        }

    }


    private void showLoadingDialog() {


        stopCamera();
        loadingView.setVisibility(View.VISIBLE);

    }

    private void cancelLoadingDialog() {

        loadingView.setVisibility(View.GONE);
    }

    private void cropImage(CropCallback callback) {
        isCropping = true;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                org.opencv.core.Size imageSize = new org.opencv.core.Size(1240, 1754);
                if (currentImage == null || largest == null) {

                    callback.onError();
                }
                Moments moment = Imgproc.moments(largest);
                int x = (int) (moment.get_m10() / moment.get_m00());
                int y = (int) (moment.get_m01() / moment.get_m00());
//SORT POINTS RELATIVE TO CENTER OF MASS
                Point[] sortedPoints = new Point[4];
                double[] data;
//        int count = 0;
                for (int i = 0; i < largest.rows(); i++) {
                    data = largest.get(i, 0);
                    double datax = data[0];
                    double datay = data[1];
                    if (datax < x && datay < y) {
                        sortedPoints[0] = new Point(datax, datay);
//                count++;
                    } else if (datax > x && datay < y) {
                        sortedPoints[1] = new Point(datax, datay);
//                count++;
                    } else if (datax < x && datay > y) {
                        sortedPoints[2] = new Point(datax, datay);
//                count++;
                    } else if (datax > x && datay > y) {
                        sortedPoints[3] = new Point(datax, datay);
//                count++;
                    }
                }
                MatOfPoint2f src = new MatOfPoint2f(
                        sortedPoints[0],
                        sortedPoints[1],
                        sortedPoints[2],
                        sortedPoints[3]);
//        Log.v("drc [oints", src.toArray().toString() + "");
                int lineWidth = 0;
//                src.adjustROI(lineWidth, lineWidth, lineWidth, lineWidth);
                MatOfPoint2f dst = new MatOfPoint2f(
                        new Point(0, 0),
                        new Point((int) imageSize.width, 0),
                        new Point(0, (int) imageSize.height),
                        new Point((int) imageSize.width, (int) imageSize.height)
                );
                Mat warpMat = Imgproc.getPerspectiveTransform(src, dst);
                //This is your new image as Mat
                Mat destImage = new Mat();
                // Imgproc.cvtColor(currentImage,destImage,Imgproc.COLOR_RGBA2GRAY);
                Imgproc.warpPerspective(currentImage, destImage, warpMat, imageSize);
                Bitmap bmp = null;
                Mat tmp = new Mat((int) imageSize.height, (int) imageSize.width, CvType.CV_8U, new Scalar(4));
                destImage.copyTo(tmp);
                bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);

                Utils.matToBitmap(tmp, bmp);


                Bitmap currentBitmap = Bitmap.createBitmap(currentImage.cols(), currentImage.rows(), Bitmap.Config.ARGB_8888);

                if(!rectangles.isEmpty())
                Imgproc.drawContours(currentImage, rectangles, -1, new Scalar(255, 0, 162), 4);
                Utils.matToBitmap(currentImage, currentBitmap);
                Utils.matToBitmap(tmp, bmp);

//        ivBitmap.setImageBitmap(bmp);

                ArrayList<String> images = new ArrayList<>();

                images.add(saveImage(currentBitmap));
                images.add(saveImage(bmp));

                shareImageUri(images);

                callback.onComplete(bmp);


            } catch (Exception e) {
                callback.onError();
            }
        });


    }

    /**
     * Saves the image as PNG to the app's cache directory.
     *
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    private String saveImage(Bitmap image) {
        //TODO - Should be processed in another thread
        File imagesFolder = new File(getCacheDir(), "images");
        String filePath = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            filePath = file.getAbsolutePath();
//            uri = FileProvider.getUriForFile(this, "com.testntrack.opencvscanner.fileprovider", file);

        } catch (IOException e) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return filePath;
    }

    /**
     * Shares the PNG image from Uri.
     *
     * @param uri Uri of image to share.
     */
    private void shareImageUri(ArrayList<String> uri) {

        Intent intent = new Intent();
//        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putStringArrayListExtra(ScanActivity.Companion.getFILE_URI(), uri);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setType("image/png");

        setResult(RESULT_OK, intent);
        finish();

//        startActivity(intent);
    }



}

interface CropCallback {
    void onComplete(Bitmap bmp);

    void onError();
}
