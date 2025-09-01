@file:Suppress("UNCHECKED_CAST")

package com.example.scanner.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.scanner.R
import com.example.scanner.databinding.TemplateAttributeData1Binding
import com.example.scanner.databinding.TemplateAttributeDataBinding
import com.example.scanner.databinding.TemplateAttributeTitleBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateCheckBoxBinding
import com.example.scanner.databinding.TemplateDividerBinding
import com.example.scanner.databinding.TemplateImageSqueryBinding
import com.example.scanner.databinding.TemplateInputTextBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateRecyclerPresenterBinding
import com.example.scanner.databinding.TemplateSpinnerBinding
import com.example.scanner.models.AcceptInfoResponse.Attribute
import com.example.scanner.models.ComponentInfoResponse
import com.example.scanner.models.ComponentInfoResponse.Marking
import com.example.scanner.modules.Other
import com.example.scanner.ui.MainActivity
import com.example.scanner.ui.navigation_over.TransparentFragment
import com.example.scanner.ui.views.MyTextInputEditText
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import org.apache.commons.io.FilenameUtils
import timber.log.Timber
import java.io.File
import kotlin.collections.forEach
import kotlin.reflect.KProperty1

@SuppressLint("ClickableViewAccessibility")
fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

fun Context.softInput(view:View,show:Boolean){
    view.post {
        if (show)
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(view,InputMethodManager.SHOW_FORCED)
        else
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken,0)
    }
}

fun Boolean.toRusString():String{
    return if (this) "да" else "нет"
}

fun isValidAction(actionId: Int, event: KeyEvent?):Boolean{
    return (
            actionId == EditorInfo.IME_ACTION_DONE
            ||
            actionId == EditorInfo.IME_ACTION_UNSPECIFIED
            )
            &&
            (
                    event?.action == KeyEvent.ACTION_DOWN
                            ||
                            event==null
                    )
}

fun Double.toStringPresent():String{
    val isHasFractionalPartOfNumber=
        this%this.toInt()>0
    return if (isHasFractionalPartOfNumber)
        this.toString()
    else
        this.toInt().toString()
}

fun Any.valueByName(stringName: String): Any? {
    return try {
         (this::class.members
            .first { field-> field.name == stringName }  as KProperty1<Any, *>).get(this)
    }catch (_: Exception){
        stringName
    }
}

fun <T>LifecycleOwner.batch2(mutableLiveData: MutableLiveData<T>, currentVal:ArrayList<T>){
    mutableLiveData.observe(this,object : Observer<T>{
        override fun onChanged(value: T) {
            if (currentVal.isNotEmpty()) {
                mutableLiveData.postValue(currentVal[0])
                currentVal.removeAt(0)
            }
            if (currentVal.isEmpty()){
                mutableLiveData.removeObserver(this)
            }
        }
    })
}

fun String.toChunks(lengthChunk:Int): ArrayList<String>{
    var tmp=this
    return ArrayList<String>()
        .apply {
            while (tmp.isNotBlank()) {
                if (tmp.length <= lengthChunk) {
                    add(tmp)
                    tmp = tmp.substring(tmp.length, tmp.length)
                } else {
                    add(tmp.substring(0, lengthChunk))
                    tmp = tmp.substring(lengthChunk, tmp.length)
                }
            }
        }


}

fun Any.toPresentString(any:Any?): String{
    return if (any is Boolean)
        any.toRusString()
    else if (any is Double)
        any.toStringPresent()
    else
        "$any"
}

fun ImageView.loadImage(uri: String,callback: Callback?=null){
    Other.getInstanceSingleton().picasso.load(uri)
           .into(this,callback)
}

fun String.downloadFile(context: Context){
    val  strSrc= Uri.decode(this)
    val request = DownloadManager.Request(strSrc.toUri())
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationUri(Uri.fromFile(File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), File(strSrc).name
        )))
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

}

//region anim View
const val floatEnable=1f
const val floatDisable=.3f

fun View.fadeIn(){
    val animation = AlphaAnimation(0f, 1f)
    animation.duration=1000
    animation.fillAfter=true
    startAnimation(animation)
}
fun ViewGroup.getAllDescendants(view: View = this): MutableList<View> {
    val descendants = mutableListOf<View>()
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            descendants.add(child) // Add the child itself
            descendants.addAll(getAllDescendants(child)) // Recursively add its descendants
        }
    }
    return descendants
}

