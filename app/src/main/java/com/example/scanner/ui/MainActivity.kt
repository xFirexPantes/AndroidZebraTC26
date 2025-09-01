package com.example.scanner.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.scanner.R
import com.example.scanner.databinding.ActivityMainBinding
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseActivity
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.ComponentFragment
import com.example.scanner.ui.navigation.ComponentFragmentInfo
import com.example.scanner.ui.navigation.InvoiceFragment
import com.example.scanner.ui.navigation.HomeFragment
import com.example.scanner.ui.navigation.InvoiceFragmentInfo
import com.example.scanner.ui.navigation.InvoiceFragmentInfoLine
import com.example.scanner.ui.navigation.InvoiceFragmentLines
import com.example.scanner.ui.navigation.IsolatorFragment
import com.example.scanner.ui.navigation.IsolatorFragmentIsolate
import com.example.scanner.ui.navigation.IsolatorFragmentMinus
import com.example.scanner.ui.navigation.login.LoginFragment
import com.example.scanner.ui.navigation_setting.LogsFragment
import com.example.scanner.ui.navigation_over.ProgressFragment
import com.example.scanner.ui.navigation.ReceiveFragment
import com.example.scanner.ui.navigation.ReceiveFragmentInfo
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import com.example.scanner.ui.navigation_setting.SettingFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class MainActivity : BaseActivity() {

    companion object{
        private var _mainActivity:WeakReference<MainActivity>?=null
        fun getInstanceSingleton(): MainActivity?{
            return _mainActivity?.get()
        }
    }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var navControllerDrawer:NavController?=null
    private var navControllerMain:NavController?=null
    private var navControllerMainOver:NavController?=null
    lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        _mainActivity =WeakReference(this)

        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navControllerMain = findNavController(R.id.nav_host_fragment_main)
        navControllerMainOver = findNavController(R.id.nav_host_fragment_main_over)
        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {
                scanViewModel.mainActivityRouter.navigate(
                    SettingFragment::class.java
                )
            }
        })

        setupActionBarWithNavController(navControllerMain!!, appBarConfiguration)

        navView.setupWithNavController(navControllerMain!!)

        scanViewModel.mainActivityRouter.mainActivityFormState.observe(this) { route ->

            when (route?.kClass) {

                //region Home
                HomeFragment::class.java ->
                    navControllerMain?.navigate(R.id.action_nav_login_fragment_to_nav_home)
                //endregion

                //region Invoice

                InvoiceFragment::class.java ->
                    navControllerMain?.navigate(R.id.nav_invoices, route.params)
                //endregion

                //region InvoiceInfo
                InvoiceFragmentInfo::class.java ->
                    navControllerMain?.navigate(R.id.nav_invoice_info_fragment, route.params)
                //endregion

                //region InvoiceLines
                InvoiceFragmentLines::class.java ->
                    navControllerMain?.navigate(R.id.nav_invoice_lines_fragment, route.params)
                //endregion
                //region Invoice Line Info
                InvoiceFragmentInfoLine::class.java ->
                    navControllerMain?.navigate(R.id.nav_invoice_line_info_fragment, route.params)
                //endregion

                //region Components

                ComponentFragment::class.java ->
                    navControllerMain?.navigate(R.id.nav_components_fragment, route.params)
                //endregion

                //region Components Info
                ComponentFragmentInfo::class.java ->
                    navControllerMain?.navigate(R.id.nav_components_info_fragment, route.params)
                //endregion

                //region LoginFragment
                LoginFragment::class.java ->
                    navControllerMain?.navigate(R.id.action_global_nav_login_fragment)
                //endregion

                //region Settings
                LogsFragment::class.java ->
                    navControllerDrawer?.navigate(R.id.nav_logs)

                SettingFragment::class.java ->
                    navControllerDrawer?.navigate(R.id.action_global_nav_setting)

                //endregion

                //region IsolatorFragment

                IsolatorFragment::class.java ->
                    navControllerMain?.navigate(R.id.nav_isolator, route.params)

                IsolatorFragmentIsolate::class.java ->
                    navControllerMain?.navigate(R.id.nav_isolator_isolate_fragment, route.params)

                IsolatorFragmentMinus::class.java ->
                    navControllerMain?.navigate(R.id.nav_isolator_minus_fragment, route.params)
                //endregion

                //region ReceiveFragment

                ReceiveFragment::class.java ->
                    navControllerMain?.navigate(R.id.nav_receive, route.params)

                ReceiveFragmentInfo::class.java ->
                    navControllerMain?.navigate(R.id.nav_receive_info_fragment, route.params)

                //endregion

                //region ProgressFragment
                ProgressFragment::class.java ->
                    navControllerMainOver?.navigate(R.id.action_global_progress_fragment)
                //endregion

                //region TransparentFragment
                TransparentFragment::class.java ->
                    navControllerMainOver?.navigate(R.id.action_global_transparent_fragment,route.params)
                //endregion

                //region ErrorsFragment
                ErrorsFragment::class.java ->
                    navControllerMainOver?.navigate(R.id.errorsFragment, route.params)
                //endregion
            }

            scanViewModel.mainActivityRouter.clear()

        }

        binding.root.post {
            navControllerDrawer = findNavController(R.id.nav_host_fragment_setting)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun finish() {
        navControllerMain?.previousBackStackEntry
            ?.let { navControllerMain?.navigateUp() }
            ?:run { super.finish() }
    }

    data class MainActivityFormState<T: Fragment>(val kClass:Class<T>?, val params: Bundle?)

    class MainActivityRouter {

        companion object {
            private lateinit var mainActivityRouter: MainActivityRouter
            fun getInstanceSingleton(): MainActivityRouter {

                if (!Companion::mainActivityRouter.isInitialized)
                    mainActivityRouter = MainActivityRouter()

                return mainActivityRouter
            }
        }

        val mainActivityFormState=
            MutableLiveData<MainActivityFormState<*>?>()

        fun <T: Fragment>navigate(kClass:Class<T>, params:Bundle?=null): Job {
            return Other.getInstanceSingleton().mainCoroutineScope.launch {
                mainActivityFormState.value=
                    MainActivityFormState(kClass, params)
            }
        }

        fun clear(){
            mainActivityFormState.value?.let { mainActivityFormState.value=null }
        }

    }
}





