package com.ttawatchai.networklibrary.model

data class BaseResponse(
    val isError: Boolean,
    val statusCode: Int,
    val message: String,
    val modelError: String,
    val modelErrorMessage: String
)