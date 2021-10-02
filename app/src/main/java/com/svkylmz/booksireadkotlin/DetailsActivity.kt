package com.svkylmz.booksireadkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.svkylmz.booksireadkotlin.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {
    //view binding start
    private lateinit var binding: ActivityDetailsBinding

    private lateinit var database: SQLiteDatabase

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //String = READ_EXTERNAL_STORAGE
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //initialize database
        database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.bookNameTextView.setText("")
            binding.authorNameTextView.setText("")
            binding.pageTextView.setText("")
            binding.imageView.setImageResource(R.drawable.select)
            binding.button.visibility = View.VISIBLE
        } else {
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)
            val cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", arrayOf(selectedId.toString()))
            val bookNameIndex = cursor.getColumnIndex("bookname")
            val authorNameIndex = cursor.getColumnIndex("authorname")
            val pageIndex = cursor.getColumnIndex("page")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.bookNameTextView.setText(cursor.getString(bookNameIndex))
                binding.authorNameTextView.setText(cursor.getString(authorNameIndex))
                binding.pageTextView.setText(cursor.getString(pageIndex))
                val byteArray = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }
    }

    fun saveDetails(v: View) {
        val bookName = binding.bookNameTextView.text.toString()
        val authorName = binding.authorNameTextView.text.toString()
        val page = binding.pageTextView.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = minimalizeImage(selectedBitmap!!, 300)

            //convert image to data
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            //DATABASE OPERATIONS
            try {
                //val database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookname VARCHAR, authorname VARCHAR, page VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO books (bookname, authorname, page, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, bookName)
                statement.bindString(2, authorName)
                statement.bindString(3, page)
                statement.bindBlob(4, byteArray)
                statement.execute()

            } catch(e: Exception) {
                e.printStackTrace()
            }

            //After save operations, return to main activity and close other activities
            val intent = Intent(this@DetailsActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun minimalizeImage(image: Bitmap, maximumSize: Int) : Bitmap {
        //image minimalization with bitmap before saving
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        } else {
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(v: View) {
        //permission control to access the gallery
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(v, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            //access to gallery with intent
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@DetailsActivity.contentResolver, imageData) //getBitmap duplicated
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result) {
                //permission granted - access to gallery with intent
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(this@DetailsActivity, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }
}