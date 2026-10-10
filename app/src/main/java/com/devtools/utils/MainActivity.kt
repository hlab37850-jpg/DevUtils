package com.devtools.utils

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        binding.rgMode.setOnCheckedChangeListener { _, checkedId ->
            showToolMode(checkedId)
        }

        // Existing JSON Formatter logic retained.
        binding.btnFormat.setOnClickListener {
            val inputJson = binding.etInput.text.toString()
            if (inputJson.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص JSON", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val jsonObject = JSONObject(inputJson)
                binding.etOutput.text = jsonObject.toString(4)
                Toast.makeText(this, "تم التنسيق بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.etOutput.text = "خطأ في تنسيق JSON: \n${e.message}"
                Toast.makeText(this, "نص JSON غير صالح", Toast.LENGTH_SHORT).show()
            }
        }

        // Existing Base64 encoder retained.
        binding.btnEncode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص للتشفير", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val encoded = Base64.encodeToString(
                input.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
            binding.etOutput.text = encoded
            Toast.makeText(this, "تم التشفير بنجاح", Toast.LENGTH_SHORT).show()
        }

        // Existing Base64 decoder retained.
        binding.btnDecode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص Base64 لفك التشفير", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val decodedBytes = Base64.decode(input, Base64.DEFAULT)
                binding.etOutput.text = String(decodedBytes, StandardCharsets.UTF_8)
                Toast.makeText(this, "تم فك التشفير بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "خطأ: نص Base64 غير صالح."
                Toast.makeText(this, "نص Base64 غير صالح", Toast.LENGTH_SHORT).show()
            }
        }

        // Hash generator: MD5, SHA-1, SHA-256.
        binding.btnGenerateHash.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val bytes = input.toByteArray(StandardCharsets.UTF_8)
                val md5 = MessageDigest.getInstance("MD5").digest(bytes).toHex()
                val sha1 = MessageDigest.getInstance("SHA-1").digest(bytes).toHex()
                val sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()
                binding.etOutput.text = "MD5:\n$md5\n\nSHA-1:\n$sha1\n\nSHA-256:\n$sha256"
                Toast.makeText(this, "تم توليد Hash بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر توليد Hash: ${e.message.orEmpty()}"
                Toast.makeText(this, "تعذر توليد Hash", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClear.setOnClickListener {
            binding.etInput.text.clear()
            binding.etOutput.text = ""
        }
    }

    private fun showToolMode(checkedId: Int) {
        val isBase64 = checkedId == R.id.rbBase64
        val isHash = checkedId == R.id.rbHash

        binding.tvToolTitle.text = when {
            isBase64 -> "محول Base64"
            isHash -> "مولد Hash"
            else -> "منسق JSON"
        }
        binding.etInput.hint = when {
            isBase64 -> "ألصق النص أو Base64 هنا..."
            isHash -> "أدخل النص المراد توليد Hash له..."
            else -> "ألصق JSON هنا..."
        }

        // Keep the existing JSON and Base64 controls; show only actions for the selected tool.
        binding.btnFormat.visibility = if (isBase64 || isHash) View.GONE else View.VISIBLE
        binding.btnEncode.visibility = if (isBase64) View.VISIBLE else View.GONE
        binding.btnDecode.visibility = if (isBase64) View.VISIBLE else View.GONE
        binding.llBase64Actions.visibility = if (isBase64) View.VISIBLE else View.GONE
        binding.llHashActions.visibility = if (isHash) View.VISIBLE else View.GONE
        binding.btnClear.visibility = if (isHash) View.GONE else View.VISIBLE
        binding.tvOutputLabel.text = if (isHash) "نتائج Hash (قابلة للتحديد والنسخ):" else "النتيجة:"
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
