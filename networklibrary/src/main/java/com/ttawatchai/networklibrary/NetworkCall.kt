package com.ttawatchai.networklibrary

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
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
    companion object{
        val LOG_TAG: String = "Networkcall"

    }
    private lateinit var callService: Call<T>


    fun makeCall(context: Context, call: Call<T>): MutableLiveData<Resource<T>> {
        callService = call
        val callBackKt = CallBackKt<T>()
//        callBackKt.result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            callService.clone().enqueue(callBackKt)
        }
        Log.i(LOG_TAG, callService.request().toString())
        return callBackKt.result
    }

    class CallBackKt<T> : Callback<T> {
        var result: MutableLiveData<Resource<T>> = MutableLiveData()

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (t.message != null) {
                if (t.message == "timeout") {
                    result.value = Resource.error(
                        "เกิดข้อผิดพลาดเซิร์ฟเวอร์ไม่มีการตอบสนอง กรุณาลองใหม่อีกครั้ง",
                        null
                    )
                    Log.e(LOG_TAG, t.message!!)
                    t.printStackTrace()
                } else {
                    result.value = Resource.error("เกิดข้อผิดพลาด กรุณาติดต่อผู้ดูเเลระบบ", null)
                    Log.e(LOG_TAG, t.message!!)
                    t.printStackTrace()
                }
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
                    val error = JSONObject(response.errorBody()!!.string()).toString()
                    val data = gson.fromJson(error, ErrorResponse::class.java)
                    if (data != null && data.message.isEmpty()) {
                        result.value =
                            Resource.error("เกิดข้อผิดพลาด กรุณาติดต่อผู้ดูเเลระบบ", null)
                    } else {
                        result.value = Resource.error(data.message, null)
                    }
                    Log.e(
                        LOG_TAG,
                        response.message() + " : " + error + "\n" + call.request().toString()
                    )
                } catch (e: Exception) {
                    result.value = Resource.error("เกิดข้อผิดพลาด กรุณาติดต่อผู้ดูเเลระบบ", null)
                }

            }
        }
    }


    fun cancel() {
        if (::callService.isInitialized) {
            callService.cancel()
        }
    }
}