package com.example.qrscanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qrscanner.analyzer.QrAnalyzer
import com.example.qrscanner.database.HistoryManager
import com.example.qrscanner.databinding.ActivityMainBinding
import com.example.qrscanner.databinding.LayoutResultBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var historyManager: HistoryManager
    private var camera: Camera? = null
    private var isFlashOn = false
    private var isAnalysisActive = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        historyManager = HistoryManager(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnFlash.setOnClickListener { toggleFlash() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, QrAnalyzer { value, type ->
                if (isAnalysisActive) {
                    isAnalysisActive = false
                    triggerHapticFeedback()
                    runOnUiThread { processScanResult(value, type) }
                }
            })

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlash() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                it.cameraControl.enableTorch(isFlashOn)
            }
        }
    }

    private fun triggerHapticFeedback() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }

    private fun processScanResult(value: String, type: Int) {
        val typeString = when (type) {
            Barcode.TYPE_URL -> "URL Link"
            Barcode.TYPE_WIFI -> "Wi-Fi Network"
            Barcode.TYPE_TEXT -> "Plain Text"
            else -> "Data / Code"
        }

        historyManager.saveScan(value, typeString)

        val dialog = BottomSheetDialog(this)
        val sheetBinding = LayoutResultBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.resType.text = typeString
        sheetBinding.resContent.text = value

        if (type == Barcode.TYPE_URL) {
            sheetBinding.btnAction.text = "Open URL"
            sheetBinding.btnAction.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
                dialog.dismiss()
            }
        } else {
            sheetBinding.btnAction.visibility = View.GONE
        }

        sheetBinding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("QR_Result", value))
            Toast.with(this, "Copied!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setOnDismissListener { isAnalysisActive = true }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
