package com.example.scanner.modules

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.View
import com.google.gson.Gson
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.example.scanner.R
import com.example.scanner.models.LoggedInUserResponse
import java.lang.ref.WeakReference

class Pref(context: Context,private val gson: Gson) {

    companion object{
        private var _pref: WeakReference<Pref>? = null
        fun getInstanceSingleton(context: Context): Pref {
            return _pref
                ?.get()
                ?:run {
                    Pref(context,Other.getInstanceSingleton().gson).apply { _pref =WeakReference(this) }
                }
        }


        private const val PREFS="prefs"
        private const val PREF_LOGGED_USER="pref_logged_user"
        private const val PREF_CHECK_BOX_EAN8="pref_check_box_ean8"
        private const val PREF_CHECK_BOX_EAN13="pref_check_box_ean13"
        private const val PREF_CHECK_BOX_CODE39="pref_check_box_code39"
        private const val PREF_CHECK_BOX_CODE128="pref_check_box_code128"
        private const val PREF_INDEX_SCANNER="pref_index_scanner"
        private const val PREF_TIME_LAST_ACTIVE="pref_time_last_active"
        private const val PREF_ORDER_LINES="pref_order_lines"
        private const val PREF_HISTORY_MANUAL_INPUT_SCAN_CODES="pref_history_manual_input_scan_code"
        private const val PREF_ENABLE_MANUAL_INPUT="pref_enable_manual_input1"
        private const val PREF_FILE_SERVER_NAME="pref_file_server_name"
        private const val PREF_PANTES_SERVER_NAME="pref_pantes_server_name"
    }


    var fileServerName: String?
        get() = preferences.getString(PREF_FILE_SERVER_NAME,null)
        set(value) = preferences.edit { putString(PREF_FILE_SERVER_NAME,value) }

    val scannerIconDrawableId=
        MutableLiveData(R.drawable.ic_qr)

    var pantesServerName: String
        get() = preferences.getString(PREF_PANTES_SERVER_NAME,"109.73.192.152")?:"109.73.192.152"
        set(value) = preferences.edit { putString(PREF_PANTES_SERVER_NAME,value) }

    var manualInputHistory: ArrayList<String>
        get() {
            return gson.fromJson<ArrayList<String>>(preferences.getString(PREF_HISTORY_MANUAL_INPUT_SCAN_CODES, "[]"),ArrayList::class.java)
        }
        set(value) {
            while (value.size>20){
                value.removeAt(20)
            }
            preferences.edit{putString(PREF_HISTORY_MANUAL_INPUT_SCAN_CODES,gson.toJson(value))}
        }
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
    private val resources: Resources by lazy {
        context.resources
    }

    var manualInputIsEnable: Int
        get() = preferences.getInt(PREF_ENABLE_MANUAL_INPUT,View.VISIBLE)
        set(value){
            preferences.edit { putInt(PREF_ENABLE_MANUAL_INPUT, value) }
            enableManualInputMutableLiveData.postValue(value)
        }

    val enableManualInputMutableLiveData=
        MutableLiveData<Int>(manualInputIsEnable)

    var orderLines:String?
        get() = preferences.getString(PREF_ORDER_LINES,null)
        set(value) = preferences.edit { putString(PREF_ORDER_LINES, value) }

    var timeLastActive: Long
        get() = preferences.getLong(PREF_TIME_LAST_ACTIVE,System.currentTimeMillis())
        set(value) = preferences.edit { putLong(PREF_TIME_LAST_ACTIVE, value) }

    var scannerIndex: Int
        get() = preferences.getInt(PREF_INDEX_SCANNER,1)
        set(value) = preferences.edit { putInt(PREF_INDEX_SCANNER, value) }

    var checkBoxCode128: Boolean
        get() = preferences.getBoolean(PREF_CHECK_BOX_CODE128,true)
        set(value) = preferences.edit { putBoolean(PREF_CHECK_BOX_CODE128, value) }
    var checkBoxCode39: Boolean
        get() = preferences.getBoolean(PREF_CHECK_BOX_CODE39,true)
        set(value) = preferences.edit { putBoolean(PREF_CHECK_BOX_CODE39, value) }

    var checkBoxEAN13: Boolean
        get() = preferences.getBoolean(PREF_CHECK_BOX_EAN13,true)
        set(value) = preferences.edit { putBoolean(PREF_CHECK_BOX_EAN13, value) }

    var checkBoxEAN8: Boolean
        get() = preferences.getBoolean(PREF_CHECK_BOX_EAN8,true)
        set(value) = preferences.edit { putBoolean(PREF_CHECK_BOX_EAN8, value) }


    var loggedInUserResponse: LoggedInUserResponse?
        get(){
            return gson.fromJson(preferences.getString(PREF_LOGGED_USER,null), LoggedInUserResponse::class.java)
        }
        set(value) = preferences.edit{
            putString(PREF_LOGGED_USER, gson.toJson(value, LoggedInUserResponse::class.java))

        }

    fun getResource():Resources{
        return resources
    }
}