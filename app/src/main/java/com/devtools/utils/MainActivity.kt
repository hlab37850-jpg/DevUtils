package com.devtools.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.appcompat.app.AppCompatActivity
import com.devtools.utils.databinding.ActivityMainBinding
import com.devtools.utils.data.AppDatabase
import com.devtools.utils.data.HistoryEntity
import com.devtools.utils.tools.CaseConverterTool
import com.devtools.utils.tools.CommitMessageTool
import com.devtools.utils.tools.GitCommandConverterTool
import com.devtools.utils.tools.GitignoreGeneratorTool
import com.devtools.utils.tools.DateDiffCalculatorTool
import com.devtools.utils.tools.DuplicateRemoverTool
import com.devtools.utils.tools.HttpTool
import com.devtools.utils.tools.JwtTool
import com.devtools.utils.tools.MockDataTool
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
    private lateinit var database: AppDatabase
    private lateinit var exportSettingsLauncher: ActivityResultLauncher<String>
    private lateinit var importSettingsLauncher: ActivityResultLauncher<Array<String>>
    private val settingsPrefsName = "devutils_settings"
    private val settingsModeKey = "night_mode"

    private val regexTool = RegexTool()
    private val wordCounterTool = WordCounterTool()
    private val caseConverterTool = CaseConverterTool()
    private val duplicateRemoverTool = DuplicateRemoverTool()
    private val httpTool = HttpTool()
    private val jwtTool = JwtTool()
    private val mockDataTool = MockDataTool()
    private val commitMessageTool = CommitMessageTool()
    private val gitCommandConverterTool = GitCommandConverterTool()
    private val gitignoreGeneratorTool = GitignoreGeneratorTool()
    private val dateDiffCalculatorTool = DateDiffCalculatorTool()

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedMode = getSharedPreferences(settingsPrefsName, MODE_PRIVATE).getInt(settingsModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getInstance(this)
        exportSettingsLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) exportSettings(uri) }
        importSettingsLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) importSettings(uri) }
        setupUI()
        showToolMode(binding.rgMode.checkedRadioButtonId)
    }

    private fun setupUI() {
        binding.btnToggleTheme.setOnClickListener { toggleNightMode() }
        binding.btnHistory.setOnClickListener { showHistory() }
        binding.btnShareOutput.setOnClickListener { shareOutput() }
        binding.btnExportSettings.setOnClickListener { exportSettingsLauncher.launch("devutils-settings.json") }
        binding.btnImportSettings.setOnClickListener { importSettingsLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }
        updateThemeButton()
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
                saveHistory("JSON", inputJson, binding.etOutput.text.toString())
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
            saveHistory("Base64 Encode", input, encoded)
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
                saveHistory("Base64 Decode", input, binding.etOutput.text.toString())
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
                saveHistory("Hash", input, binding.etOutput.text.toString())
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
                saveHistory("URL Encode", input, binding.etOutput.text.toString())
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
                saveHistory("URL Decode", input, binding.etOutput.text.toString())
                toast("تم فك تشفير URL بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر فك تشفير URL: ${e.message.orEmpty()}"
                toast("نص URL غير صالح أو تعذر فك تشفيره")
            }
        }

        // Existing UUID generator and clipboard action.
        binding.btnGenerateUuid.setOnClickListener {
            binding.etOutput.text = UUID.randomUUID().toString()
            saveHistory("UUID", "", binding.etOutput.text.toString())
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
                saveHistory("Regex", "Pattern: $pattern\n$input", binding.etOutput.text.toString())
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


        // Phase 3: asynchronous HTTP requests; network I/O stays off the UI thread.
        binding.btnHttpSend.setOnClickListener {
            val url = binding.etHttpUrl.text.toString().trim()
            val method = if (binding.rgHttpMethod.checkedRadioButtonId == R.id.rbHttpPost) "POST" else "GET"
            val body = binding.etHttpBody.text.toString().takeIf { it.isNotBlank() }
            if (url.isBlank()) {
                toast("أدخل رابط URL أولاً")
                return@setOnClickListener
            }
            binding.btnHttpSend.isEnabled = false
            binding.btnHttpSend.text = "جارٍ الإرسال..."
            binding.etOutput.text = "جارٍ انتظار استجابة الخادم..."
            lifecycleScope.launch {
                try {
                    val result = suspendCancellableCoroutine<String> { continuation ->
                        httpTool.executeRequest(url, method, body) { response ->
                            if (continuation.isActive) continuation.resume(response)
                        }
                    }
                    binding.etOutput.text = result
                    if (!result.startsWith("خطأ الشبكة:")) saveHistory("HTTP", "$method $url\n${body.orEmpty()}", result)
                    if (result.startsWith("خطأ الشبكة:")) toast("تعذر الاتصال. تحقق من الإنترنت والرابط.")
                } catch (e: Exception) {
                    binding.etOutput.text = "خطأ الشبكة: ${e.message.orEmpty()}"
                    toast("تعذر الاتصال. تحقق من الإنترنت والرابط.")
                } finally {
                    binding.btnHttpSend.isEnabled = true
                    binding.btnHttpSend.text = "إرسال الطلب"
                }
            }
        }

        // JWT payload decoding only; the token signature is not verified.
        binding.btnJwtDecode.setOnClickListener {
            try {
                binding.etOutput.text = jwtTool.decodeJwt(binding.etJwtToken.text.toString().trim())
                saveHistory("JWT", binding.etJwtToken.text.toString().trim(), binding.etOutput.text.toString())
                toast("تم فك Payload بنجاح")
            } catch (e: Exception) {
                binding.etOutput.text = "تعذر تحليل JWT: ${e.message.orEmpty()}"
                toast("JWT غير صالح")
            }
        }

        binding.btnGenerateMock.setOnClickListener {
            binding.etOutput.text = mockDataTool.generateMockData()
            saveHistory("Mock Data", "", binding.etOutput.text.toString())
            toast("تم توليد بيانات تجريبية")
        }


        // Phase 4: Conventional Commit message generator.
        binding.btnGenerateCommit.setOnClickListener {
            try {
                binding.etOutput.text = commitMessageTool.generateCommitMessage(
                    type = binding.spCommitType.selectedItem.toString(),
                    scope = binding.etCommitScope.text.toString(),
                    description = binding.etCommitDescription.text.toString(),
                    body = binding.etCommitBody.text.toString()
                )
                saveHistory("Commit Message", "${binding.spCommitType.selectedItem} ${binding.etCommitScope.text} ${binding.etCommitDescription.text}\n${binding.etCommitBody.text}", binding.etOutput.text.toString())
                toast("تم توليد رسالة Commit")
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "تعذر توليد الرسالة: ${e.message.orEmpty()}"
                toast(e.message ?: "تحقق من بيانات Commit")
            } catch (e: Exception) {
                toast("حدث خطأ أثناء توليد رسالة Commit")
            }
        }

        binding.btnConvertGitCommand.setOnClickListener {
            try {
                binding.etOutput.text = gitCommandConverterTool.convertCommand(
                    command = binding.etGitCommand.text.toString(),
                    fromShell = binding.spShellFrom.selectedItem.toString(),
                    toShell = binding.spShellTo.selectedItem.toString()
                )
                saveHistory("Git Command", binding.etGitCommand.text.toString(), binding.etOutput.text.toString())
                toast("تم تحويل الأمر")
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "تعذر تحويل الأمر: ${e.message.orEmpty()}"
                toast(e.message ?: "تعذر تحويل الأمر")
            } catch (e: Exception) {
                toast("حدث خطأ أثناء تحويل الأمر")
            }
        }

        binding.btnGenerateGitignore.setOnClickListener {
            try {
                binding.etOutput.text = gitignoreGeneratorTool.generateGitignore(binding.spGitignoreType.selectedItem.toString())
                saveHistory("Gitignore", binding.spGitignoreType.selectedItem.toString(), binding.etOutput.text.toString())
                toast("تم توليد قالب .gitignore")
            } catch (e: IllegalArgumentException) {
                binding.etOutput.text = "تعذر توليد القالب: ${e.message.orEmpty()}"
                toast(e.message ?: "نوع مشروع غير صالح")
            }
        }
        binding.btnCopyGitignore.setOnClickListener {
            val text = binding.etOutput.text.toString()
            if (text.isBlank()) toast("ولّد قالب .gitignore أولاً") else copyToClipboard(text)
        }

        binding.btnCalculateDateDiff.setOnClickListener {
            try {
                val diff = dateDiffCalculatorTool.calculateDateDiff(
                    binding.etStartDate.text.toString().trim(),
                    binding.etEndDate.text.toString().trim()
                )
                binding.tvDiffYears.text = "السنوات: ${diff.getValue("years")}"
                binding.tvDiffMonths.text = "الأشهر: ${diff.getValue("months")}"
                binding.tvDiffDays.text = "الأيام: ${diff.getValue("days")}"
                binding.tvDiffTotalDays.text = "إجمالي الأيام: ${diff.getValue("totalDays")}"
                binding.etOutput.text = "السنوات: ${diff.getValue("years")}\nالأشهر: ${diff.getValue("months")}\nالأيام: ${diff.getValue("days")}\nإجمالي الأيام: ${diff.getValue("totalDays")}"
                saveHistory("Date Difference", "${binding.etStartDate.text} → ${binding.etEndDate.text}", binding.etOutput.text.toString())
                toast("تم حساب الفرق بين التاريخين")
            } catch (e: IllegalArgumentException) {
                toast(e.message ?: "تاريخ غير صالح. استخدم YYYY-MM-DD")
            } catch (e: Exception) {
                toast("تعذر حساب فرق التاريخين")
            }
        }

        // Phase 2: remove duplicate lines while preserving first occurrence order.
        binding.btnRemoveDuplicates.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isBlank()) {
                toast("أدخل النص أولاً")
                return@setOnClickListener
            }
            binding.etOutput.text = duplicateRemoverTool.execute(input)
            saveHistory("Duplicate Remover", input, binding.etOutput.text.toString())
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
        val isHttp = checkedId == R.id.rbHttp
        val isJwt = checkedId == R.id.rbJwt
        val isMockData = checkedId == R.id.rbMockData
        val isCommitMessage = checkedId == R.id.rbCommitMessage
        val isGitCommand = checkedId == R.id.rbGitCommand
        val isGitignore = checkedId == R.id.rbGitignore
        val isDateDiff = checkedId == R.id.rbDateDiff

        binding.tvToolTitle.text = when {
            isBase64 -> "محول Base64"
            isHash -> "مولد Hash"
            isUrl -> "محول URL"
            isUuid -> "مولد UUID"
            isRegex -> "مختبر Regex"
            isWordCounter -> "عداد الكلمات"
            isCaseConverter -> "محول النصوص"
            isDuplicateRemover -> "مزيل التكرارات"
            isHttp -> "عميل HTTP"
            isJwt -> "محلل JWT"
            isMockData -> "مولد البيانات الوهمية"
            isCommitMessage -> "مولد Commit"
            isGitCommand -> "محول Git Commands"
            isGitignore -> "مولد .gitignore"
            isDateDiff -> "حاسبة التواريخ"
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

        binding.etInput.visibility = if (isUuid || isHttp || isJwt || isMockData || isCommitMessage || isGitCommand || isGitignore || isDateDiff) View.GONE else View.VISIBLE
        binding.llCommitInputs.visibility = if (isCommitMessage) View.VISIBLE else View.GONE
        binding.llGitCommandInputs.visibility = if (isGitCommand) View.VISIBLE else View.GONE
        binding.llGitignoreInputs.visibility = if (isGitignore) View.VISIBLE else View.GONE
        binding.llDateDiffInputs.visibility = if (isDateDiff) View.VISIBLE else View.GONE
        binding.llHttpInputs.visibility = if (isHttp) View.VISIBLE else View.GONE
        binding.llJwtInputs.visibility = if (isJwt) View.VISIBLE else View.GONE
        binding.llMockActions.visibility = if (isMockData) View.VISIBLE else View.GONE
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
            isHttp -> "استجابة HTTP:"
            isJwt -> "Payload (لم يتم التحقق من التوقيع):"
            isMockData -> "البيانات المولدة:"
            isCommitMessage -> "رسالة Commit:"
            isGitCommand -> "الأمر المحوّل:"
            isGitignore -> "محتوى .gitignore:"
            isDateDiff -> "فرق التاريخين:"
            else -> "النتيجة:"
        }

        if (isWordCounter) updateWordStats(binding.etInput.text.toString())
    }

    private fun saveHistory(toolName: String, input: String, output: String) {
        if (output.isBlank() || output.startsWith("خطأ") || output.startsWith("تعذر")) return
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { database.historyDao().insert(HistoryEntity(toolName = toolName, input = input, output = output, timestamp = System.currentTimeMillis())) }
        }
    }

    private fun showHistory() {
        lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) { database.historyDao().getRecentHistory() }
            if (entries.isEmpty()) {
                AlertDialog.Builder(this@MainActivity).setTitle("سجل العمليات").setMessage("لا توجد عمليات محفوظة حتى الآن.").setPositiveButton("حسناً", null).show()
                return@launch
            }
            val labels = entries.map { entry -> "${entry.toolName} • ${entry.output.replace("\\n", " ").take(70)}" }.toTypedArray()
            AlertDialog.Builder(this@MainActivity).setTitle("آخر ${entries.size} عملية")
                .setItems(labels) { _, index ->
                    val entry = entries[index]
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("${entry.toolName} • ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(entry.timestamp))}")
                        .setMessage("المدخل:\\n${entry.input}\\n\\nالنتيجة:\\n${entry.output}")
                        .setPositiveButton("إغلاق", null)
                        .setNeutralButton("مشاركة") { _, _ -> shareText(entry.output) }.show()
                }
                .setNegativeButton("إغلاق", null)
                .setNeutralButton("مسح السجل") { _, _ -> lifecycleScope.launch(Dispatchers.IO) { database.historyDao().deleteAll() }; toast("تم مسح السجل") }
                .show()
        }
    }

    private fun shareOutput() {
        val output = binding.etOutput.text.toString()
        if (output.isBlank()) { toast("لا توجد نتيجة لمشاركتها"); return }
        shareText(output)
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
        startActivity(Intent.createChooser(intent, "مشاركة النتيجة"))
    }

    private fun toggleNightMode() {
        val prefs = getSharedPreferences(settingsPrefsName, MODE_PRIVATE)
        val current = prefs.getInt(settingsModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val next = when (current) { AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_YES; AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO; else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM }
        prefs.edit().putInt(settingsModeKey, next).apply()
        AppCompatDelegate.setDefaultNightMode(next)
        updateThemeButton()
    }

    private fun updateThemeButton() {
        val mode = getSharedPreferences(settingsPrefsName, MODE_PRIVATE).getInt(settingsModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding.btnToggleTheme.text = when (mode) { AppCompatDelegate.MODE_NIGHT_YES -> "☀ الوضع الداكن"; AppCompatDelegate.MODE_NIGHT_NO -> "☾ الوضع الفاتح"; else -> "◐ تلقائي" }
    }

    private fun exportSettings(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = getSharedPreferences(settingsPrefsName, MODE_PRIVATE)
            val json = JSONObject().put("app", "DevUtils").put("version", 1).put("nightMode", prefs.getInt(settingsModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)).toString(2)
            val success = runCatching { contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) } ?: error("تعذر فتح الملف للكتابة") }.isSuccess
            withContext(Dispatchers.Main) { toast(if (success) "تم تصدير الإعدادات" else "تعذر تصدير الإعدادات") }
        }
    }

    private fun importSettings(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val text = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: error("تعذر قراءة الملف")
                val json = JSONObject(text)
                require(json.optString("app") == "DevUtils") { "هذا الملف ليس إعدادات DevUtils" }
                val mode = json.optInt("nightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                require(mode == AppCompatDelegate.MODE_NIGHT_YES || mode == AppCompatDelegate.MODE_NIGHT_NO || mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) { "قيمة الوضع الليلي غير صالحة" }
                getSharedPreferences(settingsPrefsName, MODE_PRIVATE).edit().putInt(settingsModeKey, mode).apply()
                mode
            }
            withContext(Dispatchers.Main) { result.onSuccess { mode -> AppCompatDelegate.setDefaultNightMode(mode); updateThemeButton(); toast("تم استيراد الإعدادات") }.onFailure { error -> toast(error.message ?: "تعذر استيراد الإعدادات") } }
        }
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
            saveHistory("Case Converter", "$mode\n$input", binding.etOutput.text.toString())
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
