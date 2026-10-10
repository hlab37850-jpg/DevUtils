package com.devtools.utils

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
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
        binding.btnClear.setOnClickListener {
            binding.etInput.text.clear()
            binding.etOutput.text = ""
        }
    }
}
