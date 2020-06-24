package com.ttawatchai.networklibrary

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ttawatchai.networklibrary.model.BaseResponse
import com.ttawatchai.networklibrary.model.ErrorResponse
import com.ttawatchai.networklibrary.model.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NetworkCall<T> {
    companion object {
        val LOG_TAG: String = "Networkcall"
        lateinit var baseContext: Context

    }

    private lateinit var callService: Call<T>


    fun makeCall(context: Context, call: Call<T>): MutableLiveData<Resource<T>> {
        if (!isNetworkConnected(context)) {
            var result: MutableLiveData<Resource<T>> = MutableLiveData()
            baseContext = context
            result.value = Resource.error(
                context.getString(R.string.txt_internet_error),
                null,
                baseContext.getString(R.string.msg_fail_internet)
            )
            return result
        }
        callService = call
        val callBackKt = CallBackKt<T>()
        CoroutineScope(Dispatchers.IO).launch {
            callService.clone().enqueue(callBackKt)
        }
        Log.i(LOG_TAG, callService.request().toString())
        return callBackKt.result
    }

    class CallBackKt<T> : Callback<T> {
        var result: MutableLiveData<Resource<T>> = MutableLiveData()

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (!t.message.isNullOrEmpty()) {
                if (t.message!!.contains(baseContext.getString(R.string.msg_fail_connect))) {
                    result.value =
                        Resource.connectFiled(
                            baseContext.getString(R.string.txt_internet_error),
                            null,
                            ""
                        )
                } else {
                    result.value =
                        Resource.error(
                             baseContext.getString(R.string.txt_internet_error),
                            null,
                            baseContext.getString(R.string.msg_timeout)
                        )
                }
                Log.e(LOG_TAG, t.message!!)
                t.printStackTrace()
            }
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                result.value = Resource.success(response.body())
                Log.d(
                    LOG_TAG,
                    "Response : ${response.body()}  :  ${result.value!!.status}"
                )
            } else {
                try {
                    val gson = Gson()
                    var error = JSONObject(response.errorBody()!!.string()).toString()
                    Log.e(
                        LOG_TAG,
                        "${response.message()}   :  $error  \n  ${call.request()}"
                    )
                    val dataErrorResponse = gson.fromJson(error, BaseResponse::class.java)
                    val data = gson.fromJson(error, ErrorResponse::class.java)
                    if (data != null && data.message.isEmpty()) {
                        result.value =
                            Resource.error(
                                 baseContext.getString(R.string.txt_internet_error),
                                null,
                                dataErrorResponse.statusCode.toString()
                            )
                    } else {
                        result.value =
                            Resource.error(
                                data.message,
                                null,
                                dataErrorResponse.statusCode.toString()
                            )
                    }
                } catch (e: Exception) {
                    result.value = Resource.error(
                         baseContext.getString(R.string.txt_internet_error),
                        null,
                        "exception"+e.message
                    )
                }

            }
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }


    fun cancel() {
        if (::callService.isInitialized) {
            callService.cancel()
        }
    }
}