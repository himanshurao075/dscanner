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


    private val CHANNEL = "samples.flutter.dev/cropImage"
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

//
//        if (batteryLevel != -1) {
//          } else {
//          result.error("UNAVAILABLE", "Battery level not available.", null)
//        }
            }
            if(call.method=="filtersImages")
            {val imgPath: String = call.argument("imgPath")!!
                val image = File(imgPath)
                val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
                var bitmap: Bitmap = BitmapFactory.decodeFile(image.path, bmOptions)

                println("flow 1")
                var whiteboardBitmap :Bitmap=   BitmapFactory.decodeFile(image.path, bmOptions)
                val currentImage = Mat()
                val whiteboardMat = Mat()
                val grayscaleMat = Mat()
                val adaptiveMat = Mat()
                val blurMat = Mat()
                println("flow 2")
                val grayImgUri : String
                val whiteboardImgUri : String

                println("flow 3")

                try{

                    Utils.bitmapToMat(bitmap, currentImage)
                    var grayBitmap : Bitmap=  Bitmap.createBitmap(
                        currentImage.cols(),
                        currentImage.rows(),
                        Bitmap.Config.ARGB_8888
                    )
                Imgproc.cvtColor(currentImage,grayscaleMat,Imgproc.COLOR_BGR2GRAY);
                    println("flow 4")

                Utils.matToBitmap(grayscaleMat,grayBitmap);
                    println("flow 4-2")

                    grayImgUri = saveImage(grayBitmap)

                    println("flow 5")


                Imgproc.adaptiveThreshold(
                    currentImage, adaptiveMat,
                    255.0,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY,
                    401,
                    14.0
                )
                    println("flow 5-1")

                    Imgproc.GaussianBlur(adaptiveMat, blurMat, Size(5.0, 5.0), 0.0)
                    println("flow 5-2")

                Core.addWeighted(blurMat, 0.5, currentImage, 0.5, 1.0, whiteboardMat)
                    println("flow 5-3")

                Utils.matToBitmap(whiteboardMat, whiteboardBitmap)
                    println("flow 5-4")

                    whiteboardImgUri = saveImage(whiteboardBitmap)
                    println("flow 6")

               } catch (e: Exception) {
                   println(e)
                   result.success("FilteredImgException $e")
               }

                val resltList = listOf(1, 2, 3, 4, 5)


                result.success(resltList)
            }
            if (call.method == "rotate") {
                val imgPath: String = call.argument("imgPath")!!
                val angle: Double = call.argument("angle")!!
                println(imgPath)
                println(angle)
                val src :Mat = Imgcodecs.imread(imgPath)

                // Create empty Mat object to store output image
                val dst : Mat=Mat()


                // Define Rotation Angle

                // Image rotation according to the angle provided
                if (angle == 90.0 || angle == -270.0)

                    Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);
                else if (angle == 180.0 || angle == -180.0)

                    Core.rotate(src, dst, Core.ROTATE_180);
                else if (angle == 270.0 || angle == -90.0)

                    Core.rotate(src, dst,
                        Core.ROTATE_90_COUNTERCLOCKWISE);
                else {

                    // Center of the rotation is given by
                    // midpoint of source image :
                    // (width/2.0,height/2.0)
                    val rotPoint :Point= Point(src.cols() / 2.0,
                        src.rows() / 2.0);

                    // Create Rotation Matrix
                    val rotMat :Mat = Imgproc.getRotationMatrix2D(
                        rotPoint, angle, 1.0);

                    // Apply Affine Transformation
                    Imgproc.warpAffine(src, dst, rotMat, src.size(),
                        Imgproc.WARP_INVERSE_MAP);

                    // If counterclockwise rotation is required use
                    // following: Imgproc.warpAffine(src, dst,
                    // rotMat, src.size());
                }

                // Save rotated image

                // Destination where rotated image is saved
                // on local directory
                Imgcodecs.imwrite(imgPath, dst)

                // Print message for successful execution of program
                println("Image Rotated Successfully")

