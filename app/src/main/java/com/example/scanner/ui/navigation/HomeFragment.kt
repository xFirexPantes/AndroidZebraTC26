package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.scanner.R
import com.example.scanner.app.floatDisable
import com.example.scanner.app.floatEnable
import com.example.scanner.databinding.FragmentHomeBinding
import com.example.scanner.modules.Other
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.InvoiceMenuFragment.Companion.PARAM_STEP_1_VALUE
import com.example.scanner.ui.navigation.ReceiveFragment.Companion.EXTRA_RGM
import com.example.scanner.ui.navigation.login.LoginFragment
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_over.ProgressFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
private const val REQUEST_INSTALL_PERMISSION = 1001

class HomeFragment : BaseFragment() {
    private val handler = Handler(Looper.getMainLooper())
    private val homeViewModel: HomeViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private var networkPath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.loginInit()
        Other.getInstanceSingleton().ioCoroutineScope.launch {
            scanViewModel.mainActivityRouter.navigate(
                ProgressFragment::class.java
            )
            scanViewModel.mainActivityRouter.navigate(
                TransparentFragment::class.java
            )
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return FragmentHomeBinding.inflate(inflater, container, false)
        .apply {
            homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner) {
                when (val state = it) {
                    is HomeFragmentFormState.SetView -> {
                        userViewText.text = state.username

                        fun forbiddenToast() {
                            Toast.makeText(
                                requireContext(), "Доступ к данному разделу запрещен",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        issuance.button.alpha =
                            if (state.issuance) {
                                issuance.button.setOnClickListener {
                                    homeViewModel.mainActivityRouter.navigate(
                                        InvoiceMenuFragment::class.java,
                                        Bundle().apply { putSerializable(PARAM_STEP_1_VALUE, "") }
                                    )
                                }
                                floatEnable
                            } else {
                                issuance.button.setOnClickListener { forbiddenToast() }
                                floatDisable
                            }

                        search.button.alpha =
                            if (state.search) {
                                search.button.setOnClickListener {
                                    homeViewModel.mainActivityRouter.navigate(
                                        ComponentFragment::class.java,
                                        Bundle().apply {
                                            putSerializable(
                                                ComponentFragment.PARAM,
                                                ""
                                            )
                                        }
                                    )
                                }
                                floatEnable
                            } else {
                                search.button.setOnClickListener { forbiddenToast() }
                                floatDisable
                            }

                        isolator.button.alpha =
                            if (state.isolator) {
                                isolator.button.setOnClickListener {
                                    //homeViewModel.mainActivityRouter.navigate(ScanIsolatorFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        IsolatorFragment::class.java,
                                        Bundle().apply {
                                            putSerializable(
                                                IsolatorFragment.PARAM,
                                                ""
                                            )
                                        }
                                    )
                                }
                                floatEnable
                            } else {
                                isolator.button.setOnClickListener { forbiddenToast() }
                                floatDisable
                            }


                        accept.button.alpha =
                            if (state.accept) {
                                accept.button.setOnClickListener {
                                    //                            homeViewModel.mainActivityRouter.navigate(
                                    //                                ScanReceiveFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        ReceiveFragment::class.java,
                                        Bundle().apply {
                                            putSerializable(PARAM_STEP_1_VALUE, "")   // если нужно
                                            putSerializable(
                                                EXTRA_RGM,
                                                "first"
                                            )              // или putSerializable
                                        }
                                    )
                                }
                                floatEnable
                            } else {
                                accept.button.setOnClickListener { forbiddenToast() }
                                floatDisable
                            }
                        incontrol.button.alpha =
                            if (state.incontrol or state.accept) {
                                incontrol.button.setOnClickListener {
                                    //                            homeViewModel.mainActivityRouter.navigate(
                                    //                                ScanReceiveFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        InControlMenuFragment::class.java,
                                        Bundle().apply {
                                            putSerializable(
                                                InControlMenuFragment.PARAM_STEP_1_VALUE,
                                                ""
                                            )
                                        }
                                    )
                                }
                                floatEnable
                            } else {
                                incontrol.button.setOnClickListener { forbiddenToast() }
                                floatDisable
                            }
                        update.button.alpha = if (state.update) {
                            update.button.setOnClickListener {
                                checkForUpdates() // Вызываем проверку обновлений
                            }
                            floatEnable
                        } else {
                            update.button.setOnClickListener { forbiddenToast() }
                            floatDisable
                        }

                        admin.button.visibility = if (state.admin) {
                            admin.button.setOnClickListener {
                                homeViewModel.mainActivityRouter.navigate(
                                    AdminFragment::class.java,
                                    Bundle().apply {
                                        putSerializable(
                                            "",
                                            ""
                                        )
                                    } // Обратите внимание: ключ пустой!
                                )
                            }
                            View.VISIBLE
                        } else {
                            admin.button.setOnClickListener { forbiddenToast() }
                            View.GONE // или View.INVISIBLE — см. пояснение ниже
                        }





                        userViewContainer.setOnClickListener {
                            PopupMenu(requireContext(), it)
                                .apply {
                                    setOnMenuItemClickListener { item ->
                                        when (item.itemId) {
                                            R.id.menu_logout -> {
                                                homeViewModel.loginRepository.logout()
                                                homeViewModel.mainActivityRouter.navigate(
                                                    LoginFragment::class.java
                                                )
                                            }
                                        }
                                        true
                                    }
                                    inflate(R.menu.actions_user)
                                    show()
                                }
                        }
                        homeViewModel.batch()

                    }
                }
            }


        }
        .root

    }

    @SuppressLint("SetTextI18n")
    private fun startcheck(versionTextView: TextView, btn: Button) {
        lifecycleScope.launch {
            try {
                // 1. Проверяем подключение
                if (!isNetworkAvailable()) {
                    versionTextView.text = "Версия: ${getCurrentVersion()} (нет интернета)"
                    return@launch
                }

                // 2. Выполняем запрос в фоновом потоке
                val isUpdateNeeded = withContext(Dispatchers.IO) {
                    val url = URL("http://192.168.5.125/txrw/version.txt")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val latestVersion = connection.inputStream.bufferedReader().use { it.readLine() }
                    val currentVersion = getCurrentVersion()

                    latestVersion != null && latestVersion != currentVersion
                }

                // 3. Обновляем TextView в UI-потоке
                val currentVersion = getCurrentVersion()
                versionTextView.text = "Версия: $currentVersion"
                if (isUpdateNeeded) {
                    btn.setBackgroundColor(Color.argb(255, 0, 255, 0))
                    versionTextView.setBackgroundColor(Color.argb(255, 0, 255, 0))
                }

            } catch (e: Exception) {
                Timber.tag("UpdateChecker").e(e, "Ошибка проверки обновлений")
                versionTextView.text = "Версия: ${getCurrentVersion()} (ошибка проверки)"
            }
        }
    }

    // Вспомогательный метод для получения текущей версии
    private fun getCurrentVersion(): String {
        return try {
            requireActivity().packageManager
                .getPackageInfo(requireActivity().packageName, 0)
                .versionName.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "не найдена"
        }
    }
    private fun checkForUpdates() {
        val networkPath = "http://192.168.5.125/txrw" // URL сервера с обновлениями
        val url = URL("$networkPath/version.txt")

        Thread {
            try {
                if (!isNetworkAvailable()) {
                    handler.post {
                        showToast("Нет подключения к интернету")
                    }

                    return@Thread
                }


                val reader = BufferedReader(InputStreamReader(url.openStream()))
                val latestVersion = reader.readLine()

                if (latestVersion != requireActivity().packageManager.getPackageInfo(
                        requireActivity().packageName, 0
                    ).versionName) {

                    Handler(Looper.getMainLooper()).post {
                        showUpdateDialog(networkPath)
                    }

                } else {

                    handler.post {
                        showToast("Нет обновлений")
                    }
                }
            } catch (e: Exception) {
                Timber.tag("UpdateChecker").e(e, "Ошибка проверки обновлений: ${e.message}")
                handler.post {
                    showToast("Ошибка при проверке обновлений")
                }
            } finally {

            }
        }.start()
    }

    private fun showUpdateDialog(networkPath: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Доступно обновление")
            .setMessage("Желаете установить новую версию?")
            .setPositiveButton("Да") { _, _ -> downloadAndInstallUpdate(networkPath) }
            .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Пользователь дал разрешение — продолжаем загрузку
            downloadAndInstallUpdateInternal(networkPath)
        } else {
            handler.post {
                showToast("Разрешение не получено")
            }

        }
    }
    private fun isInstallPermissionGranted(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 30 ->
                requireActivity().packageManager.canRequestPackageInstalls()
            Build.VERSION.SDK_INT == 29 ->
                true  // В Android 10 считаем, что разрешено (или проверяем иначе)
            else ->
                true  // Для старых версий разрешение не требуется
        }
    }

    private fun downloadAndInstallUpdate(networkPath: String) {
        this.networkPath = networkPath

        if (!isInstallPermissionGranted()) {
            // Показываем настройки только если разрешения НЕТ
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${requireActivity().packageName}")
            )
            installPermissionLauncher.launch(intent)
            return
        }

        // Разрешение есть → начинаем загрузку
        downloadAndInstallUpdateInternal(networkPath)
    }
    // Фоновая загрузка файла
    private suspend fun downloadFile(urlString: String, fileSize: Long): File {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.connect()

            val downloadDir = File(requireActivity().filesDir, "downloads").apply {
                if (!exists()) mkdirs()
            }
            val apkFile = File(downloadDir, "update.apk")

            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead: Long = 0

                    while (input.read(buffer).also { bytesRead = it } > 0) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        val progress = (totalBytesRead * 100 / fileSize).toInt()
                        updateProgress(progress)
                    }
                }
            }
            apkFile
        }
    }

    private fun downloadAndInstallUpdateInternal(networkPath: String) = lifecycleScope.launch {
        try {
            val fileSize = withContext(Dispatchers.IO) {
                val url = URL("$networkPath/trxw.apk")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.connect()

                val size = connection.contentLengthLong
                if (size <= 0) throw IOException("Неверный размер файла")
                size
            }

            val apkFile = withContext(Dispatchers.IO) {
                downloadFile("$networkPath/trxw.apk", fileSize)
            }

            installApk(apkFile)

        } catch (e: IOException) {
            handler.post {
                showToast("Ошибка сети: ${e.message}")
            }

        } catch (e: Exception) {
            handler.post {
                showToast("Ошибка: ${e.message}")
            }

        }
    }
    // Установка APK
    private fun installApk(apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val authority = "${requireActivity().packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(
                requireContext(),
                authority,
                apkFile
            )
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.fromFile(apkFile),
                "application/vnd.android.package-archive"
            )
        }

        startActivity(intent)
    }

    // Обработка результата разрешения
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 30) {
                if (requireActivity().packageManager.canRequestPackageInstalls()) {
                    // Повторный запуск загрузки после получения разрешения

                } else {
                    handler.post {
                        showToast("Разрешение не получено")
                    }

                }
            } else {
                // Для SDK 29 считаем, что пользователь сам включит
                handler.post {
                    showToast("Включите «Неизвестные источники» в настройках")
                }

            }
        }
    }

    private fun updateProgress(progress: Int) {
        handler.post {
            Toast.makeText(requireContext(), "Загрузка: $progress%", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    class HomeViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

        companion object {
            private var _HomeViewModel: WeakReference<HomeViewModel>? = null
            fun getInstanceSingleton(app: Context): HomeViewModel {
                return _HomeViewModel
                    ?.get()
                    ?:run {
                        HomeViewModel(LoginRepository.getInstanceSingleton(app))
                            .apply { _HomeViewModel = WeakReference(this) }
                    }
            }
        }

        val homeFragmentFormState = MutableLiveData<HomeFragmentFormState<*>>()



        private var array=arrayOf(
//            Pair(
//                InvoiceFragmentLines::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentLines.PARAMS_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentLines.PARAMS1_INVOICE_NAME,"93880")
//                    putSerializable(InvoiceFragmentLines.PARAMS2_COLLECTED,false)
//                }
//            ),
//            Pair(
//                InvoiceFragmentLines::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentLines.PARAMS_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentLines.PARAMS1_INVOICE_NAME,"93880-0")
//                    putSerializable(InvoiceFragmentLines.PARAMS2_COLLECTED,true)
//                }
//            ),
//            Pair(
//                InvoiceFragment::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragment.PARAM,"93880")
//                }
//            ),
//            Pair(
//                ReceiveFragment::class.java,
//                Bundle().apply {
//
//                }
//            ),
//            Pair(
//                ComponentFragment::class.java,
//                Bundle().apply {
//                    putSerializable(IsolatorFragment.PARAM,"01")
//                }
//            ),
            Pair(
                ErrorsFragment::class.java,
                Bundle().apply {
                    putSerializable(ErrorsFragment.PARAM, Exception("Test Exception"))
                }
            ),
//            Pair(
//                IsolatorFragment::class.java,
//                Bundle().apply {
//                    putSerializable(IsolatorFragment.PARAM,"01")
//                }
//            ),
//            Pair(
//                InvoiceFragmentInfoLine::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_LINE_ID,1395758)
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_LINE_COLLECTED,false)
//                }
//            ),

        )


        fun batch(){
            array=arrayOf()
            array.forEach {
                Other.getInstanceSingleton().mainCoroutineScope.launch {
                    mainActivityRouter.navigate(it.first,it.second)
                    Thread.sleep(1000)
                }
            }
            array=arrayOf()
        }

        fun loginInit() {
            if (loginRepository.isLoggedIn)
                loginRepository.pref.getResource().let { resources: Resources ->
                    loginRepository.user?.let {
                        homeFragmentFormState.value= HomeFragmentFormState.SetView(
                            username = it.name ?: resources.getString(android.R.string.unknownName),
//                            accept = true,
//                            issuance = true,
//                            search = true,
//                            isolator = true
                            accept = it.access.accept,
                            issuance = it.access.issuance,
                            search = it.access.search,
                            isolator = it.access.isolator,
                            incontrol = it.access.incontrol,
                            update = it.access.update,
                            admin = it.access.admin
                        )
                    }
                }
            else
                mainActivityRouter.navigate(LoginFragment::class.java)

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val versionTextView: TextView = view.findViewById(R.id.textVersion)
        val btn: Button = view.findViewById(R.id.update)

        // Сразу запускаем проверку обновлений
        startcheck(versionTextView,btn)
    }

}