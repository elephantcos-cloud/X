package com.example.proqrscanner.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import com.example.proqrscanner.databinding.BottomSheetResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

object ResultBottomSheet {

    fun show(context: Context, result: MainViewModel.ScanResult, onDismiss: () -> Unit) {
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetResultBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        binding.tvType.text = result.type
        binding.tvContent.text = result.content

        binding.btnCopy.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("scanned", result.content))
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpen.setOnClickListener {
            try {
                if (result.content.startsWith("http://") || result.content.startsWith("https://")) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.content)))
                } else {
                    Toast.makeText(context, "Not a valid URL", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
    }
}
