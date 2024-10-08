package com.example.videocallapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.videocallapp.ui.CloseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainServiceReceiver : BroadcastReceiver() {

    @Inject
    lateinit var serviceRepository: MainServiceRepository
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_EXIT") {
            //we want to exit the whole application
            serviceRepository.stopService()
            context?.startActivity(Intent(context, CloseFragment::class.java))

        }
    }
}