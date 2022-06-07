package com.example.assignment2

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.carrier.CarrierIdentifier
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URL
import com.android.volley.toolbox.ImageRequest
import org.json.JSONException
import org.w3c.dom.Text

class BarcodeScannerActivity : AppCompatActivity() {

    lateinit var textView_http_response: TextView

    lateinit var nameIdentifier: TextView
    lateinit var ingredientIdentifier: TextView
    lateinit var nutrientsIdentifier: TextView

    lateinit var decodeValue: TextView
    lateinit var itemName: TextView
    lateinit var nutrientsName: TextView
    lateinit var ingredientsName: TextView

    lateinit var itemImage: ImageView

    var image_found = false

    private val WEB_API_PREFIX = "https://world.openfoodfacts.org/api/v0/product/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        val queue = Volley.newRequestQueue(this)

        textView_http_response = findViewById(R.id.textView_http_response)

        nameIdentifier = findViewById(R.id.nameIdentifier)
        ingredientIdentifier = findViewById(R.id.ingredientIdentifier)
        nutrientsIdentifier = findViewById(R.id.nutrientsIdentifier)

        decodeValue = findViewById(R.id.decodeValue)
        itemName = findViewById(R.id.itemName)
        nutrientsName = findViewById(R.id.nutrientsName)
        ingredientsName = findViewById(R.id.ingredientName)

        itemImage = findViewById(R.id.itemImage)

        // starts here

        // gets the barcode # from the previous activity
        decodeValue.text = intent.getStringExtra("tvResult")

        getJsonResponse(queue)

    }


    fun getApiUrl(): String {
        val api = WEB_API_PREFIX + decodeValue.text.toString() + ".json"
        return api
    }

    // for getting non-image values
    fun getJsonResponse(requestQueue: RequestQueue) {

        val stringRequest = StringRequest(Request.Method.GET, getApiUrl(),
            { response ->
                textView_http_response.text = response.toString()
                try {

                    val jsonObject = JSONObject(response)

                    val status = jsonObject["status"].toString()

                    if (status == "0") {
                        itemName.text = "No Item"
                        ingredientsName.text = "No Item"
                        nutrientsName.text = "No Item"
                    } else {
                        val product = jsonObject.getJSONObject("product")
                        // get information from the key, "product"
                        val name = product.getString("product_name")
                        itemName.text = name

                        val ingredientText =
                            product.getString("ingredients_text").replace(", ", ",\n")
                        ingredientsName.text = ingredientText

                        val nutrientsKeys = product.getJSONObject("nutrient_levels").toString()
                        // for formatting JSONObject to "readable"...
                        val token = nutrientsKeys
                            .replace("{", "")
                            .replace("}", "")
                            .replace(":", " : ")
                            .replace("\"", "")
                            .replace(",", ",\n")
                        nutrientsName.text = token

                        val image_url = product.getString("image_url")

                        val imageRequest = ImageRequest(
                            image_url,
                            { bitmap ->
                                itemImage.setImageBitmap(bitmap)
                                image_found = true
                            },
                            0,
                            0,
                            ImageView.ScaleType.FIT_XY,
                            Bitmap.Config.ARGB_8888,
                            { error ->
                                Log.e(TAG, error.toString())
                            }
                        )
                        requestQueue.add(imageRequest)
                    }

                } catch (err: JSONException) {
                    Log.e(TAG, err.toString())
                }

            },
            Response.ErrorListener { textView_http_response.text = "That didn't work!" })

        requestQueue.add(stringRequest)
    }
}