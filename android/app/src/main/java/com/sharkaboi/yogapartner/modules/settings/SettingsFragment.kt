package com.sharkaboi.yogapartner.modules.settings

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.sharkaboi.yogapartner.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val navController by lazy { findNavController() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        findPreference<ToolBarPreference>("toolbar")?.setNavigationIconListener {
            navController.navigateUp()
        }
    }
}