package com.example.videocallapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment

class CloseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().finishAffinity()
    }
}