//            val byteArray: ByteArray
//                val byteArray: ByteArray = call.argument("bytes")!!
//                val rotateThread = RotateThread(byteArray)
//              val temp =  rotateThread.start()
//                println(temp)
                result.success(imgPath)
            }
            if (call.method == "rotateCompleted") {
                val byteArray: ByteArray = call.argument("bytes")!!
                result.success(byteArray)
            } else {
                result.notImplemented()
            }
        }
    }

    internal class RotateThread(bytes: ByteArray) : Thread() {
        var byteArray: ByteArray

        init {
            byteArray = bytes
        }


        override fun run(): Unit{
            System.out.println("started")
            val matrix = Matrix()
            matrix.postRotate(90.toFloat())
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            val rotatedBitmap: Bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
            )
            val stream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            byteArray = stream.toByteArray()
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
        inputImg: String
    ): String {
        var imageUri: String? = ""
//    val sd: File = Environment.getExternalStorageDirectory()
        val image = File(inputImg)
        try {
            val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
            var bitmap: Bitmap = BitmapFactory.decodeFile(image.path, bmOptions)
            bitmap = Bitmap.createScaledBitmap(bitmap, imgWidth.toInt(), imgHeight.toInt(), true)
            val imagePath: String
            val sortedPoints = arrayOfNulls<Point>(4)
            val point1 = Point(x1, y1)
            val point2 = Point(x2, y2)
            val point3 = Point(x3, y3)
            val point4 = Point(x4, y4)


//    MatOfPoint2f src = MatOfPoint2f()
            val src = MatOfPoint2f(
                point1, point2, point3, point4
            )


//    val src = MatOfPoint2f(
//            sortedPoints.get(0),
//            sortedPoints.get(1),
//sortedPoints.get(2),
//sortedPoints.get(3))

            val currentImage = Mat()

//    val bmp32: Bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
            Utils.bitmapToMat(bitmap, currentImage)
//    src.adjustROI(lineWidth, lineWidth, lineWidth, lineWidth);

            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(imgWidth.toDouble(), 0.0),
                Point(0.0, imgHeight),
                Point(imgWidth, imgHeight)
            )
            val warpMat = Imgproc.getPerspectiveTransform(src, dst)
            //This is your new image as Mat
            val destImage = Mat()
            // Imgproc.cvtColor(currentImage,destImage,Imgproc.COLOR_RGBA2GRAY);
            Imgproc.warpPerspective(currentImage, destImage, warpMat, Size(imgWidth, imgHeight))
//
            val tempbmp =
                Bitmap.createBitmap(destImage.cols(), destImage.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(destImage, tempbmp);

            val currentBitmap = Bitmap.createBitmap(
                currentImage.cols(),
                currentImage.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(currentImage, currentBitmap)
            Utils.matToBitmap(destImage, tempbmp)

            imageUri = saveImage(tempbmp)
            println("MY IMAGE URI $imageUri")


        } catch (e: Exception) {
            println(e)
            return "EXP : $e"
        }


//  return imagePath
        return imageUri ?: ""
    }

    private fun saveImage(image: Bitmap): String {
        //TODO - Should be processed in another thread

        val imagesFolder = File(context.getCacheDir(), "images")

        var filePath: String? = null
        println("func flow 1")
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, System.currentTimeMillis().toString() + ".png")
            println("func flow 2")

            val stream = FileOutputStream(file)
            println("func flow 3")

            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            println("func flow 4")

            stream.flush()
            stream.close()
            println("func flow 5")

            filePath = file.absolutePath
            println("func flow 6")

            //            uri = FileProvider.getUriForFile(this, "com.testntrack.opencvscanner.fileprovider", file);
        } catch (e: IOException) {
            println("func flow 7")

            Log.d("dfsd", "IOException while trying to write file for sharing: " + e.message)
            println("func flow 8")

        }
        println("func flow 9")
        var result : String = ""
        result = filePath?:""
        println("func flow 10")
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

