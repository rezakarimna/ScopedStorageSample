package com.scopedstoragesample.task1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.scopedstoragesample.databinding.FragmentSelectAndEditFileBinding
import java.io.*


class SelectAndEditFileFragment : Fragment() {
    private lateinit var binding: FragmentSelectAndEditFileBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var uriLoadFile: Uri
    var resolver = activity?.contentResolver

    companion object {
        // Request code for creating a PDF document.
        const val CREATE_FILE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelectAndEditFileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveFile.setOnClickListener { saveFileText(uriLoadFile , binding.editLoadFile.text.toString()) }
        binding.btnOpenFile.setOnClickListener { openFileTextWhitSaf() }
        binding.btnCreateFile.setOnClickListener {
//            createFile()
        openDocTree()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissions()
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
//                    binding.txtPath.text = data?.data.toString()
                    val uri = Uri.parse(data?.data.toString())
//                    binding.imgPhoto.setImageURI(uri)
                }
            }
        updateOrRequestPermissions()
        openDocTree()
    }

    private fun selectFile2() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        resultLauncher.launch(intent)
    }

    private fun selectFile() {
        var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        resultLauncher.launch(chooseFile)
    }

    fun openDocTree() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, 100)
    }

    fun openIntentChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission: Boolean = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED
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

    private fun permissions() {
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                writePermissionGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: writePermissionGranted

                if (readPermissionGranted) {
                    Toast.makeText(activity, "read files without permission.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(
                        activity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(Intent.EXTRA_TITLE, "myText.txt")

        }
        startActivityForResult(intent, 1)
    }

    private fun openFileTextWhitSaf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "text/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val uri = Uri.parse(data?.data.toString())
            saveFileText2(uri , binding.editNewFile.text.toString())
        }
        if (requestCode == 2) {
            uriLoadFile = Uri.parse(data?.data.toString())
            loadFileText(uriLoadFile)
        }
    }

    private fun saveFileText(uri: Uri , text:String) {
       // DocumentsContract.deleteDocument(activity?.applicationContext?.contentResolver!! , uri)
        var fos: OutputStream? = null
        try {
            fos = activity?.contentResolver?.openOutputStream(uri)
            fos?.write(text.toByteArray())
            fos?.close()
            Toast.makeText(activity, "file create", Toast.LENGTH_SHORT).show()
            binding.editLoadFile.setText("")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "file not create", Toast.LENGTH_SHORT).show()
        }

    }
    private fun saveFileText2(uri: Uri , text:String) {
        var fos: OutputStream? = null
        try {
            fos = activity?.contentResolver?.openOutputStream(uri)
            fos?.write(text.toByteArray())
            fos?.close()
            Toast.makeText(activity, "file create", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "file not create", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadFileText(uri: Uri) {
        var fis: InputStream? = null
        try {
            fis = activity?.contentResolver?.openInputStream(uri)
            val inputStreamReader = InputStreamReader(fis)
            val bufferedReader = BufferedReader(inputStreamReader)
            var text: String?
            val sb = StringBuffer()
            while (bufferedReader.readLine().also { text = it } != null) {
                sb.append(text)
            }
            binding.editLoadFile.setText(sb.toString())

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                assert(fis != null)
                fis!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun alterDocument(uri: Uri , text: String) {
        try {
            resolver?.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(text.toByteArray())
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}