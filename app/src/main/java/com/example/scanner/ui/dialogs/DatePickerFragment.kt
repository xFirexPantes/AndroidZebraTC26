package com.example.scanner.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import com.example.scanner.R
import com.example.scanner.ui.base.BaseFragmentDialog
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Callback
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class DatePickerFragment(private val defaultDate:Any?=null,private val callback: ((date:String)->Unit)?=null): BaseFragmentDialog(), DatePickerDialog.OnDateSetListener {
    companion object{

    }
    private lateinit var simpleDateFormat:SimpleDateFormat
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        simpleDateFormat=SimpleDateFormat(getString(R.string.date_format),Locale.getDefault())
        val c = Calendar.getInstance()
        try {
            //SimpleDateFormat("yyyy-MM-dd").format(Date())
            c.time=simpleDateFormat.parse("$defaultDate")

        }catch (_:Exception){}

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        // Create a new instance of DatePickerDialog and return it.

        return DatePickerDialog(requireContext(), this, year, month, day).apply {
            datePicker.minDate= Calendar.getInstance().timeInMillis
        }
    }
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Do something with the date the user picks.
        val calendar=Calendar.getInstance()
        calendar.set(Calendar.YEAR,year)
        calendar.set(Calendar.MONTH,month)
        calendar.set(Calendar.DAY_OF_MONTH,day)
        calendar.set(Calendar.HOUR_OF_DAY,0)
        //calendar.set(Calendar.HOUR,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        callback?.invoke(
            simpleDateFormat.format(calendar.time)
        )

    }


}