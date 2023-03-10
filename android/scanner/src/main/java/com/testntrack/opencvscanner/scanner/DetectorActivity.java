package com.testntrack.opencvscanner.scanner;

import org.opencv.android.BaseLoaderCallback;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.core.content.FileProvider;

import com.testntrack.opencvscanner.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetectorActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private Button cropButton;

    private ImageView imageView;

    private SeekBar seekBar;

    private Mat inputGray;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public DetectorActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);


        cropButton = findViewById(R.id.crop_button);

        imageView = findViewById(R.id.imageView);
//        seekBar = findViewById(R.id.seekBar);


        cropButton.setOnClickListener(this::cropButtonListener);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);


//        mOpenCvCameraView.setRotation(90);

//        mOpenCvCameraView.setRotation(90);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {

        if (inputGray != null)
            inputGray.release();
    }


    Boolean isCropping = false;
    MatOfPoint2f largest;
    int findCount = 0;
    Mat currentImage;
    List<MatOfPoint> rectangles = new ArrayList<MatOfPoint>();

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {


        Mat inputGray = inputFrame.rgba();
//        Mat mRgbaT = inputGray.t();
//        Core.flip(inputGray.t(), mRgbaT, 1);
//        Imgproc.resize(mRgbaT, mRgbaT, inputGray.size());
//

        currentImage = inputFrame.rgba();

        if (isCropping) {
            return inputGray;
        }

        Size sizeRgba = inputGray.size();
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = 0;
        int top = 0;

        int width = cols;
        int height = rows;

        Mat dest = new Mat();


        Mat input = inputGray.submat(top, top + height, left, left + width);
        List<MatOfPoint> points = new ArrayList<MatOfPoint>();


        Imgproc.cvtColor(input, dest, Imgproc.COLOR_BGRA2GRAY);


        Imgproc.medianBlur(dest, input, 5);

//        Imgproc.blur(input,input,new Size(5,5));

        Imgproc.Canny(input, dest, 75, 200, 3);


        try {

            MatOfPoint2f temp = findLargestContour(dest);

            //checking for the document is same as before
            if (temp != null) {

                if (largest != null && largest.size() == temp.size()) {
                    hideButton();
                    findCount++;


                } else {
                    findCount = 0;
                    largest = temp;
                    MatOfPoint lar = new MatOfPoint();
                    largest.convertTo(lar, CvType.CV_32S);
                    rectangles.clear();
                    rectangles.add(lar);


                    // Log.v(TAG, String.valueOf(largest.size()));

                    Imgproc.drawContours(inputGray, rectangles, -1, new Scalar(0, 255, 0), 2);

                }


                //if (findCount > 1) {
                showButton();
                // }


            } else {
//                largest=null;
//                currentImage = inputFrame.rgba();
//
            }

            Log.v(TAG, "largest contour" + largest);

        } catch (Exception e) {


            Log.v(TAG, "EXCEPTION IN FINDING LARGEST CONTOUR");
            Log.v(TAG, e.getMessage());
        }


        return inputGray;


        //return inputFrame.gray();
    }


    private void cropButtonListener(View v) {
        isCropping = true;
        Size imageSize = new Size(1200, 1600);

        if (currentImage == null) {
            return;
        }

        ;

//        Mat dst = Converters.vector_Point2f_to_Mat(largest.toList());
//        Mat dst = new Mat();
//        largest.convertTo(dst,CvType.CV_32F);
//        currentImage.convertTo(currentImage,CvType.CV_32F);
        //  Mat src = Converters.vector_Point2f_to_Mat(currentImage.checkVector()=);

        Moments moment = Imgproc.moments(largest);
        int x = (int) (moment.get_m10() / moment.get_m00());
        int y = (int) (moment.get_m01() / moment.get_m00());


//SORT POINTS RELATIVE TO CENTER OF MASS
        Point[] sortedPoints = new Point[4];

        double[] data;
        int count = 0;
        for (int i = 0; i < largest.rows(); i++) {
            data = largest.get(i, 0);
            double datax = data[0];
            double datay = data[1];
            if (datax < x && datay < y) {
                sortedPoints[0] = new Point(datax, datay);
                count++;
            } else if (datax > x && datay < y) {
                sortedPoints[1] = new Point(datax, datay);
                count++;
            } else if (datax < x && datay > y) {
                sortedPoints[2] = new Point(datax, datay);
                count++;
            } else if (datax > x && datay > y) {
                sortedPoints[3] = new Point(datax, datay);
                count++;
            }
        }
        MatOfPoint2f src = new MatOfPoint2f(
                sortedPoints[0],
                sortedPoints[1],
                sortedPoints[2],
                sortedPoints[3]);

        Log.v("drc [oints", src.toArray().toString() + "");

        int lineWidth = 100;
        src.adjustROI(lineWidth, lineWidth, lineWidth, lineWidth);

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point((int) imageSize.width, 0),
                new Point(0, (int) imageSize.height),
                new Point((int) imageSize.width, (int) imageSize.height)
        );
        Mat warpMat = Imgproc.getPerspectiveTransform(src, dst);
        //This is you new image as Mat
        Mat destImage = new Mat();
        // Imgproc.cvtColor(currentImage,destImage,Imgproc.COLOR_RGBA2GRAY);
        Imgproc.warpPerspective(currentImage, destImage, warpMat, imageSize);
        Bitmap bmp = null;
        Mat tmp = new Mat((int) imageSize.height, (int) imageSize.width, CvType.CV_8U, new Scalar(4));
        destImage.copyTo(tmp);

        //   Point center = new Point((int) ((tmp.cols() - 1) / 2), (int) ((tmp.rows() - 1) / 2));
        //    Mat rot = Imgproc.getRotationMatrix2D(center, -90, 1);
        //  Imgproc.warpAffine(destImage, destImage, rot, new Size(tmp.cols(),tmp.rows()));
        try {

            //  Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGBA2RGB);

//            Imgproc.adaptiveThreshold(destImage, destImage, 200, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
            //    Imgproc.cvtColor(destImage, tmp, Imgproc.COLOR_GRAY2RGBA, 3);
//
            bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmp, bmp);


            /// used to rotate bitmap with an angle
            Bitmap scaleBitmap = rotateImage(bmp, 90f);

            imageView.setImageBitmap(scaleBitmap);
            imageView.setVisibility(View.VISIBLE);
            shareImageUri(saveImage(scaleBitmap));


