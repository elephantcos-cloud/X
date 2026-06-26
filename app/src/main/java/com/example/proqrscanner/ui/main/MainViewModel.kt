package com.example.proqrscanner.ui.main

import android.app.Application
import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proqrscanner.data.repository.HistoryRepository
import com.example.proqrscanner.ui.scanner.QrAnalyzer
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private var camera: Camera? = null
    private var isFlashOn = false
    private var analyzer: QrAnalyzer? = null

    private val _flashState = MutableLiveData(false)
    val flashState: LiveData<Boolean> = _flashState

    private val _scanResultEvent = MutableLiveData<ScanResult?>()
    val scanResultEvent: LiveData<ScanResult?> = _scanResultEvent

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun startCamera() {
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()

            analyzer = QrAnalyzer { value, type ->
                handleScanResult(value, type)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer!!)
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    // lifecycle owner টি Activity থেকে পাস করতে হবে। আমরা সরাসরি bindToLifecycle
                    // ডাকতে পারি না, কারণ ViewModel এ lifecycle নেই। এজন্য আমরা Activity-তে ক্যামেরা সেটআপ করি।
                    // এটা আমরা পরে Activity থেকে কল করবো। এখানে শুধু ইন্ডিকেট করা।
                )
                // কিন্তু এটা ViewModel থেকে করা যাবে না। তাই startCamera মেথডটা আমরা Activity-তে রেখে দেব।
            } catch (e: Exception) {
                _errorMessage.postValue("Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun toggleFlash() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                it.cameraControl.enableTorch(isFlashOn)
                _flashState.value = isFlashOn
            }
        }
    }

    private fun handleScanResult(value: String, type: Int) {
        val typeStr = when (type) {
            Barcode.TYPE_URL -> "URL"
            Barcode.TYPE_WIFI -> "Wi-Fi"
            Barcode.TYPE_TEXT -> "Text"
            else -> "Code"
        }
        viewModelScope.launch {
            historyRepository.addScan(value, typeStr)
        }
        _scanResultEvent.postValue(ScanResult(value, typeStr))
    }

    fun onResultConsumed() {
        _scanResultEvent.value = null
    }

    data class ScanResult(val content: String, val type: String)
}
