package com.scopedstoragesample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.scopedstoragesample.task1.SelectAndEditFileFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startFragment(SelectAndEditFileFragment())

    }
    private fun startFragment(newFragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.container, newFragment);
       // ft.addToBackStack("add");
        ft.commit();
    }
}