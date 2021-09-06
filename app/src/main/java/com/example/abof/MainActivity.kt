package com.example.abof

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.abof.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the ViewModel component.
        val model: ExperimentDataViewModel by viewModels()

        // Inflate view and obtain an instance of the binding class.
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Assign the component to a property in the binding class.
        binding.lifecycleOwner = this
        binding.viewModel = model
    }
}