fun View.fadeOutGone(){
    val animation = AlphaAnimation(1f, 0f)
    animation.duration=500
    animation.fillAfter=true
    startAnimation(animation)
    animation.setAnimationListener(object :Animation.AnimationListener{
        override fun onAnimationStart(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            visibility= View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {

        }
    })
    postDelayed({visibility= View.GONE},animation.duration)
}
//endregion

//region Template Presenter Binding
enum class AttributesType{
    CheckBox,MaterialDivider,ImageSquareViewContainer,SpinnerView,InputText,RecyclerView,TitleAttribute,DataAttribute,DataAttribute1
}

private fun TemplatePresenterBinding.allAttributes():HashMap<AttributesType, View>{
    root.tag=when(root.tag){
        null-> HashMap()
        else -> root.tag as HashMap<AttributesType, View>
    }
    return root.tag as HashMap<AttributesType, View>
}
private fun TemplatePresenterBinding.newAttribute(
    type: AttributesType
): ViewBinding{

    val viewBindingAttribute=
        when(type) {
            AttributesType.CheckBox ->
                TemplateCheckBoxBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.MaterialDivider ->
                TemplateDividerBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.ImageSquareViewContainer ->
                TemplateImageSqueryBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.SpinnerView ->
                TemplateSpinnerBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.InputText ->
                TemplateInputTextBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.RecyclerView ->
                TemplateRecyclerPresenterBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.TitleAttribute ->
                TemplateAttributeTitleBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.DataAttribute ->
                TemplateAttributeDataBinding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )

            AttributesType.DataAttribute1 ->
                TemplateAttributeData1Binding.inflate(
                    LayoutInflater.from(this.root.context),
                    this.root, false
                )
        }

    root.addView(viewBindingAttribute.root)

    allAttributes()
        .put(type,viewBindingAttribute.root)

    return viewBindingAttribute
}

