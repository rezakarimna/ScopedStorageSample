package com.scopedstoragesample.task1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.scopedstoragesample.databinding.FragmentSelectAndEditFileBinding
import java.io.*

class SelectAndEditFileFragment : Fragment() {
    private lateinit var binding: FragmentSelectAndEditFileBinding
    private lateinit var uriLoadFile: Uri

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
        binding.btnCreateFile.setOnClickListener { createFile() }
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
            saveFileText(uri , binding.editNewFile.text.toString())
        }
        if (requestCode == 2) {
            uriLoadFile = Uri.parse(data?.data.toString())
            loadFileText(uriLoadFile)
        }
    }

    private fun saveFileText(uri: Uri , text:String) {
        var fos: OutputStream? = null
        try {
            fos = activity?.contentResolver?.openOutputStream(uri)
            fos?.write(text.toByteArray())
            fos?.close()
            Toast.makeText(activity, "file create", Toast.LENGTH_SHORT).show()
            binding.editLoadFile.setText("")
            binding.editNewFile.setText("")
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

}