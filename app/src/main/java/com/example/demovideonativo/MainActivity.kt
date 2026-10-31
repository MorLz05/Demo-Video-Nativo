package com.example.demovideonativo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ID de prueba para Video Nativo
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asume que tienes un FrameLayout con id "adContainer" en tu layout XML
        val adContainer = findViewById<FrameLayout>(R.id.adContainer)

        // Se recomienda construir el AdLoader en un hilo de fondo
        CoroutineScope(Dispatchers.IO).launch {
            val adLoader = AdLoader.Builder(applicationContext, AD_UNIT_ID)

                .forNativeAd { nativeAd ->
                    Log.d("NativeVideo", "Anuncio nativo cargado")

                    if (nativeAd.mediaContent?.hasVideoContent() == true) {
                        // Inflar el layout que ahora es un NativeAdView
                        val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as NativeAdView

                        // Encontrar las vistas
                        val mediaView = adView.findViewById<MediaView>(R.id.mediaView)
                        val adTitle = adView.findViewById<TextView>(R.id.adTitle)
                        val adIcon = adView.findViewById<ImageView>(R.id.adIcon)
                        val adCta = adView.findViewById<Button>(R.id.adCta)

                        // Asignar los datos a las vistas
                        adTitle.text = nativeAd.headline
                        adCta.text = nativeAd.callToAction ?: "Ver más"
                        nativeAd.icon?.drawable?.let { adIcon.setImageDrawable(it) }
                        adIcon.visibility = if (nativeAd.icon != null) View.VISIBLE else View.GONE

                        // Registrar CADA vista en el NativeAdView (esto reemplaza a registerViewForInteraction)
                        adView.headlineView = adTitle
                        adView.iconView = adIcon
                        adView.callToActionView = adCta
                        adView.mediaView = mediaView
                        mediaView.mediaContent = nativeAd.mediaContent

                        // Asignar el anuncio al NativeAdView
                        adView.setNativeAd(nativeAd)

                        // Ocultar placeholder y agregar la tarjeta
                        findViewById<TextView>(R.id.placeholderText)?.visibility = View.GONE
                        adContainer.removeAllViews()
                        adContainer.addView(adView)
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("NativeVideo", "Error al cargar: ${adError.message}")
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

            // Cargar el anuncio
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }
}