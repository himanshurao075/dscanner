package com.testntrack.opencvscanner

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.testntrack.opencvscanner.scanner.DetectorActivity
import com.testntrack.opencvscanner.scanner.DetectorCameraXActivity

class ScanActivity : AppCompatActivity() {


    companion object {
        val REQUEST_CODE: Int = 8080;
        val FILE_URI = "CROPPED-BITMAP"

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val croppedImage = data?.getStringExtra(FILE_URI);

            resultImageView.setImageBitmap(BitmapFactory.decodeFile(croppedImage))

            setResult(Activity.RESULT_OK,data);

            finish();
        }
    }


    fun startScanner() {
        val startIntent = Intent(applicationContext, DetectorCameraXActivity::class.java);

        startActivityForResult(startIntent, REQUEST_CODE)

//     resultLauncher.launch(startIntent)


    }

    private lateinit var startBtn: Button
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>


    private lateinit var resultImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        startScanner()


    }


    fun initView() {
        startBtn = findViewById(R.id.btnStartScanner)
        resultImageView = findViewById(R.id.ivResult)

        startBtn.setOnClickListener {
            startScanner();
        }
    }
}