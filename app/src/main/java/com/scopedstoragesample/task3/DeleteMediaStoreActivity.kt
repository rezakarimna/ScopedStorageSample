package com.scopedstoragesample.task3

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.scopedstoragesample.databinding.ActivityDeleteMediaStoreBinding
import com.scopedstoragesample.task3.util.ClickItem
import com.scopedstoragesample.task3.util.sdk29AndUp

class DeleteMediaStoreActivity : AppCompatActivity(), ClickItem {
    private lateinit var binding: ActivityDeleteMediaStoreBinding
    private lateinit var externalStoragePhotoAdapter: AdapterGallery
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteMediaStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                writePermissionGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: writePermissionGranted

                if (readPermissionGranted) {
                    setupExternalStorageRecyclerView()
                    Toast.makeText(this, "read files without permission.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG)
                        .show()
                }

            }

        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    Toast.makeText(this, "Photo deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Photo couldn't be deleted", Toast.LENGTH_SHORT).show()
                }
                setupExternalStorageRecyclerView()
            }


        setupExternalStorageRecyclerView()
        updateOrRequestPermissions()

    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission: Boolean = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun loadPhotosFromExternalStorage(): List<ImageModel> {
        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )

        val photos = mutableListOf<ImageModel>()
        contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                photos.add(ImageModel(contentUri.toString(), id))
            }
            photos.toList()
        } ?: listOf()

        return photos
    }

    private fun setupExternalStorageRecyclerView() = binding.rvPublicPhotos.apply {
        val photos = loadPhotosFromExternalStorage()
        externalStoragePhotoAdapter = AdapterGallery(photos, this@DeleteMediaStoreActivity)
        binding.rvPublicPhotos.adapter = externalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)

    }

    override fun selectItem(fileName: ImageModel) {
        Log.i("TAGselectItem", "selectItem: " + fileName.urlImage)
        deletePhotoFromExternalStorage(fileName.urlImage)

    }

    private fun deletePhotoFromExternalStorage(photoUri: String) {
        val uri = Uri.parse(photoUri)
        try {
            contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
        setupExternalStorageRecyclerView()
    }
}