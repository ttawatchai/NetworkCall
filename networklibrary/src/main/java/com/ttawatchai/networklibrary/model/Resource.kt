package com.ttawatchai.networklibrary.model

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(msg: String, data: T?, error: String): Resource<T>? {
            return when (error) {
                "null" -> {
                    Resource(Status.ERROR, data, "$msg- code: timeout")
                }
                "internet error" -> {
                    Resource(Status.ERROR, data, "$msg-")
                }
                else -> {
                    Resource(Status.ERROR, data, "$msg- code: $error")
                }
            }

        }

        fun <T> connectFiled(msg: String, data: T?, error: String): Resource<T>? {
            return Resource(Status.ERROR, data, "$msg-")
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(
                Status.LOADING,
                data,
                null
            )
        }

        fun <T> warning(msg: String, data: T?): Resource<T>? {
            return Resource(
                Status.WARNING,
                data,
                msg
            )
        }

        fun <T> network(msg: String, data: T?): Resource<T>? {
            return Resource(
                Status.NETWORK,
                data,
                msg
            )
        }

        fun <T> unauthorized(msg: String, data: T?): Resource<T>? {
            return Resource(
                Status.UNAUTHORIZED,
                data,
                msg
            )
        }

        fun <T> haveToken(msg: String, data: T?): Resource<T>? {
            return Resource(
                Status.HAVE_TOKEN,
                data,
                msg
            )
        }

    }
}
enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    NETWORK,
    WARNING,
    UNAUTHORIZED,
    HAVE_TOKEN
}

enum class StateDialog {
    COMPLETE,
    ERROR,
    INFORMATION,
    NONE
}