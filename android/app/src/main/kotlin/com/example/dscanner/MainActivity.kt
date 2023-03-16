package com.example.dscanner


import android.annotation.TargetApi
// import required packages
import org.opencv.core.Core
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import android.graphics.Matrix
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.io.*


class MainActivity : FlutterActivity() {
    private val CHANNEL = "samples.flutter.dev/dscanner"
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "cropImage") {
                val x1: Double = call.argument("x1")!!
                val x2: Double = call.argument("x2")!!
                val x3: Double = call.argument("x3")!!
                val x4: Double = call.argument("x4")!!
                val y1: Double = call.argument("y1")!!
                val y2: Double = call.argument("y2")!!
                val y3: Double = call.argument("y3")!!
                val y4: Double = call.argument("y4")!!
                val imgWidth: Int = call.argument("width")!!
                val imgHeight: Int = call.argument("height")!!
                val imgPath: String = call.argument("imgPath")!!
                val croppedImage = cropImage(
                    x1,
                    x2,
                    x3,
                    x4,
                    y1,
                    y2,
                    y3,
                    y4,
                    imgWidth.toDouble(),
                    imgHeight.toDouble(),
                    imgPath
                )
                result.success(croppedImage)


            }
            if (call.method == "filtersImages") {
                val imgPath: String = call.argument("imgPath")!!
                val imageFile = File(imgPath)
                val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
                var bitmap: Bitmap = BitmapFactory.decodeFile(imageFile.path, bmOptions)
                var whiteboardBitmap: Bitmap = BitmapFactory.decodeFile(imageFile.path, bmOptions)
                val currentImage = Mat()
                val currentImage2 = Mat()
                val options: BitmapFactory.Options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(File(imgPath).getAbsolutePath(), options)
                val imageHeight: Int = options.outHeight
                val imageWidth: Int = options.outWidth
                var whiteboardMat: Mat = Mat()
                var adaptiveMat = Mat()
                val blurMat = Mat()
                var grayImgUri: String = ""
                var whiteboardImgUri: String = ""
                try {
                    /// Convert image bitmap to mat
                    Utils.bitmapToMat(bitmap, currentImage)
                    Utils.bitmapToMat(bitmap, currentImage2)

                    /// Converting image mat  to grayscale  mat
                    var grayscaleMat: Mat = Mat()
                    var grayBitmap: Bitmap = Bitmap.createBitmap(
                        currentImage2.cols(),
                        currentImage2.rows(),
                        Bitmap.Config.ARGB_8888
                    )
                    Imgproc.cvtColor(currentImage, currentImage, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.cvtColor(currentImage2, grayscaleMat, Imgproc.COLOR_BGR2GRAY);
                    ///Converting Grayscale mat to grayscale Bitmap image
                    Utils.matToBitmap(grayscaleMat, grayBitmap);
                    /// Get Grayscale image Uri
                    grayImgUri = saveImage(grayBitmap)
//                  Imgproc.threshold(grayscaleMat, adaptiveMat, 200.0, 255.0, Imgproc.THRESH_BINARY)
                    ///// Converting image mat to adaptive image mat for whiteboard image
                    Imgproc.adaptiveThreshold(
                        currentImage, adaptiveMat,
                        255.0,
                        Imgproc.ADAPTIVE_THRESH_MEAN_C,
                        Imgproc.THRESH_BINARY,
                        401,
                        14.0,
                    )
                    //// Converting apative mat to blur mat   For image smoothness
                    Imgproc.GaussianBlur(adaptiveMat, blurMat, Size(5.0, 5.0), 0.0)
                    Core.addWeighted(blurMat, 0.5, currentImage, 0.5, 1.0, whiteboardMat)

                    /// Converting whiteboard mat to bitmap
                    Utils.matToBitmap(whiteboardMat, whiteboardBitmap)

                    /// Get whiteboard image uri
                    whiteboardImgUri = saveImage(whiteboardBitmap)
                } catch (e: Exception) {
                    println("Native ======>  ImageFilter : Excpetion = $e")
                    result.success("Some Excpetion $e")
                }
                val resltList = listOf(imgPath, whiteboardImgUri, grayImgUri)
                result.success(resltList)
            }

            if (call.method == "rotate") {
                var outputImgUri : String = ""
                val imgPath: String = call.argument("imgPath")!!
                val angle: Double = call.argument("angle")!!
                val src: Mat = Imgcodecs.imread(imgPath)
                // Create empty Mat object to store output image
                val dst: Mat = Mat()
                try{// Define Rotation Angle
                    // Image rotation according to the angle provided
                    if (angle == 90.0 || angle == -270.0)
                        Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);
                    else if (angle == 180.0 || angle == -180.0)
                        Core.rotate(src, dst, Core.ROTATE_180);
                    else if (angle == 270.0 || angle == -90.0)
                        Core.rotate(
                            src, dst,
                            Core.ROTATE_90_COUNTERCLOCKWISE
                        );
                    else {
                        // Center of the rotation is given by
                        // midpoint of source image :
                        // (width/2.0,height/2.0)
                        val rotPoint: Point = Point(
                            src.cols() / 2.0,
                            src.rows() / 2.0
                        );
                        // Create Rotation Matrix
                        val rotMat: Mat = Imgproc.getRotationMatrix2D(
                            rotPoint, angle, 1.0
                        );
                        // Apply Affine Transformation
                        Imgproc.warpAffine(
                            src, dst, rotMat, src.size(),
                            Imgproc.WARP_INVERSE_MAP
                        );
                        // If counterclockwise rotation is required use
                        // following: Imgproc.warpAffine(src, dst,
                        // rotMat, src.size());
                    }
                    //// convert dst Mat to bitmap

                    var rotatedImgBitmap: Bitmap = Bitmap.createBitmap(
                        dst.cols(),
                        dst.rows(),
                        Bitmap.Config.ARGB_8888
                    )
                    Utils.matToBitmap(dst,rotatedImgBitmap);


                    /// Get Image Uri from rotatedBitmap


                    outputImgUri = saveImage(rotatedImgBitmap);



//
//                    // Save rotated image
//                    // Destination where rotated image is saved
//                    // on local directory
//
//                    val imagetype = Imgcodecs.imwrite(imgPath, dst)
//
                    // Print message for successful execution of program
                    println("Image Rotated Successfully $outputImgUri")

                }
                catch (e: Exception) {
                    println("Native ======>  ImageRotate : Excpetion = $e")
                    result.success("Some Excpetion $e")
                }
                result.success(outputImgUri)
            } else {
                result.notImplemented()
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun cropImage(
        x1: Double,
        x2: Double,
        x3: Double,
        x4: Double,
        y1: Double,
        y2: Double,
        y3: Double,
        y4: Double,
        imgWidth: Double,
        imgHeight: Double,
        originalImgPath: String,
    ): String {
        var imageUri: String? = ""
        val imageFile = File(originalImgPath)
        try {
            val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
            var bitmap: Bitmap = BitmapFactory.decodeFile(imageFile.path, bmOptions)
            bitmap = Bitmap.createScaledBitmap(bitmap, imgWidth.toInt(), imgHeight.toInt(), true)
            val point1 = Point(x1, y1)
            val point2 = Point(x2, y2)
            val point3 = Point(x3, y3)
            val point4 = Point(x4, y4)
            val src = MatOfPoint2f(
                point1, point2, point3, point4
            )
            val currentImage = Mat()
            /// Convert Input Image Bitmap to Mat
            Utils.bitmapToMat(bitmap, currentImage)
            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(imgWidth.toDouble(), 0.0),
                Point(0.0, imgHeight),
                Point(imgWidth, imgHeight)
            )
            /// Get Wrap Materix
            val warpMat = Imgproc.getPerspectiveTransform(src, dst)
            val destImage = Mat()
            //// Get Prespective Cropped Image (Saved in destImage Mat )
            Imgproc.warpPerspective(currentImage, destImage, warpMat, Size(imgWidth, imgHeight))
            val tempbmp =
                Bitmap.createBitmap(destImage.cols(), destImage.rows(), Bitmap.Config.ARGB_8888)
            ///// Converting Output Mat to Bitmap
            Utils.matToBitmap(destImage, tempbmp)
//            Utils.matToBitmap(currentImage, currentBitmap)
            imageUri = saveImage(tempbmp)
        } catch (e: Exception) {
            return "EXP : $e"
        }
        println("Native ======>  Cropped Image Result : ${imageUri}");
        return imageUri ?: ""
    }

    private fun saveImage(image: Bitmap): String {
        val imagesFolder = File(context.getCacheDir(), "tempImage")
        var filePath: String? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, System.currentTimeMillis().toString() + ".png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            filePath = file.absolutePath
        } catch (e: IOException) {
            println("Native ======> Exception : Exception occur while saving temp chache image")
        }
        var result: String = ""
        result = filePath ?: ""
        return result
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}

