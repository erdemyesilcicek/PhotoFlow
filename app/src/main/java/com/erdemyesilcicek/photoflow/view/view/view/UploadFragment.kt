package com.erdemyesilcicek.photoflow.view.view.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.erdemyesilcicek.photoflow.databinding.FragmentUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore

    //galeri için
    private lateinit var permissonLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var selectedPhoto : Uri? = null
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

        registerLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uploadButton.setOnClickListener { uploadButtonClicked(it) }
        binding.imageViewUpload.setOnClickListener { imageViewUploadClicked(it) }
    }

    fun uploadButtonClicked(view: View){
        val uuid = UUID.randomUUID()
        val photoName = "${uuid}.jpg"
        val reference = storage.reference
        val photoReference = reference.child("images").child(photoName)
        if(selectedPhoto != null){
            photoReference.putFile(selectedPhoto!!).addOnSuccessListener {uploadTask ->
                //url al
                photoReference.downloadUrl.addOnSuccessListener {uri ->
                    if(auth.currentUser != null){
                        val downloadUrl = uri.toString()
                        //veritabanına kayıt
                        val postMap = hashMapOf<String, Any>()
                        postMap.put("downloadUrl", downloadUrl)
                        postMap.put("email", auth.currentUser?.email.toString())
                        postMap.put("comment", binding.descriptionTextUpload.text.toString())
                        postMap.put("date", Timestamp.now())

                        db.collection("Posts").add(postMap).addOnSuccessListener {documentReference ->
                            //veri dbde
                            val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                            Navigation.findNavController(requireView()).navigate(action)
                        }.addOnFailureListener {exception ->
                            Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.addOnFailureListener {exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun imageViewUploadClicked(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //READ MEDIA IMAGES
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin yok
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    //izin mantığını kullanıcıya göstermemiz lazım. Android kendi karar veriyor.
                    Snackbar.make(view, "need permission for gallery",Snackbar.LENGTH_INDEFINITE).setAction("give permission",
                        View.OnClickListener {
                            //izin istememiz lazım
                            permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }else{
                    //izin istememiz lazım
                    permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin var
                //galeriye gitme kodu lazım
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            //READ EXTERNAL STORAGE
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin yok
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //izin mantığını kullanıcıya göstermemiz lazım. Android kendi karar veriyor.
                    Snackbar.make(view, "need permission for gallery",Snackbar.LENGTH_INDEFINITE).setAction("give permission",
                        View.OnClickListener {
                            //izin istememiz lazım
                            permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else{
                    //izin istememiz lazım
                    permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //izin var
                //galeriye gitme kodu lazım
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLaunchers(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    selectedPhoto = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPhoto!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageViewUpload.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPhoto)
                            binding.imageViewUpload.setImageBitmap(selectedBitmap)
                        }
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

        permissonLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(),"need permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}