//             seekBar.setVisibility(View.VISIBLE);
//
//            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    imageView.setImageBitmap(applyMagicFilter(scaleBitmap,(int)((progress*255)/100)));
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//
//                }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//
//                }
//            });

        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }


        imageView.setOnClickListener(v1 -> {
            imageView.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            isCropping = false;
        });


    }

    private Bitmap applyMagicFilter(Bitmap input, double threshold) {

        Mat inputMat = new Mat();

        Utils.bitmapToMat(input, inputMat);

        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGRA2GRAY);

        Imgproc.medianBlur(inputMat, inputMat, 3);

        Imgproc.adaptiveThreshold(inputMat, inputMat, threshold, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);


        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_GRAY2RGB, 4);
//
        Bitmap output = Bitmap.createBitmap(inputMat.cols(), inputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputMat, output);
        return output;

    }

    private Bitmap rotateImage(Bitmap input, float angle) {

        Matrix rotate = new Matrix();
        rotate.setRotate(angle);
        Bitmap rBitmap = Bitmap.createBitmap(input, 0, 0, input.getWidth(), input.getHeight(), rotate, false);
        Bitmap output = Bitmap.createScaledBitmap(rBitmap, rBitmap.getHeight(), rBitmap.getWidth(), false);

        return output;

    }

