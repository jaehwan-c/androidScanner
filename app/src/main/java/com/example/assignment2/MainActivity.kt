package com.example.assignment2

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {

    lateinit var btnScanBarcode: Button
    lateinit var ivBarcode: ImageView
    lateinit var tvResult: TextView
    lateinit var btnGetResult: Button

    private val CAMERA_PERMISSION_CODE = 123
    private val READ_STORAGE_PERMISSION_CODE = 113
    private val WRITE_STORAGE_PERMISSION_CODE = 113

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var TAG = "MyTag"

    lateinit var inputImage: InputImage
    lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanBarcode=findViewById(R.id.btnScanBarcode)
        ivBarcode=findViewById(R.id.ivBarcode)
        tvResult=findViewById(R.id.tvResult)
        btnGetResult=findViewById(R.id.btnGetResult)

        barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.EAN_13, Barcode.UPC_A)
                .build()
        )

        cameraLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object: ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data = result?.data

                    try{
                        val photo = data?.extras?.get("data") as Bitmap
                        inputImage = InputImage.fromBitmap(photo, 0)
                        processBarcode()

                    } catch (e:Exception) {
                        Log.d(TAG, "onActivityResult: "+e.message)
                    }
                }
            }
        )

        galleryLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object: ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data = result?.data

                    if (data != null) {
                        inputImage = InputImage.fromFilePath(this@MainActivity, data.data!!)
                    }
                    processBarcode()
                }
            }
        )

        btnScanBarcode.setOnClickListener{
            val options = arrayOf("Camera", "Gallery")

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Pick an Option")

            builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                if (which==0) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
                    cameraLauncher.launch(cameraIntent)
                } else {
                    val storageIntent = Intent()
                    storageIntent.setType("image/*")
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT)
                    galleryLauncher.launch(storageIntent)
                }

            })
            builder.show()
        }

        btnGetResult.setOnClickListener{
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            val barcodeValue = tvResult.text
            intent.putExtra("tvResult", barcodeValue)
            startActivity(intent)
        }

    }

    private fun processBarcode() {
        ivBarcode.visibility = View.GONE
        tvResult.visibility = View.VISIBLE
        btnGetResult.visibility = View.VISIBLE

        tvResult.text = "If this appears, the barcode is not scanned. Please Try Again."

        barcodeScanner.process(inputImage).addOnSuccessListener {
            for (barcode in it) {
                val data=barcode.displayValue
                tvResult.text="${data}"
                    }
                }.addOnFailureListener {
            Log.d(TAG,"processBarcode: ${it.message}" )
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission(android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
    }

    private fun checkPermission(permission:String,requestCode:Int){
        if (ContextCompat.checkSelfPermission(this@MainActivity,permission) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_CODE)
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode==READ_STORAGE_PERMISSION_CODE){
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE)
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode==WRITE_STORAGE_PERMISSION_CODE){
            if(!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}