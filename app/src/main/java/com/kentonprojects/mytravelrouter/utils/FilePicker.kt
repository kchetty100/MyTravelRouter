package com.kentonprojects.mytravelrouter.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun rememberFilePicker(
    onFileSelected: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            readFileContent(context, it, onFileSelected)
        }
    }
    
    return {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch("*/*")
    }
}

private fun readFileContent(
    context: Context,
    uri: Uri,
    onContentRead: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.readText()
        reader.close()
        inputStream?.close()
        onContentRead(content)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
