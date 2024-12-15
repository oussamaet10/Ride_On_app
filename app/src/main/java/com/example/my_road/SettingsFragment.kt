package com.example.my_road

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment

import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private lateinit var exitButton: Button
    private lateinit var languageButtonEnglish: Button
    private lateinit var languageButtonArabic: Button
    private lateinit var languageButtonFrench: Button

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        languageButtonEnglish = view.findViewById(R.id.languageButtonEnglish)
        languageButtonArabic = view.findViewById(R.id.languageButtonArabic)
        languageButtonFrench = view.findViewById(R.id.languageButtonFrench)

        // Find the button by its ID
        exitButton = view.findViewById(R.id.exitButton)

        // Set click listener for the button
        exitButton.setOnClickListener {
            requireActivity().finish()  // Closes the activity and exits the app
        }

        languageButtonEnglish.setOnClickListener {
            changeLanguage("en") // Change to English
        }

        languageButtonArabic.setOnClickListener {
            changeLanguage("ar") // Change to Arabic
        }

        languageButtonFrench.setOnClickListener {
            changeLanguage("fr") // Change to French
        }
    }


    private fun changeLanguage(localeCode: String) {
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
        requireActivity().recreate() // Restart activity to apply language changes
    }



}
