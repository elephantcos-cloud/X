package com.example.proqrscanner.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import com.example.proqrscanner.databinding.ActivityMainBinding
import com.example.proqrscanner.ui.history.HistoryActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.startCamera() else showPermissionDenied()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCameraPermission()

        binding.btnFlash.setOnClickListener {
            viewModel.toggleFlash()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(HistoryActivity.newIntent(this))
        }

        observeViewModel()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun observeViewModel() {
        viewModel.flashState.observe(this) { isOn ->
            binding.btnFlash.setImageResource(
                if (isOn) android.R.drawable.ic_menu_compass else android.R.drawable.ic_menu_compass
            )
            // আপনি ইমেজ পরিবর্তন করতে পারেন
        }

        viewModel.scanResultEvent.observe(this) { result ->
            result?.let {
                // Bottom sheet দেখানো
                ResultBottomSheet.show(this, it) {
                    viewModel.onResultConsumed()
                }
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showPermissionDenied() {
        Snackbar.make(binding.root, "Camera permission required", Snackbar.LENGTH_INDEFINITE)
            .setAction("Grant") { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
            .show()
    }
}
