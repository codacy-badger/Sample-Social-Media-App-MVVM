package com.ardakazanci.anlikmotivasyon.ui.onboarding.signup

import android.Manifest
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.ardakazanci.anlikmotivasyon.R
import com.ardakazanci.anlikmotivasyon.databinding.SignUpFragmentBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.qingmei2.rximagepicker.core.RxImagePicker
import com.qingmei2.rximagepicker.ui.SystemImagePicker
import es.dmoral.toasty.Toasty


class SignUpFragment : Fragment() {

    private lateinit var signUpViewModel: SignUpViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // <<< DATA-BINDING AYARLARLARI BAŞLANGIÇ >>>
        val binding = DataBindingUtil.inflate<SignUpFragmentBinding>(
            inflater,
            R.layout.sign_up_fragment,
            container,
            false
        )

        binding.lifecycleOwner = this
        signUpViewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)
        binding.signUpViewModel = signUpViewModel
        // <<< DATA-BINDING AYARLARLARI SON >>>

        // <<< FOTOĞRAF SEÇME İŞLEMİ BAŞLANGIÇ >>>
        binding.imgTopPhotoSelect.setOnClickListener {
            Dexter.withActivity(activity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    @SuppressLint("CheckResult")
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        this@SignUpFragment.context!!.let {
                            RxImagePicker.create(SystemImagePicker::class.java)
                                .openGallery(it).subscribe { result ->
                                    signUpViewModel.fieldUserImageUri.postValue(result.uri)
                                }

                        }


                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {}

                }).check()

        }
        // <<< FOTOĞRAF SEÇME İŞLEMİ SON >>>

        // <<< FIELD KONTROLLERİ BAŞLANGIÇ >>>
        signUpViewModel.fieldEmptyControl.observe(this, Observer { message ->
            this.context!!.let {
                Toasty.custom(
                    it,
                    message,
                    ContextCompat.getDrawable(it, R.drawable.ic_info),
                    ContextCompat.getColor(it, R.color.color_sign_button),
                    ContextCompat.getColor(it, android.R.color.white),
                    Toasty.LENGTH_SHORT,
                    true,
                    true
                ).show()
            }
        })
        // <<< FIELD KONTROLLERİ SON >>>

        // <<< KAYIT DURUM KONTROLÜ BAŞLANGIÇ >>>
        signUpViewModel.signUpSuccess.observe(this, Observer {
            this.context!!.let {
                Toasty.custom(
                    it, "Kayıt Başarılı",
                    ContextCompat.getDrawable(it, R.drawable.ic_info),
                    ContextCompat.getColor(it, R.color.color_sign_button),
                    ContextCompat.getColor(it, android.R.color.white),
                    Toasty.LENGTH_SHORT,
                    true,
                    true
                ).show()
            } // Toast Message
        })
        // <<< KAYIT DURUM KONTROLÜ SON >>>

        // <<< NAVIGATION KONTROLLERİ BAŞLANGIÇ >>>
        binding.tvSigninLinkText.setOnClickListener { view: View ->
            signUpViewModel.cancelCoroutines()
            view.findNavController().navigate(R.id.action_signUpFragment_to_signInFragment2)
        }
        // <<< NAVIGATION KONTROLLERİ SON >>>

        return binding.root
    }


}
