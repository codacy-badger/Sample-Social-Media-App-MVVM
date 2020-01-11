package com.ardakazanci.samplesocialmediaapp.ui.main.ui.content


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ardakazanci.samplesocialmediaapp.data.network.ApiService
import com.ardakazanci.samplesocialmediaapp.repositories.ContentAddRepository
import com.ardakazanci.samplesocialmediaapp.utils.Constants
import com.ardakazanci.samplesocialmediaapp.utils.toast
import com.securepreferences.SecurePreferences
import com.titanium.locgetter.main.LocationGetterBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class ContentAddViewModel(private val app: Application) : AndroidViewModel(app) {

    private val mContext: Context = app.applicationContext

    // << SHAREDPREFS DEĞERLERİ >>
    val preferences: SharedPreferences =
        SecurePreferences(
            mContext,
            Constants.PREF_USER_TOKEN_VALUE,
            Constants.PREF_USER_TOKEN
        )

    val userToken: String? = preferences.getString(Constants.PREF_USER_TOKEN_VALUE, null)
    val userId: String? = preferences.getString(Constants.PREF_USER_ID_VALUE, null)

    // << LOCATION DEĞERLERİ >>
    private var locationGetter: LocationGetterBuilder
    private var geocoder: Geocoder
    private val LOG_TAG = "ContentAddVM"


    // Paylaşım Kuralları
    /*
    * İçerik metni olmak zorunda
    * Konum Olmak zorunda
    * Fotoğraf olmak zorunda
    * User ID olmak zorunda
    */

    // Data Binding Değerleri
    val bindContentText = MutableLiveData<String>()
    val bindContentLocation = MutableLiveData<String>()


    private val _mProgressVisible = MutableLiveData<Boolean>()
    val mProgressVisible: LiveData<Boolean>
        get() = _mProgressVisible


    // <<< COROUTINES BAŞLANGIÇ >>>
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    // <<< COROUTINES SON >>>


    init {
//        Log.i(LOG_TAG, userToken)
//        Log.i(LOG_TAG, userId)
//        Log.i(LOG_TAG, "onCreated")
        locationGetter = LocationGetterBuilder(app.applicationContext)
        geocoder = Geocoder(app.applicationContext, Locale.getDefault())
    }


    private val contentAddRepository: ContentAddRepository =
        ContentAddRepository(ApiService.mainApi)


    fun binContentShare() {
        _mProgressVisible.postValue(true)
        val userContentText = bindContentText.value
        val userLocationValue = bindContentLocation.value



        if (userContentText.isNullOrEmpty()) {
            mContext.toast("Lütfen içerik bilgisi giriniz.")
        } else if (userContentText.length >= 120) {
            mContext.toast("Lütfen 120 karakterden fazla içerik girmeyiniz.")
        } else if (userLocationValue.isNullOrEmpty()) {
            mContext.toast("Lütfen konum bilginizi giriniz.")
        } else {
            mContext.toast("İçerik paylaş butonuna tıklandı - İçerik metni :  ${bindContentText.value} ve ${bindContentLocation.value}")

            setUserShareContent(userContentText, userLocationValue)
        }


    }

    private fun setUserShareContent(userContentValue: String, userLocationValue: String) {


        val unixTime = System.currentTimeMillis() / 1000L
        val userTokenBearer = "Bearer $userToken"
        scope.launch {

            if (userId.isNullOrEmpty()) {
                mContext.toast("UserID değeri okunamadı.")
            } else if (userToken.isNullOrEmpty()) {
                mContext.toast("UserTOKEN değeri okunamadı.")
            } else {

                val contentAddProcess = contentAddRepository.getContentAddResponse(
                    userId, unixTime, userLocationValue, userContentValue, userTokenBearer
                )

                contentAddProcess!!.let {
                    if (contentAddProcess.success) {
                        mContext.toast("Gönderi başarılı")
                        _mProgressVisible.postValue(false)
                    } else if (!contentAddProcess.success) {
                        mContext.toast("Gönderi başarısız")
                        _mProgressVisible.postValue(false)
                    } else {
                        mContext.toast("Başka bir problem yaşandı")
                        _mProgressVisible.postValue(false)
                    }
                }


            }


        }


    }

    // Zorunlu Değil
    fun bindContentLocation() {

        locationGetter.let {

            try {

                locationGetter.build().getLatestLocation()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { location ->
                        // mContext.toast("Lokasyon seçim fonksiyonu : ${location.toString()}")
                        val lat = location.latitude
                        val lng = location.longitude
                        val adress = geocoder.getFromLocation(lat, lng, 1)
                        val address_1 = adress[0].locality.toString() // Mauntaion View
                        val address_2 = adress[0].countryName.toString()

                        if (!address_1.isNullOrEmpty() && !!address_2.isNullOrEmpty()) {

                            app.toast("Konum bilgisi alınırken problem yaşandı.")

                        } else {

                            bindContentLocation.value = address_1 + " " + address_2
                            app.toast("Lokasyon değeri başarıyla alındı")
                        }

                    }

            } catch (e: Exception) {
                Log.e(LOG_TAG, e.printStackTrace().toString())
            }

        }

    }


    override fun onCleared() {
        super.onCleared()
        Log.i(LOG_TAG, "OnCleared")
    }

    fun clearedCoroutines() = coroutineContext.cancel()


}