fun TemplatePresenterBinding.setAttribute(
    pair: Pair<Array<out Any>, String>,
    srcData: Any,
    mainActivityRouter: MainActivity.MainActivityRouter?=null
){

    try {
        //pair.first[0] - name field database 1
        //pair.first[1] - name field database 2
        //pair.first[2] - ClickableSpan
        //pair.first[3] - orientation parent LinearLayout
        //pair.first[4] - name field database 3
        //pair.first[5] - add checkbox
        if (!pair.second.isBlank()){
            templateAttributeTitleTextView.text = pair.second
        }
        when (pair.first[0]) {
            "image" -> {
                try {

                    val data:ArrayList<ComponentInfoResponse.Images> = srcData.valueByName("images") as ArrayList<ComponentInfoResponse.Images>
                    if (data.isEmpty())
                        throw Exception()
                    val inflater=LayoutInflater.from(templateRecyclerRecyclerView.context)
                    templateRecyclerRecyclerView.layoutManager= LinearLayoutManager(templateRecyclerRecyclerView.context)
                    templateRecyclerRecyclerView.adapter= object :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
                        override fun onCreateViewHolder(
                            parent: ViewGroup,
                            viewType: Int
                        ): RecyclerView.ViewHolder {

                            return object :RecyclerView.ViewHolder(
                                TemplateCardBinding.inflate(inflater,parent,false)
                                    .root
                            ) {}
                        }

                        override fun onBindViewHolder(
                            holder: RecyclerView.ViewHolder,
                            position: Int
                        ) {

                            TemplateCardBinding.bind(holder.itemView).containerVertical.removeAllViews()
                            TemplateCardBinding.bind(holder.itemView).containerVertical
                                .apply {
                                    addView(
                                        TemplatePresenterBinding.inflate(inflater,root,false)
                                            .apply {
                                                templateImageSquareContainer.visibility =View.VISIBLE
                                                data[position].url?.let {url->
                                                    templateImageSquareImageView.loadImage(url,object :Callback{
                                                        override fun onSuccess() {
                                                            templateImageSquareImageView.setOnClickListener {
                                                                mainActivityRouter?.navigate(
                                                                    TransparentFragment::class.java,
                                                                    Bundle().apply {
                                                                        putSerializable(
                                                                            TransparentFragment.PARAM,
                                                                            url
                                                                        )
                                                                    }
                                                                )
                                                            }
                                                        }

                                                        override fun onError(e: java.lang.Exception?) {
                                                            (templateImageSquareImageView.layoutParams as LinearLayout.LayoutParams).height=LinearLayout.LayoutParams.WRAP_CONTENT
                                                            templateImageSquareImageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                                                            templateImageSquareImageView.setImageResource(R.drawable.ic_error)
                                                            templateImageSquareImageViewError.text=
                                                                StringBuilder("Файл не найден \n${e?.message}")
                                                        }
                                                    })
                                                }

                                            }
                                            .root
                                    )
                                    addView(
                                        TemplatePresenterBinding.inflate(inflater,root,false)
                                            .apply {
                                                setAttribute(Pair(arrayOf("horizontalDivider"),""),"")
                                            }.root

                                    )

                                }

                        }

                        override fun getItemCount(): Int {
                            return data.size
                        }

                    }
                    templateRecyclerContainer.visibility =View.VISIBLE

                }catch (_: Exception){
                    try {
                        templateImageSquareContainer.visibility =View.VISIBLE
                        val url=srcData.valueByName("image").toString()
                        templateImageSquareImageView.loadImage(
                            url,
                            object :Callback{
                                override fun onSuccess() {
                                    templateImageSquareImageView.setOnClickListener {
                                        mainActivityRouter?.navigate(
                                            TransparentFragment::class.java,
                                            Bundle().apply {
                                                putSerializable(
                                                    TransparentFragment.PARAM,
                                                    url
                                                )
                                            }
                                        )
//                                                    holder.itemView.context.startActivity(
//                                                        Intent(Intent.ACTION_VIEW)
//                                                            .apply {
//                                                                setDataAndType(
//                                                                    url.toUri(),
//                                                                    "image/*"
//                                                                )
//                                                            }
//                                                    )
                                    }
                                }

                                override fun onError(e: java.lang.Exception?) {
                                    (templateImageSquareImageView.layoutParams as LinearLayout.LayoutParams).height=LinearLayout.LayoutParams.WRAP_CONTENT
                                    templateImageSquareImageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                                    templateImageSquareImageView.setImageResource(R.drawable.ic_error)
                                    templateImageSquareImageViewError.text= StringBuilder("Файл не найден \n${e?.message}")
                                }

                            })
                    }catch (_: Exception){}
                }
            }
            "rack" -> {
                templateAttributeDataTextView.text =
                    root.context.getString(
                        R.string.position,
                        srcData.valueByName(
                            pair.first[0].toString()
                        ).toString().toString(),
                        srcData.valueByName(
                            pair.first[1].toString()
                        ).toString().toString()
                    )
            }
            "separate" -> {
                templateAttributeDataTextView.text =
                    srcData.valueByName(
                        pair.first[0].toString()
                    ).toString().toString()
                        .toBoolean().toRusString()
            }
            "markings" -> {
                templateSpinnerTitle.text = pair.second
                templateSpinnerContainer.visibility =
                    View.VISIBLE
                templateSpinnerSpinner.adapter =
                    ArrayAdapter<String>(
                        root.context,
                        android.R.layout.simple_list_item_1,
                        ArrayList<String?>()
                            .apply {
                                (srcData.valueByName(
                                    pair.first[0].toString()
                                ) as List<Attribute>).forEach {
                                    add(
                                        StringBuilder().append(
                                            it.name
                                        ).append(
                                            ":"
                                        ).append(
                                            it.value
                                        ).toString()
                                    )
                                }
                            })

            }
            "marking"->{
                templateSpinnerTitle.text = pair.second
                templateSpinnerContainer.visibility=
                    View.VISIBLE
                templateSpinnerSpinner.adapter=
                    ArrayAdapter<String>(
                        root.context,
                        android.R.layout.simple_list_item_1,
                        ArrayList<String?>()
                            .apply {
                                val markings=srcData.valueByName("markings") as List<Marking>
                                markings.forEach {
                                    add(
                                        it.label
                                    )
                                }
                            })

            }
            "attribute"->{
                val attributesToHashMap=srcData.valueByName("attributesToHashMap")
                templateAttributeTitleTextView.text=pair.first[1].toString()
                templateAttributeDataTextView.text=(attributesToHashMap as HashMap<*,*>)[pair.first[1]].toString()

            }
            "document"->{

                val s=try {
                    //File(Uri.decode(srcData.valueByName("document").toString())).name
                    //FilenameUtils.getName("\\\\tsrv\\docs\\Заказчики\\ЦПТ Агроцифра\\СИ БУ-702\\KD\\47804025.301415.001 Радиатор\\Детали\\47804025.752694.001-01 _ Радиатор.pdf")
                    FilenameUtils.getName(Uri.decode(srcData.valueByName("document").toString()))
                }catch (_: Exception){
                    srcData.valueByName("document").toString()
                }

                templateAttributeDataTextView.text=
                    if (pair.first.size>2){
                        val clickableSpan:ClickableSpan = pair.first[2] as ClickableSpan
                        val spannableString=SpannableString(s)
                        spannableString.setSpan(clickableSpan, 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        templateAttributeDataTextView.movementMethod = LinkMovementMethod.getInstance()
                        spannableString
                    }else{
                        s
                    }

            }
            "horizontalDivider"->{
                templateDividerHorizontalDivider.visibility= View.VISIBLE
                templateAttributeTitleTextView.visibility= View.GONE
                templateAttributeDataTextView.visibility= View.GONE
            }
            else -> {

                templateAttributeDataTextView.text =toPresentString(
                    srcData.valueByName(pair.first[0].toString()))

                //region addition setup this view
                //region vertical
                if (pair.first.size>=4 && pair.first[3] is Int){
                    root.orientation=pair.first[3] as Int
                }
                //endregion
                //region position
                if (pair.first[0]=="amount") {
                    try {
                        templateAttributeData1TextView.gravity = Gravity.CENTER_HORIZONTAL
                        templateAttributeData1TextView.text =
                            StringBuilder()
                                .append(" ")
                                .append(
                                    root.context.getString(
                                        R.string.position,
                                        srcData.valueByName(
                                            "rack"
                                        ).toString().toString(),
                                        srcData.valueByName(
                                            "cell"
                                        ).toString().toString()
                                    )
                                ).toString()
                        templateAttributeData1TextView.visibility = View.VISIBLE

                    } catch (_: Exception) {}
                }
                //endregion
                //region checkbox
                if (pair.first.size>=6 && pair.first[5]==true){
                    (templateCheckBoxCheckBox.parent as FrameLayout).visibility= View.VISIBLE
                }
                //endregion
                //endregion
            }
        }

    }catch (e:Exception){
        Timber.tag("ViewBinding").d(e)
    }

}


