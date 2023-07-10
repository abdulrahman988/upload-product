package com.example.uploadproduct

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.uploadproduct.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private var selectedImagesListUri = mutableListOf<Uri>()
    private var selectedImagesListString = mutableListOf<String>()

    private var firestore = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance().reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //multiple images selected
                    val data = result.data
                    if (data?.clipData != null) {
                        val count = data.clipData?.itemCount
                        for (i in 0 until count!!) {
                            val imageUri: Uri = data.clipData!!.getItemAt(i)!!.uri
                            selectedImagesListUri.add(imageUri)
                        }
                    } else if (data?.data != null) {
                        val imageUri = data.data!!
                        selectedImagesListUri.add(imageUri)
                    }
                }
                binding.tvSelectedImages.text = selectedImagesListUri.size.toString()
            }

        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            resultLauncher.launch(intent)

        }


        binding.buttonSaveProduct.setOnClickListener {
            uploadImage()

            lifecycleScope.launch() {
                 saveProduct()
             }



        }
    }


//    private fun validateProductInformation(): Boolean {
//        if (binding.edName.text.toString().isEmpty()) {
//            return false
//        }
//        if (binding.edPrice.text.toString().isEmpty()) {
//            return false
//        }
//        if (binding.edCategory.text.toString().isEmpty()) {
//            return false
//        }
//        if (selectedImagesListString.isEmpty()) {
//            return false
//        }
//
//        return true
//
//
//    }

    private fun uploadImage(){
                selectedImagesListUri.forEach {
                    val imageRef = storage.child("products/images/${UUID.randomUUID()}")
                    val uploadTask = imageRef.putFile(it)

                    uploadTask
                        .addOnSuccessListener {
                            val downloadUrl = imageRef.downloadUrl
                            downloadUrl.addOnSuccessListener {
                                Log.d("asd", "image link  ${it.toString()}")
                                selectedImagesListString.add(it.toString())
                                Log.d("asd", "selected image inside ${selectedImagesListString.size}")
                            }
                        }
                        .addOnFailureListener {
                            Log.d("asd", it.message.toString())
                        }

                }
        }


    private suspend fun saveProduct() {

//        Log.d("asd", selectedImagesListUri.size.toString())
//
//        runBlocking {
//            async {
//                selectedImagesListUri.forEach {
//                    val imageRef = storage.child("products/images/${UUID.randomUUID()}")
//                    val uploadTask = imageRef.putFile(it)
//
//                    uploadTask
//                        .addOnSuccessListener {
//                            val downloadUrl = imageRef.downloadUrl
//                            downloadUrl.addOnSuccessListener {
//                                Log.d("asd", "image link  ${it.toString()}")
//                                selectedImagesListString.add(it.toString())
//                                Log.d(
//                                    "asd", "selected image inside ${selectedImagesListString.size}"
//                                )
//                            }
//                        }
//                        .addOnFailureListener {
//                            Log.d("asd", it.message.toString())
//                        }
//
//                }
//            }
//        }
        delay(100000)


        val id = UUID.randomUUID().toString()
        val name = binding.edName.text.toString().trim()
        val category = binding.edCategory.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val productSize = binding.edSizes.text.toString().trim().split(",").toList()
        val colorsList = binding.edColors.text.toString().trim().split(",").toList()

        val product = Product(
            id,
            name,
            category,
            price.toFloat(),
            offerPercentage.toFloat(),
            description,
            colorsList,
            productSize,
            selectedImagesListString
        )
        firestore.collection("Products").add(product)
            .addOnSuccessListener {
                Log.d("asd", "product saves successfully")
            }.addOnFailureListener {
                Log.d("asd", it.message.toString())
            }
        Log.d("asd", "selected image save product ${selectedImagesListString.size}")

    }
}
