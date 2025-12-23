package com.github.ostap_stud

import android.app.Application
import com.github.ostap_stud.analysis.CarLicenseDetector

class CarLicenseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initDetectorInterpreters()
    }

    private fun initDetectorInterpreters(){
        CarLicenseDetector.carInterpreter =
            CarLicenseDetector.createInterpreter(
                applicationContext, CarLicenseDetector.CAR_MODEL_FILENAME
            )
        CarLicenseDetector.licInterpreter =
            CarLicenseDetector.createInterpreter(
                applicationContext, CarLicenseDetector.LICENSE_MODEL_FILENAME
            )
    }

}