val TemplatePresenterBinding.templateImageSquareContainer: ConstraintLayout
    get()  {

        return allAttributes()[AttributesType.ImageSquareViewContainer]
            ?.let {
                TemplateImageSqueryBinding.bind(it).dataAttributeImageSquareViewContainer
            }
            ?:run {
                (newAttribute(AttributesType.ImageSquareViewContainer) as TemplateImageSqueryBinding).root
            }


    }
val TemplatePresenterBinding.templateDividerHorizontalDivider: MaterialDivider
    get()  {
        return allAttributes()[AttributesType.MaterialDivider]
            ?.let {
                TemplateDividerBinding.bind(it).horizontalDivider
            }
            ?:run {
                (newAttribute(AttributesType.MaterialDivider) as TemplateDividerBinding).horizontalDivider
            }
    }
val TemplatePresenterBinding.templateCheckBoxCheckBox: CheckBox
    get()  {
        return allAttributes()[AttributesType.CheckBox]
            ?.let {
                TemplateCheckBoxBinding.bind(it).dataAttributeCheckBox
            }
            ?:run {
                (newAttribute(AttributesType.CheckBox) as TemplateCheckBoxBinding).dataAttributeCheckBox
            }
    }
val TemplatePresenterBinding.templateCheckBoxRoot: FrameLayout
    get() {
        return allAttributes()[AttributesType.CheckBox]
            ?.let {
                TemplateCheckBoxBinding.bind(it).root
            }
            ?:run {
                (newAttribute(AttributesType.CheckBox) as TemplateCheckBoxBinding).root
            }
    }
val TemplatePresenterBinding.templateImageSquareImageView: ImageView
    get()  {
        return allAttributes()[AttributesType.ImageSquareViewContainer]
            ?.let {
                TemplateImageSqueryBinding.bind(it).dataAttributeSquareImageView
            }
            ?:run {
                (newAttribute(AttributesType.ImageSquareViewContainer) as TemplateImageSqueryBinding).dataAttributeSquareImageView
            }
    }
val TemplatePresenterBinding.templateImageSquareImageViewError: TextView
    get()  {
        return allAttributes()[AttributesType.ImageSquareViewContainer]
            ?.let {
                TemplateImageSqueryBinding.bind(it).dataAttributeSquareImageViewError
            }
            ?:run {
                (newAttribute(AttributesType.ImageSquareViewContainer) as TemplateImageSqueryBinding).dataAttributeSquareImageViewError
            }
    }
