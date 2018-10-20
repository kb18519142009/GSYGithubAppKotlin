package com.shuyu.github.kotlin.repository

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import com.shuyu.github.kotlin.common.config.AppConfig
import com.shuyu.github.kotlin.common.net.*
import com.shuyu.github.kotlin.common.utils.Debuger
import com.shuyu.github.kotlin.common.utils.GSYPreference
import com.shuyu.github.kotlin.model.LoginRequestModel
import com.shuyu.github.kotlin.model.User
import com.shuyu.github.kotlin.service.LoginService
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * 登录数据仓库对象
 */
class LoginRepository @Inject constructor(private val retrofit: Retrofit, private val userRepository: UserRepository) {

    private var usernameStorage: String by GSYPreference(AppConfig.USER_NAME, "")

    private var passwordStorage: String by GSYPreference(AppConfig.PASSWORD, "")

    private var accessTokenStorage: String by GSYPreference(AppConfig.ACCESS_TOKEN, "")

    private var userBasicCodeStorage: String by GSYPreference(AppConfig.USER_BASIC_CODE, "")


    fun getTokenObservable(): Observable<String> {
        return retrofit.create(LoginService::class.java)
                .authorizations(LoginRequestModel.generate())
                .flatMap {
                    FlatMapResponse2Result(it)
                }.map {
                    it.token ?: ""
                }.doOnNext {
                    Debuger.printfLog("token $it")
                    accessTokenStorage = it
                }.onErrorResumeNext(Function<Throwable, Observable<String>> { t ->
                    Debuger.printfLog("token onErrorResumeNext ")
                    clearTokenStorage()
                    Observable.error(t)
                })
    }


    fun login(context: Context, username: String, password: String, token: MutableLiveData<Boolean>) {

        clearTokenStorage()

        val type = "$username:$password"

        val base64 = Base64.encodeToString(type.toByteArray(), Base64.NO_WRAP).replace("\\+", "%2B")

        Debuger.printfLog("base64Str login $base64")

        usernameStorage = username

        userBasicCodeStorage = base64


        val loginService = getTokenObservable()

        val userService = userRepository.getPersonInfoObservable()

        val authorizations = Observable.zip(loginService, userService,
                BiFunction<String, User, User> { _, user ->
                    user
                }).flatMap {
            FlatMapResult2Response(it)
        }

        RetrofitFactory.executeResult(authorizations, object : ResultProgressObserver<User>(context) {
            override fun onSuccess(result: User?) {
                passwordStorage = password
                token.value = true
            }

            override fun onCodeError(code: Int, message: String) {
                token.value = false
            }

            override fun onFailure(e: Throwable, isNetWorkError: Boolean) {
                token.value = false
            }

        })

    }


    fun clearTokenStorage() {
        accessTokenStorage = ""
        userBasicCodeStorage = ""
    }
}