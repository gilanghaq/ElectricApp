package com.example.electricapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.electricapp.databinding.ActivityAddNewBinding
import com.example.electricapp.model.ElectricModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var imageUri: Uri
    private var isEditing: Boolean = false
    private var electricModel: ElectricModel? = null

    companion object {
        private const val REQUEST_CAMERA = 27
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Meteran")

        electricModel = intent.getSerializableExtra("electricModel") as ElectricModel?
        if (electricModel != null) {
            isEditing = true
            setFormData(electricModel)
        }

        binding.saveButton.setOnClickListener {
            if (isEditing) {
                updateData()
            } else {
                saveData()
            }
        }

        binding.addImage.setOnClickListener {
            intentCamera()
        }

        //enable back arrow in action bar
        supportActionBar!!.apply {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
        //change action bar color
        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.navy_200
                )
            )
        )
        //change text in action bar
        supportActionBar!!.title = "Tambah Meteran Baru"
    }

    private fun intentCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val imgBitmap = data?.extras?.get("data") as Bitmap
            uploadImage(imgBitmap)
        }
    }

    private fun uploadImage(imgBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val timestamp = System.currentTimeMillis()
        val fileName = "Meteran_$timestamp.jpg"
        val ref = FirebaseStorage.getInstance().getReference()
            .child("Meteran/${FirebaseAuth.getInstance().currentUser?.uid}/$fileName")

        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { urlTask ->
                        urlTask.result?.let {
                            imageUri = it
                            binding.addImage.setImageBitmap(imgBitmap)
                        }
                    }
                }
            }
    }

    private fun saveData() {
        val fillName = binding.fillName.text.toString()
        val fillAddress = binding.fillAddress.text.toString()
        val fillDate = binding.fillDate.text.toString()
        val fillPower = binding.fillPower.text.toString()
        val fillFirstMeter = binding.fillFirstMater.text.toString()
        val fillLastMeter = binding.fillLastMeter.text.toString()

        if (fillName.isNotEmpty() && fillAddress.isNotEmpty() && fillDate.isNotEmpty() && fillPower.isNotEmpty() && fillFirstMeter.isNotEmpty() && fillLastMeter.isNotEmpty()) {
            val cattleId = dbRef.push().key
            val cattle = ElectricModel(
                fillId = cattleId,
                fillName = fillName,
                fillAddress = fillAddress,
                fillDate = fillDate,
                fillPower = fillPower,
                imageUrl = imageUri.toString(),
                fillFirstMeter = fillFirstMeter,
                fillLastMeter = fillLastMeter
            )

            if (cattleId != null) {
                dbRef.child(cattleId).setValue(cattle)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Cattle added successfully", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to add cattle", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFormData(electricModel: ElectricModel?) {
        electricModel?.let {
            binding.fillName.setText(it.fillName)
            binding.fillAddress.setText(it.fillAddress)
            binding.fillDate.setText(it.fillDate)
            binding.fillPower.setText(it.fillPower)
            binding.fillFirstMater.setText(it.fillFirstMeter)
            binding.fillLastMeter.setText(it.fillLastMeter)
        }
    }

    private fun updateData() {
        val fillName = binding.fillName.text.toString()
        val fillAddress = binding.fillAddress.text.toString()
        val fillDate = binding.fillDate.text.toString()
        val fillPower = binding.fillPower.text.toString()
        val fillFirstMeter = binding.fillFirstMater.text.toString()
        val fillLastMeter = binding.fillLastMeter.text.toString()

        if (fillName.isNotEmpty() && fillAddress.isNotEmpty() && fillDate.isNotEmpty() && fillPower.isNotEmpty() && fillFirstMeter.isNotEmpty() && fillLastMeter.isNotEmpty()) {
            electricModel?.let {
                val cattleId = it.fillId

                val updatedCattle = ElectricModel(
                    fillId = cattleId,
                    fillName = fillName,
                    fillAddress = fillAddress,
                    fillDate = fillDate,
                    fillPower = fillPower,
                    imageUrl = it.imageUrl, // Menyimpan URL gambar yang sudah ada
                    fillFirstMeter = fillFirstMeter,
                    fillLastMeter = fillLastMeter
                )

                if (cattleId != null) {
                    // Jika ada gambar yang baru dipilih, unggah gambar tersebut
                    if (imageUri != null) {
                        uploadImage(updatedCattle, cattleId)
                    } else {
                        updateCattleData(updatedCattle)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(updatedCattle: ElectricModel, cattleId: String) {
        val baos = ByteArrayOutputStream()
        val timestamp = System.currentTimeMillis()
        val fileName = "Meteran_$timestamp.jpg"
        val ref = FirebaseStorage.getInstance().getReference()
            .child("Meteran/${FirebaseAuth.getInstance().currentUser?.uid}/$fileName")

        val imgBitmap = (binding.addImage.drawable as BitmapDrawable).bitmap
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { urlTask ->
                        urlTask.result?.let { imageUrl ->
                            updatedCattle.imageUrl =
                                imageUrl.toString() // Mendapatkan URL gambar yang baru
                            updateCattleData(updatedCattle)
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateCattleData(updatedCattle: ElectricModel) {
        dbRef.child(updatedCattle.fillId!!).setValue(updatedCattle)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cattle updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update cattle", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