val TemplatePresenterBinding.templateSpinnerTitle: TextView
    get()  {
        return allAttributes()[AttributesType.SpinnerView]
            ?.let {
                TemplateSpinnerBinding.bind(it).dataTitleSpinnerView
            }
            ?:run {
                (newAttribute(AttributesType.SpinnerView) as TemplateSpinnerBinding).dataTitleSpinnerView
            }

    }
val TemplatePresenterBinding.templateSpinnerContainer: LinearLayout
    get()  {
        return allAttributes()[AttributesType.SpinnerView]
            ?.let {
                TemplateSpinnerBinding.bind(it).dataAttributeSpinnerViewContainer
            }
            ?:run {
                (newAttribute(AttributesType.SpinnerView) as TemplateSpinnerBinding).dataAttributeSpinnerViewContainer
            }
    }
val TemplatePresenterBinding.templateSpinnerSpinner: Spinner
    get()  {
        return allAttributes()[AttributesType.SpinnerView]
            ?.let {
                TemplateSpinnerBinding.bind(it).dataAttributeSpinnerView
            }
            ?:run {
                (newAttribute(AttributesType.SpinnerView) as TemplateSpinnerBinding).dataAttributeSpinnerView
            }
    }
val TemplatePresenterBinding.templateInputTextContainer: FrameLayout
    get()  {
        return allAttributes()[AttributesType.InputText]
            ?.let {
                TemplateInputTextBinding.bind(it).dataAttributeNoteViewContainer
            }
            ?:run {
                (newAttribute(AttributesType.InputText) as TemplateInputTextBinding)
                    .dataAttributeNoteViewContainer
            }
    }
val TemplatePresenterBinding.templateInputTextTextLayout: TextInputLayout
    get()  {
        return allAttributes()[AttributesType.InputText]
            ?.let {
                TemplateInputTextBinding.bind(it).noteLayout
            }
            ?:run {
                (newAttribute(AttributesType.InputText) as TemplateInputTextBinding).noteLayout
            }
    }
val TemplatePresenterBinding.templateInputTextMyTextInput: MyTextInputEditText
    get()  {
        return allAttributes()[AttributesType.InputText]
            ?.let {
                TemplateInputTextBinding.bind(it).note
            }
            ?:run {
                (newAttribute(AttributesType.InputText) as TemplateInputTextBinding).note
            }
    }
val TemplatePresenterBinding.templateRecyclerRecyclerView: RecyclerView
    get()  {
        return allAttributes()[AttributesType.RecyclerView]
            ?.let {
                TemplateRecyclerPresenterBinding.bind(it).dataRecyclerView
            }
            ?:run {
                (newAttribute(AttributesType.RecyclerView) as TemplateRecyclerPresenterBinding).dataRecyclerView
            }
    }
val TemplatePresenterBinding.templateRecyclerContainer: FrameLayout
    get()  {
        return allAttributes()[AttributesType.RecyclerView]
            ?.let {
                TemplateRecyclerPresenterBinding.bind(it).dataRecyclerViewContainer
            }
            ?:run {
                (newAttribute(AttributesType.RecyclerView) as TemplateRecyclerPresenterBinding).dataRecyclerViewContainer
            }
    }
val TemplatePresenterBinding.templateAttributeTitleTextView: TextView
    get()  {
            return allAttributes()[AttributesType.TitleAttribute]
                ?.let {
                    TemplateAttributeTitleBinding.bind(it).titleAttribute
                }
                ?:run {
                    (newAttribute(AttributesType.TitleAttribute) as TemplateAttributeTitleBinding).titleAttribute
                }
    }
val TemplatePresenterBinding.templateAttributeDataTextView: TextView
    get()  {
        return (
                allAttributes()[AttributesType.DataAttribute]
                    ?.let {
                        TemplateAttributeDataBinding.bind(it)
                    }
                    ?:run {
                        (newAttribute(AttributesType.DataAttribute) as TemplateAttributeDataBinding)
                    }
                ).dataAttribute
    }
val TemplatePresenterBinding.templateAttributeData1TextView: TextView
    get()  {

        return (allAttributes()[AttributesType.DataAttribute1]
            ?.let {
                TemplateAttributeData1Binding.bind(it)
            }
            ?:run {
                (newAttribute(AttributesType.DataAttribute1) as TemplateAttributeData1Binding)
            }
                ).dataAttribute1
    }

//endregion

const val textInvalidValue="неверное значение"
