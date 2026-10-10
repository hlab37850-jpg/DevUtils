package com.devtools.utils

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import org.json.JSONObject
import java.nio.charset.StandardCharsets

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
            showBase64Mode(checkedId == R.id.rbBase64)
        }

        // JSON Formatter logic is intentionally retained.
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

        binding.btnClear.setOnClickListener {
            binding.etInput.text.clear()
            binding.etOutput.text = ""
        }
    }

    private fun showBase64Mode(isBase64: Boolean) {
        binding.tvToolTitle.text = if (isBase64) "محول Base64" else "منسق JSON"
        binding.etInput.hint = if (isBase64) {
            "ألصق النص أو Base64 هنا..."
        } else {
            "ألصق JSON هنا..."
        }
        binding.btnFormat.visibility = if (isBase64) View.GONE else View.VISIBLE
        binding.llBase64Actions.visibility = if (isBase64) View.VISIBLE else View.GONE
    }
}
