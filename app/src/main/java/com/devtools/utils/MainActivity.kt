package com.devtools.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import com.devtools.utils.tools.CaseConverterTool
import com.devtools.utils.tools.DuplicateRemoverTool
import com.devtools.utils.tools.RegexTool
import com.devtools.utils.tools.WordCounterTool
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val regexTool = RegexTool()
    private val wordCounterTool = WordCounterTool()
    private val caseConverterTool = CaseConverterTool()
    private val duplicateRemoverTool = DuplicateRemoverTool()

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

        // Existing JSON formatter.
        binding.btnFormat.setOnClickListener {
            val inputJson = binding.etInput.text.toString()
            if (inputJson.isBlank()) {
                toast("الرجاء إدخال نص JSON")
                return@setOnClickListener
            }
            try {
                val jsonObject = JSONObject(inputJson)
                binding.etOutput.text = jsonObject.toString(4)
                toast("تم التنسيق بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "خطأ في تنسيق JSON: \n${e.message}"
                toast("نص JSON غير صالح")
            }
        }

        // Existing Base64 encoder.
        binding.btnEncode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("الرجاء إدخال نص للتشفير")
                return@setOnClickListener
            }
            val encoded = Base64.encodeToString(
                input.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
            binding.etOutput.text = encoded
            toast("تم التشفير بنجاح")
        }

        // Existing Base64 decoder.
        binding.btnDecode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("الرجاء إدخال نص Base64 لفك التشفير")
                return@setOnClickListener
            }
            try {
                val decodedBytes = Base64.decode(input, Base64.DEFAULT)
                binding.etOutput.text = String(decodedBytes, StandardCharsets.UTF_8)
                toast("تم فك التشفير بنجاح")
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "خطأ: نص Base64 غير صالح."
                toast("نص Base64 غير صالح")
            }
        }

        // Existing Hash generator: MD5, SHA-1, SHA-256.
        binding.btnGenerateHash.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("الرجاء إدخال نص")
                return@setOnClickListener
            }
            try {
                val bytes = input.toByteArray(StandardCharsets.UTF_8)
                val md5 = MessageDigest.getInstance("MD5").digest(bytes).toHex()
                val sha1 = MessageDigest.getInstance("SHA-1").digest(bytes).toHex()
                val sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()
                binding.etOutput.text = "MD5:\n$md5\n\nSHA-1:\n$sha1\n\nSHA-256:\n$sha256"
                toast("تم توليد Hash بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر توليد Hash: ${e.message.orEmpty()}"
                toast("تعذر توليد Hash")
            }
        }

        // Existing URL encoder.
        binding.btnUrlEncode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("الرجاء إدخال نص لتشفير URL")
                return@setOnClickListener
            }
            try {
                binding.etOutput.text = URLEncoder.encode(input, StandardCharsets.UTF_8.name())
                toast("تم تشفير URL بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر تشفير URL: ${e.message.orEmpty()}"
                toast("حدث خطأ أثناء تشفير URL")
            }
        }

        // Existing URL decoder.
        binding.btnUrlDecode.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("الرجاء إدخال نص URL لفك التشفير")
                return@setOnClickListener
            }
            try {
                binding.etOutput.text = URLDecoder.decode(input, StandardCharsets.UTF_8.name())
                toast("تم فك تشفير URL بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر فك تشفير URL: ${e.message.orEmpty()}"
                toast("نص URL غير صالح أو تعذر فك تشفيره")
            }
        }

        // Existing UUID generator and clipboard action.
        binding.btnGenerateUuid.setOnClickListener {
            binding.etOutput.text = UUID.randomUUID().toString()
            toast("تم توليد UUID جديد")
        }
        binding.btnCopyUuid.setOnClickListener {
            val uuid = binding.etOutput.text.toString().trim()
            if (uuid.isBlank()) {
                toast("ولّد UUID أولاً")
                return@setOnClickListener
            }
            copyToClipboard(uuid)
        }

        // Phase 2: Regex tester.
        binding.btnRegexTest.setOnClickListener {
            val input = binding.etInput.text.toString()
            val pattern = binding.etRegexPattern.text.toString()
            try {
                binding.etOutput.text = regexTool.execute(input, pattern)
                toast("اكتمل اختبار Regex")
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "خطأ في نمط Regex: ${e.message.orEmpty()}"
                toast("نمط Regex غير صالح أو فارغ")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر تنفيذ Regex: ${e.message.orEmpty()}"
                toast("حدث خطأ أثناء اختبار Regex")
            }
        }

        // Phase 2: word statistics update live as the user types.
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (binding.rgMode.checkedRadioButtonId == R.id.rbWordCounter) {
                    updateWordStats(s?.toString().orEmpty())
                }
            }
        })

        // Phase 2: case conversion buttons.
        binding.btnCamelCase.setOnClickListener { convertCase(CaseConverterTool.CAMEL_CASE) }
        binding.btnSnakeCase.setOnClickListener { convertCase(CaseConverterTool.SNAKE_CASE) }
        binding.btnPascalCase.setOnClickListener { convertCase(CaseConverterTool.PASCAL_CASE) }

        // Phase 2: remove duplicate lines while preserving first occurrence order.
        binding.btnRemoveDuplicates.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("أدخل النص أولاً")
                return@setOnClickListener
            }
            binding.etOutput.text = duplicateRemoverTool.execute(input)
            toast("تمت إزالة الأسطر المكررة")
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
        val isRegex = checkedId == R.id.rbRegex
        val isWordCounter = checkedId == R.id.rbWordCounter
        val isCaseConverter = checkedId == R.id.rbCaseConverter
        val isDuplicateRemover = checkedId == R.id.rbDuplicateRemover

        binding.tvToolTitle.text = when {
            isBase64 -> "محول Base64"
            isHash -> "مولد Hash"
            isUrl -> "محول URL"
            isUuid -> "مولد UUID"
            isRegex -> "مختبر Regex"
            isWordCounter -> "عداد الكلمات"
            isCaseConverter -> "محول النصوص"
            isDuplicateRemover -> "مزيل التكرارات"
            else -> "منسق JSON"
        }

        binding.etInput.hint = when {
            isBase64 -> "ألصق النص أو Base64 هنا..."
            isHash -> "أدخل النص المراد توليد Hash له..."
            isUrl -> "أدخل النص المراد تشفيره أو فك تشفيره..."
            isUuid -> ""
            isRegex -> "أدخل النص المراد البحث عن تطابقات داخله..."
            isWordCounter -> "اكتب أو ألصق النص لحساب الإحصائيات..."
            isCaseConverter -> "أدخل النص المراد تحويل تنسيقه..."
            isDuplicateRemover -> "أدخل النص، سطرًا في كل مرة..."
            else -> "ألصق JSON هنا..."
        }

        binding.etInput.visibility = if (isUuid) View.GONE else View.VISIBLE
        binding.etRegexPattern.visibility = if (isRegex) View.VISIBLE else View.GONE
        binding.btnFormat.visibility = if (isJson) View.VISIBLE else View.GONE
        binding.llJsonActions.visibility = if (isJson || isBase64) View.VISIBLE else View.GONE
        binding.btnClear.visibility = if (isJson || isBase64) View.VISIBLE else View.GONE
        binding.llBase64Actions.visibility = if (isBase64) View.VISIBLE else View.GONE
        binding.llHashActions.visibility = if (isHash) View.VISIBLE else View.GONE
        binding.llUrlActions.visibility = if (isUrl) View.VISIBLE else View.GONE
        binding.llUuidActions.visibility = if (isUuid) View.VISIBLE else View.GONE
        binding.llRegexActions.visibility = if (isRegex) View.VISIBLE else View.GONE
        binding.llWordStats.visibility = if (isWordCounter) View.VISIBLE else View.GONE
        binding.llCaseActions.visibility = if (isCaseConverter) View.VISIBLE else View.GONE
        binding.llDuplicateActions.visibility = if (isDuplicateRemover) View.VISIBLE else View.GONE
        binding.svOutput.visibility = if (isWordCounter) View.GONE else View.VISIBLE
        binding.tvOutputLabel.visibility = if (isWordCounter) View.GONE else View.VISIBLE

        binding.tvOutputLabel.text = when {
            isHash -> "نتائج Hash (قابلة للتحديد والنسخ):"
            isUuid -> "UUID (قابل للتحديد والنسخ):"
            isUrl -> "نتيجة URL:"
            isRegex -> "نتائج التطابق:"
            isCaseConverter -> "النص المحوّل:"
            isDuplicateRemover -> "النص بعد إزالة التكرارات:"
            else -> "النتيجة:"
        }

        if (isWordCounter) updateWordStats(binding.etInput.text.toString())
    }

    private fun updateWordStats(input: String) {
        val stats = wordCounterTool.execute(input)
        binding.tvWordCount.text = "عدد الكلمات: ${stats.words}"
        binding.tvCharCount.text = "عدد الأحرف: ${stats.characters}"
        binding.tvLineCount.text = "عدد الأسطر: ${stats.lines}"
        binding.tvParagraphCount.text = "عدد الفقرات: ${stats.paragraphs}"
    }

    private fun convertCase(mode: String) {
        val input = binding.etInput.text.toString()
        if (input.isBlank()) {
            toast("أدخل النص المراد تحويله أولاً")
            return
        }
        try {
            binding.etOutput.text = caseConverterTool.execute(input, mode)
            toast("تم تحويل النص بنجاح")
        } catch (e: Exception) {
            binding.etOutput.text = "تعذر تحويل النص: ${e.message.orEmpty()}"
            toast("حدث خطأ أثناء تحويل النص")
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("DevUtils", text))
        toast("تم النسخ إلى الحافظة")
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
