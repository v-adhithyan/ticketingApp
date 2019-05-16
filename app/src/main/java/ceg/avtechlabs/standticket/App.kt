package ceg.avtechlabs.standticket

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        val font = CalligraphyConfig.Builder()
                .setDefaultFontPath("Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        CalligraphyConfig.initDefault(font)
    }
}