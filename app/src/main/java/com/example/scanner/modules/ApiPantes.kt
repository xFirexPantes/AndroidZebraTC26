package com.example.scanner.modules

import android.net.Uri
import com.example.scanner.models.AcceptInfoResponse
import com.example.scanner.models.AcceptScanResponse
import com.example.scanner.models.AcceptSearchResponse
import com.example.scanner.models.ComponentInfoResponse
import com.example.scanner.models.ComponentsSearchResponse
import com.example.scanner.models.InvoiceInfoResponse
import com.example.scanner.models.InvoiceSearchResponse
import com.example.scanner.models.IsolatorReasonsResponse
import com.example.scanner.models.IsolatorSearchResponse
import com.example.scanner.models.IssuanceIssueResponse
import com.example.scanner.models.LinesInfoResponse
import com.example.scanner.models.LinesSearchResponse
import com.example.scanner.models.LoggedInUserResponse
import com.example.scanner.models.MessageToUserFromServer
import com.example.scanner.ui.MainActivity
import com.example.scanner.ui.base.NonFatalExceptionShowDialogMessage
import com.example.scanner.ui.navigation_over.ProgressFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import timber.log.Timber
import java.lang.ref.WeakReference


class ApiPantes(
    private val onRequestExecuteStart:(()->Unit)?=null,
    private val onRequestExecuteFinish:(()->Unit)?=null,
){

    companion object {
        private var _ApiPantes: WeakReference<ApiPantes>? = null
        fun getInstanceSingleton(): ApiPantes {
            return _ApiPantes
                ?.get()
                ?: run {
                    ApiPantes(
                        onRequestExecuteStart = {
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                MainActivity.MainActivityRouter.getInstanceSingleton().navigate(
                                    ProgressFragment::class.java
                                )
                            }
                        },
                        onRequestExecuteFinish = {
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                MainActivity.MainActivityRouter.getInstanceSingleton().navigate(
                                    TransparentFragment::class.java
                                )
                            }
                        }
                    ).apply { _ApiPantes = WeakReference(this) }
                }
        }
    }

    sealed class ApiState<out T : Any?> {

        data class Success<out T : Any>(val data: T) : ApiState<T>()
        data class Error(val exception: Throwable) : ApiState<Nothing>()


        override fun toString(): String {
            return when (this) {
                is Success<*> -> "Success[data=$data]"
                is Error -> "Error[exception=$exception]"
            }
        }
    }

    interface Api {

        //region login/logout
        @POST("user/login")
        fun login(
            @Body body: Map<String, String>
        ):Call<LoggedInUserResponse>

        @POST("user/logout")
        fun logout(
            @Header("Authorization") authorization:String
        ):Call<ResponseBody>
        //endregion

        @POST("issuance/comment")
        fun issuanceComment(
            @Header("Authorization") authorization:String,
            @Query("comment") comment:String,
            @Query("invoice") invoice:String,
            @Query("line") line:String?,
            @Query("token") token:String,
        ):Call<ResponseBody>
        //region issuance
        @POST("issuance/issue")
        fun issuanceIssue(
            @Header("Authorization") authorization:String,
            @Query("coil") coil: String?,
            @Query("comment") comment:String,
            @Query("invoice") invoice:String,
            @Query("line") line:String?,
            @Query("token") token:String,
        ):Call<IssuanceIssueResponse>
        @POST("issuance/elevator")
        fun issuanceElevator(
            @Header("Authorization") authorization:String,
            @Query("invoice") invoice:String,
            @Query("line") line:String?,
            @Query("token") token:String,
        ):Call<ResponseBody>
        @POST("issuance/return")
        fun issuanceReturn(
            @Header("Authorization") authorization:String,
            @Query("coil") coil: String?,
            @Query("invoice") invoice:String,
            @Query("line") line:String?,
            @Query("token") token:String,
        ):Call<IssuanceIssueResponse>
        //endregion

        //region invoice
        @GET("invoice/info")
        @Headers("Content-Type: application/json")
        fun invoiceInfo(
            @Header("Authorization") authorization:String,
            @Query("invoice") invoice:String,
            @Query("token") token:String
        ):Call<InvoiceInfoResponse>

        @GET("invoice/search")
        @Headers("Content-Type: application/json")
        fun invoiceSearch(
            @Query("query") query: String,
            @Query("last") last: String,
            @Header("Authorization") authorization:String,
            @Query("token") token: String,
            ):Call<InvoiceSearchResponse>
        //endregion

        //region lines
        @GET("line/search")
        @Headers("Content-Type: application/json")
        fun lineSearch(
            @Header("Authorization") authorization:String,
            @Query("invoice") invoice: String,
            @Query("last") last: String,
            @Query("order") order: String,
            @Query("query") query: String,
            @Query("token") token: String,
            ):Call<LinesSearchResponse>

        @GET("line/info")
        @Headers("Content-Type: application/json")
        fun lineInfo(
            @Header("Authorization") authorization:String,
            @Query("invoice") invoice: String,
            @Query("line") line: String,
            @Query("token") token: String,
            ):Call<LinesInfoResponse>
        //endregion

        //region components
        @GET("component/search")
        @Headers("Content-Type: application/json")
        fun componentSearch(
            @Header("Authorization") authorization:String,
            @Query("last") last: String,
            @Query("query") query: String,
            @Query("token") token: String,
        ):Call<ComponentsSearchResponse>

        @GET("component/info")
        @Headers("Content-Type: application/json")
        fun componentInfo(
            @Header("Authorization") authorization:String,
            @Query("component") component: String,
            @Query("token") token: String,
            ):Call<ComponentInfoResponse>
        //endregion

        //region isolator
        @GET("isolator/search")
        @Headers("Content-Type: application/json")
        fun isolatorSearch(
            @Header("Authorization") authorization:String,
            @Query("component") component: String,
            @Query("last") last: String,
            @Query("query") query: String,
            @Query("token") token: String,
//        ):Call<ResponseBody>
        ):Call<IsolatorSearchResponse>

        @GET("isolator/reasons")
        @Headers("Content-Type: application/json")
        fun isolatorReasons(
            @Header("Authorization") authorization:String,
            @Query("token") token: String,
        ):Call<IsolatorReasonsResponse>

        @POST("isolator/isolate")
        fun isolatorIsolate(
            @Header("Authorization") authorization:String,
            @Query("component") component:String,
            @Query("note") note:String,
            @Query("quantity") quantity:String,
            @Query("reason") reason:String,
            @Query("token") token:String,
            @Query("until") until:String,
        ):Call<IsolatorSearchResponse.Item>
        @POST("isolator/minus")
        fun isolatorMinus(
            @Header("Authorization") authorization:String,
            @Query("coil") coil :Int?,
            @Query("component") component :String?,
            @Query("note") note :String?,
            @Query("quantity") quantity :String?,
            @Query("token") token:String?,
        ):Call<IsolatorSearchResponse.Item>
        //):Call<ResponseBody>


        //endregion

        //region receive
        @GET("accept/search")
        @Headers("Content-Type: application/json")
        fun acceptSearch(
            @Header("Authorization") authorization:String,
            @Query("component") query: String,
//            @Query("component") component: String,
//            @Query("qr") qr: Int?,
            @Query("token") token: String,
        ):Call<AcceptSearchResponse>
        @GET("accept/scan")
        @Headers("Content-Type: application/json")
        fun acceptScan(
            @Header("Authorization") authorization:String,
            @Query("last") last: String,
            @Query("query") query: String,
            @Query("token") token: String,
        ):Call<AcceptScanResponse>
        @GET("accept/info")
        @Headers("Content-Type: application/json")
        fun acceptInfo(
            @Header("Authorization") authorization:String,
            @Query("component") component: String,
            @Query("token") token: String,
        ):Call<AcceptInfoResponse>
        //endregion

        //region log
        @POST("log/add")
        fun log(
            @Header("Authorization") authorization:String,
            @Query("data") data:String,
            @Query("token") token: String,
        ):Call<ResponseBody>
        //endregion

    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor {
            if (it.startsWith("{") or it.startsWith("--> GET") or it.startsWith("--> POST")) {
                if(!it.contains("log/add")) {
                    Timber.tag("HttpLogShort").d(it)
                }
            }
        }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .addInterceptor( {
            onRequestExecuteStart?.invoke()
            it.proceed(it.request())

        })
        .addNetworkInterceptor({
            val proceed=it.proceed(it.request())

            if (proceed.isSuccessful) {
                onRequestExecuteFinish?.invoke()
            }
            proceed
        })
        .build()
    lateinit var api:Api

    fun installApi(baseStringUri: String){
            api = Retrofit.Builder()
            .baseUrl("http://$baseStringUri")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(Api::class.java)

    }

    private fun buildException(response: Response<*>):Exception {

        fun buildDescription(): String{

            val stringError=
                response.errorBody()?.string()

            return try {
                when(response.code()){
                    500->{
                        val messageToUserFromServer=
                            Other.getInstanceSingleton().gson.fromJson(
                                stringError,
                                MessageToUserFromServer::class.java
                            )
                        if (messageToUserFromServer.query.isNullOrEmpty())
                            "${messageToUserFromServer.error}"
                        else
                            "${messageToUserFromServer.error}\n${messageToUserFromServer.query}"
                    }
                    400 -> "$stringError"
                    else -> "\nОТВЕТ:\n${stringError}\n" +
                                "ЗАПРОС ${
                                    response.raw().request().method()
                                }:\n${Uri.decode(response.raw().request().url().toString())}\n" +
                                "ЗАПРОС БОДИ:\n" +
                                "${response.raw().request().body()}"
                }

            }catch (_: Exception){
                "$stringError"
            }
        }

        return NonFatalExceptionShowDialogMessage(buildDescription())

    }


    suspend fun issuanceComment(
        token:String,
        invoice: String,
        line:String,
        comment: String,
    ): ApiState<ResponseBody> {
        return flow {
            val response:Response<ResponseBody> =
                api.issuanceComment(
                    authorization = "Bearer $token",
                    comment = comment,
                    invoice = invoice,
                    line = line,
                    token = token
                ).execute()

            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }

    //region isolator

    suspend fun isolatorSearch(token: String, component: String, last: String, query: String): ApiState<IsolatorSearchResponse> {
        return flow {
        val response:Response<IsolatorSearchResponse> =
            api.isolatorSearch(
                "Bearer $token",
                component,
                last,
                query,
                token).execute()
        emit(
            when(response.isSuccessful){
                true-> {
                    ApiState.Success(response.body()!!)
                    //ApiState.Success(("q" as IsolatorSearchResponse))

                }
                else->ApiState.Error(buildException(response))
            }
        )
    }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun isolatorReason(token:String): ApiState<IsolatorReasonsResponse> {
        return flow {
            val response:Response<IsolatorReasonsResponse> =
                api.isolatorReasons( "Bearer $token",token).execute()
            emit(
                when(response.isSuccessful){
                    true->ApiState.Success(response.body()!!)
                    else->ApiState.Error(buildException(response))
                }
            )
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun isolatorIsolating(
        token: String,
        component: String,
        note: String,
        quantity: String,
        reason: String,
        until: String,
    ): ApiState<IsolatorSearchResponse.Item> {
        return flow {
            val response:Response<IsolatorSearchResponse.Item> =
                api.isolatorIsolate(
                    authorization = "Bearer $token",
                    component = component,
                    note = note,
                    quantity = quantity,
                    reason = reason,
                    token = token,
                    until = until
                    ).execute()
            emit(
                when(response.isSuccessful){
                    true->ApiState.Success(response.body()!!)
                    else->ApiState.Error(buildException(response))
                }
            )
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun isolatorMinus(
        token: String,
        numberCoil: Int?,
        component: String?,
        note: String?,
        quantity: String?,
    ): ApiState<IsolatorSearchResponse.Item> {
        return flow {
            val response:Response<IsolatorSearchResponse.Item> =
                api.isolatorMinus(
                    authorization = "Bearer $token",
                    coil=numberCoil,
                    component=component,
                    note = note,
                    quantity = quantity,
                    token = token
                ).execute()
            emit(
                when(response.isSuccessful){
                    true->ApiState.Success(response.body()!!)
                    else->ApiState.Error(buildException(response))
                }
            )
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    //endregion

    //region components
    suspend fun componentInfo(token:String,component:String): ApiState<ComponentInfoResponse> {
        return flow {
            val response:Response<ComponentInfoResponse> =
                api.componentInfo( "Bearer $token",component,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                //else->emit(AppResult.Success(it))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }


    suspend fun componentSearch(token:String,query:String,last:String): ApiState<ComponentsSearchResponse> {
        return flow {
            val response:Response<ComponentsSearchResponse> =
                api.componentSearch( "Bearer $token",last,query,token).execute()
            emit(
                when(response.isSuccessful){
                    true->ApiState.Success(response.body()!! as ComponentsSearchResponse)
                    else->ApiState.Error(buildException(response))
                }
            )
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    //endregion

    //region invoice

    suspend fun invoiceSearch(token:String,query:String,last:String): ApiState<InvoiceSearchResponse> {
        return flow {
            val response:Response<InvoiceSearchResponse> =
                api.invoiceSearch(
                    authorization = "Bearer $token",
                    token = token,
                    query = query,
                    last = last).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!.apply { request=query }))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun invoiceInfo(token:String,invoice:String): ApiState<InvoiceInfoResponse> {
        return flow {
            val response:Response<InvoiceInfoResponse> =
                api.invoiceInfo("Bearer $token", invoice, token).execute()

            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                //else->emit(AppResult.Success(it))
                else->emit(ApiState.Error(buildException(response)))
            }
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun issuanceIssue(
        token:String,
        coil: String?,
        comment: String,
        invoice: String,
        line:String?
    ): ApiState<IssuanceIssueResponse> {
        return flow {
            val response:Response<IssuanceIssueResponse> =
                api.issuanceIssue(
                    authorization = "Bearer $token",
                    coil = coil,
                    comment = comment,
                    invoice = invoice,
                    line = line,
                    token = token
                ).execute()

            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun issuanceReturn(token:String, coil: String?,invoice: String, line:String?): ApiState<IssuanceIssueResponse> {
        return flow {
            val response:Response<IssuanceIssueResponse> =
                api.issuanceReturn(
                    authorization = "Bearer $token",
                    coil = coil,
                    invoice = invoice,
                    line = line,
                    token = token
                ).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun issuanceElevator(
        token:String,
        invoice: String,
        line:String
    ): ApiState<ResponseBody>{
        return flow {
            val response:Response<ResponseBody> =
                api.issuanceElevator(
                    authorization = "Bearer $token",
                    invoice = invoice,
                    line = line,
                    token = token
                ).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    //endregion

    //region lines
    suspend fun linesSearch(token:String, invoice: String, order: String,last: String,query: String): ApiState<*> {
        return flow {
            val response:Response<LinesSearchResponse> =
                api.lineSearch( "Bearer $token",invoice,last,order,query,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                //else->emit(AppResult.Success(it))
                else->emit(ApiState.Error(buildException(response)))
            }

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun lineInfo(token:String, invoice: String, line:String): ApiState<LinesInfoResponse> {
        return flow {
            val response:Response<LinesInfoResponse> =
                api.lineInfo( "Bearer $token",invoice,line,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    //endregion

    //region login/logout
    suspend fun logout(token:String): ApiState<Any> {
        return flow<ApiState<Any>> {

            val response:Response<ResponseBody> =
                api.logout("Bearer $token").execute()

            emit(
                when(response.isSuccessful){
                    true->ApiState.Success(response.body()!!)
                    else->ApiState.Error(buildException(response))
                }
            )

        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun login(username: String, password: String): ApiState<LoggedInUserResponse> {
        return flow {

            val response:Response<LoggedInUserResponse>

            val payload=
                mapOf("username" to username,"password" to password)

            response = api.login(payload).execute()

            val result=when(response.isSuccessful){
                true->ApiState.Success(response.body()!!)
                false->ApiState.Error(buildException(response))
            }

            emit(result)
        }.catch {emit(ApiState.Error(it))}.single()

    }
    //endregion

    //region receive
    suspend fun acceptScan(token:String, last: String, query:String): ApiState<AcceptScanResponse> {
        return flow {
            val response:Response<AcceptScanResponse> =
                api.acceptScan( "Bearer $token",last,query,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun acceptSearch(token:String, query: String): ApiState<AcceptSearchResponse> {
        return flow {
            val response:Response<AcceptSearchResponse> =
                api.acceptSearch( "Bearer $token",query,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    suspend fun acceptInfo(token:String, component: String): ApiState<AcceptInfoResponse> {
        return flow {
            val response:Response<AcceptInfoResponse> =
                api.acceptInfo( "Bearer $token",component,token).execute()
            when(response.isSuccessful){
                true->emit(ApiState.Success(response.body()!!))
                else->emit(ApiState.Error(buildException(response)))
            }
        }.flowOn(Dispatchers.IO).catch {emit(ApiState.Error(it))}.single()
    }
    //endregion

     suspend fun log(token:String, data: String): Response<ResponseBody?> {
        return api.log( "Bearer $token",data,token).execute()
    }
}