//    public Bitmap addrizzone(Bitmap image){
//        // sourcePoints are  expected  to be clockwise ordered
//        // [top_left,top_right,bottom_right,bottom_left]
//        // getting the size of the output image
//        double dst_width = Math.max(sourcePoints.get(0).distanceFrom(sourcePoints.get(1)),sourcePoints.get(3).distanceFrom(sourcePoints.get(2)));
//        double dst_height = Math.max(sourcePoints.get(0).distanceFrom(sourcePoints.get(3)),sourcePoints.get(1).distanceFrom(sourcePoints.get(2)));
//
//        //determining point sets to get the transformation matrix
//        List<org.opencv.core.Point> srcPts = new ArrayList<org.opencv.core.Point>();
//
//
//        List<org.opencv.core.Point> dstPoints= new ArrayList<org.opencv.core.Point>();
//        dstPoints.add(new org.opencv.core.Point(0,0));
//        dstPoints.add(new org.opencv.core.Point(dst_width-1,0));
//        dstPoints.add(new org.opencv.core.Point(dst_width-1,dst_height-1));
//        dstPoints.add(new org.opencv.core.Point(0,dst_height));
//
//        Mat srcMat = Converters.vector_Point2f_to_Mat(srcPts);
//        Mat dstMat = Converters.vector_Point2f_to_Mat(dstPoints);
//
//        //getting the transformation matrix
//        Mat perspectiveTransformation = Imgproc.getPerspectiveTransform(srcMat,dstMat);
//
//        //getting the input matrix from the given bitmap
//        Mat inputMat = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
//
//        Utils.bitmapToMat(image,inputMat);
//
//        Imgproc.cvtColor(inputMat,inputMat,Imgproc.COLOR_RGB2GRAY);
//
//        //getting the output matrix with the previously determined sizes
//        Mat outputMat = new Mat((int) dst_height,(int) dst_width,CvType.CV_8UC1);
//
//        //applying the transformation
//        Imgproc.warpPerspective(inputMat,outputMat,perspectiveTransformation,new Size(dst_width,dst_height));
//
//        //creating the output bitmap
//        Bitmap outputBitmap = Bitmap.createBitmap((int)dst_width,(int)dst_height, Bitmap.Config.RGB_565);
//
//        //Mat to Bitmap
//        Imgproc.cvtColor(outputMat,outputMat,Imgproc.COLOR_GRAY2RGB);
//        Utils.matToBitmap(outputMat,outputBitmap);
//
//        return outputBitmap;
//    }

    private void showButton() {
        cropButton.setVisibility(View.VISIBLE);


    }

    private void hideButton() {
        cropButton.setVisibility(View.GONE);
    }

    private MatOfPoint2f findLargestContour(Mat src) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get the 5 largest contours
        Collections.sort(contours, (o1, o2) -> {
            double area1 = Imgproc.contourArea(o1);

            double area2 = Imgproc.contourArea(o2);
//            Log.v(TAG,"area1"+String.valueOf(area1));
//            Log.v(TAG,"area2"+String.valueOf(area2));
            return (int) (area2 - area1);
        });
        if (contours.size() > 5) contours.subList(4, contours.size() - 1).clear();

        MatOfPoint2f largest = null;
//        if(contours.size()>2)
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f c = new MatOfPoint2f();
            contour.convertTo(c, CvType.CV_32FC2);
            Imgproc.approxPolyDP(c, approx, Imgproc.arcLength(c, true) * 0.02, true);

            if (approx.total() == 4 && Imgproc.contourArea(contour) > 400) {
                // the contour has 4 points, it's valid
                largest = approx;
                break;
            }
        }


        return largest;
    }

    void detectCorner(Mat inputImage) {


    }

    void findDocument(Mat frame) {

        Bitmap bmp = null;
        Mat tmp = new Mat((int) frame.size().height, (int) frame.size().width, CvType.CV_8U, new Scalar(4));
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
            bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmp, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
    }


    /**
     * Saves the image as PNG to the app's cache directory.
     *
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    private Uri saveImage(Bitmap image) {
        //TODO - Should be processed in another thread
        File imagesFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.testntrack.fileprovider", file);

        } catch (IOException e) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

    /**
     * Shares the PNG image from Uri.
     *
     * @param uri Uri of image to share.
     */
    private void shareImageUri(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");

        startActivity(intent);
    }
}
