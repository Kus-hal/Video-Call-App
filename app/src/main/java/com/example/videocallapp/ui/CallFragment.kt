package com.example.videocallapp.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.videocallapp.R
import com.example.videocallapp.databinding.FragmentCallBinding
import com.example.videocallapp.service.MainService
import com.example.videocallapp.service.MainServiceRepository
import com.example.videocallapp.util.convertToHumanTime
import com.example.videocallapp.webrtc.RTCAudioManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CallFragment : Fragment(), MainService.EndCallListener {

    private var target: String? = null
    private var isVideoCall: Boolean = true
    private var isCaller: Boolean = true
    private var isMicrophoneMuted = false
    private var isCameraMuted = false
    private var isSpeakerMode = true
    private var isScreenCasting = false

    @Inject
    lateinit var serviceRepository: MainServiceRepository
//    private lateinit var requestScreenCaptureLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentCallBinding? = null
    private val views get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestScreenCaptureLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val intent = result.data
//                    MainService.screenPermissionIntent = intent
//                    isScreenCasting = true
//                    updateUiToScreenCaptureIsOn()
////                    serviceRepository.toggleScreenShare(true)
//                }
//            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return views.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        init()
    }

    private fun init() {
        // Fetch arguments from the Bundle
        val bundle = arguments ?: return // Return early if no arguments found

        // Retrieve data from the bundle safely
        target = bundle.getString("target")?.let {
            it
        } ?: kotlin.run {
            activity?.onBackPressed()
            return
        }

        isVideoCall = bundle.getBoolean("isVideoCall", true)
        isCaller = bundle.getBoolean("isCaller", true)

        // Setting up views and UI logic
        views.apply {
            callTitleTv.text = "In call with $target"

            // Coroutine to update call timer
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0..3600) {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        callTimerTv.text = i.convertToHumanTime()
                    }
                }
            }

            // Hide video-specific buttons if it's not a video call
            if (!isVideoCall) {
                toggleCameraButton.isVisible = false
                screenShareButton.isVisible = false
                switchCameraButton.isVisible = false
            }

            // Setup remote and local surface views
            MainService.remoteSurfaceView = remoteView
            MainService.localSurfaceView = localView

            // Initialize service repository with the necessary parameters
            serviceRepository.setupViews(isVideoCall, isCaller, target!!)

            // Set up end call button listener
            endCallButton.setOnClickListener {
                serviceRepository.sendEndCall()
            }

            // Set up switch camera button listener
            switchCameraButton.setOnClickListener {
                serviceRepository.switchCamera()
            }
        }

        // Additional setup functions for toggles and other listeners
        setupMicToggleClicked()
        setupCameraToggleClicked()
        setupToggleAudioDevice()
//    setupScreenCasting()

        // Assign the current instance as the end call listener for the service
        MainService.endCallListener = this
    }


//    private fun setupScreenCasting() {
//        views.apply {
//            screenShareButton.setOnClickListener {
//                if (!isScreenCasting) {
//                    AlertDialog.Builder(requireContext())
//                        .setTitle("Screen Casting")
//                        .setMessage("You sure to start casting?")
//                        .setPositiveButton("Yes") { dialog, _ ->
//                            startScreenCapture()
//                            dialog.dismiss()
//                        }
//                        .setNegativeButton("No") { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                        .create().show()
//                } else {
//                    isScreenCasting = false
////                    updateUiToScreenCaptureIsOff()
////                    serviceRepository.toggleScreenShare(false)
//                }
//            }
//        }
//    }

    private fun startScreenCapture() {
        val mediaProjectionManager =
            requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
//        requestScreenCaptureLauncher.launch(captureIntent)
    }
//
//    private fun updateUiToScreenCaptureIsOn() {
//        views.apply {
//            localView.isVisible = false
//            switchCameraButton.isVisible = false
//            toggleCameraButton.isVisible = false
//            screenShareButton.setImageResource(R.drawable.baseline_stop_screen_share_24)
//        }
//    }
//
//    private fun updateUiToScreenCaptureIsOff() {
//        views.apply {
//            localView.isVisible = true
//            switchCameraButton.isVisible = true
//            toggleCameraButton.isVisible = true
//            screenShareButton.setImageResource(R.drawable.baseline_screen_share_24)
//        }
//    }

    private fun setupMicToggleClicked() {
        views.apply {
            toggleMicrophoneButton.setOnClickListener {
                if (!isMicrophoneMuted) {
                    serviceRepository.toggleAudio(true)
                    toggleMicrophoneButton.setImageResource(R.drawable.baseline_mic_24)
                } else {
                    serviceRepository.toggleAudio(false)
                    toggleMicrophoneButton.setImageResource(R.drawable.baseline_mic_off_24)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }

    private fun setupToggleAudioDevice() {
        views.apply {
            toggleAudioDevice.setOnClickListener {
                if (isSpeakerMode) {
                    toggleAudioDevice.setImageResource(R.drawable.ic_speaker)
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.EARPIECE.name)
                } else {
                    toggleAudioDevice.setImageResource(R.drawable.baseline_hearing_24)
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE.name)
                }
                isSpeakerMode = !isSpeakerMode
            }
        }
    }

    private fun setupCameraToggleClicked() {
        views.apply {
            toggleCameraButton.setOnClickListener {
                if (!isCameraMuted) {
                    serviceRepository.toggleVideo(true)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_on)
                } else {
                    serviceRepository.toggleVideo(false)
                    toggleCameraButton.setImageResource(R.drawable.baseline_videocam_off_24)
                }
                isCameraMuted = !isCameraMuted
            }
        }
    }

    override fun onCallEnded() {
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        MainService.remoteSurfaceView?.release()
        MainService.remoteSurfaceView = null
        MainService.localSurfaceView?.release()
        MainService.localSurfaceView = null
    }
}
