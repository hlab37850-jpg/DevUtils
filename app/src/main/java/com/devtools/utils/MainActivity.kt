package com.devtools.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        showToolMode(binding.rgMode.checkedRadioButtonId)
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

        // Existing Hash generator retained: MD5, SHA-1, SHA-256.
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

        // URL encoder.
        binding.btnUrlEncode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص لتشفير URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                binding.etOutput.text = URLEncoder.encode(input, StandardCharsets.UTF_8.name())
                Toast.makeText(this, "تم تشفير URL بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر تشفير URL: ${e.message.orEmpty()}"
                Toast.makeText(this, "حدث خطأ أثناء تشفير URL", Toast.LENGTH_SHORT).show()
            }
        }

        // URL decoder.
        binding.btnUrlDecode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(this, "الرجاء إدخال نص URL لفك التشفير", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                binding.etOutput.text = URLDecoder.decode(input, StandardCharsets.UTF_8.name())
                Toast.makeText(this, "تم فك تشفير URL بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر فك تشفير URL: ${e.message.orEmpty()}"
                Toast.makeText(this, "نص URL غير صالح أو تعذر فك تشفيره", Toast.LENGTH_SHORT).show()
            }
        }

        // UUID generator.
        binding.btnGenerateUuid.setOnClickListener {
            binding.etOutput.text = UUID.randomUUID().toString()
            Toast.makeText(this, "تم توليد UUID جديد", Toast.LENGTH_SHORT).show()
        }

        binding.btnCopyUuid.setOnClickListener {
            val uuid = binding.etOutput.text.toString().trim()
            if (uuid.isBlank()) {
                Toast.makeText(this, "ولّد UUID أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            copyToClipboard(uuid)
        }

        binding.btnClear.setOnClickListener {
            binding.etInput.text.clear()
            binding.etOutput.text = ""
        }
    }

    private fun showToolMode(checkedId: Int) {
        val isJson = checkedId == R.id.rbJson
        val isBase64 = checkedId == R.id.rbBase64
        val isHash = checkedId == R.id.rbHash
        val isUrl = checkedId == R.id.rbUrl
        val isUuid = checkedId == R.id.rbUuid

        binding.tvToolTitle.text = when {
            isBase64 -> "محول Base64"
            isHash -> "مولد Hash"
            isUrl -> "محول URL"
            isUuid -> "مولد UUID"
            else -> "منسق JSON"
        }
        binding.etInput.hint = when {
            isBase64 -> "ألصق النص أو Base64 هنا..."
            isHash -> "أدخل النص المراد توليد Hash له..."
            isUrl -> "أدخل النص المراد تشفيره أو فك تشفيره..."
            isUuid -> ""
            else -> "ألصق JSON هنا..."
        }

        binding.etInput.visibility = if (isUuid) View.GONE else View.VISIBLE
        binding.btnFormat.visibility = if (isJson) View.VISIBLE else View.GONE
        binding.llJsonActions.visibility = if (isJson || isBase64) View.VISIBLE else View.GONE
        binding.btnClear.visibility = if (isHash || isUuid) View.GONE else View.VISIBLE
        binding.llBase64Actions.visibility = if (isBase64) View.VISIBLE else View.GONE
        binding.llHashActions.visibility = if (isHash) View.VISIBLE else View.GONE
        binding.llUrlActions.visibility = if (isUrl) View.VISIBLE else View.GONE
        binding.llUuidActions.visibility = if (isUuid) View.VISIBLE else View.GONE
        binding.tvOutputLabel.text = when {
            isHash -> "نتائج Hash (قابلة للتحديد والنسخ):"
            isUuid -> "UUID (قابل للتحديد والنسخ):"
            isUrl -> "نتيجة URL:"
            else -> "النتيجة:"
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("DevUtils", text))
        Toast.makeText(this, "تم النسخ إلى الحافظة", Toast.LENGTH_SHORT).show()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
