package com.example.videocallapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videocallapp.R
import com.example.videocallapp.adapter.MainRecyclerViewAdapter
import com.example.videocallapp.databinding.FragmentPhoneBookBinding
import com.example.videocallapp.repository.MainRepository
import com.example.videocallapp.service.MainService
import com.example.videocallapp.service.MainServiceRepository
import com.example.videocallapp.util.DataModel
import com.example.videocallapp.util.DataModelType
import com.example.videocallapp.util.getCameraAndMicPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PhoneBookFragment : Fragment(), MainRecyclerViewAdapter.Listener, MainService.Listener {

    private val TAG = "PhoneBookFragment"

    private var _views: FragmentPhoneBookBinding? = null
    private val views get() = _views!!

    private var username: String? = null

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository
    private var mainAdapter: MainRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _views = FragmentPhoneBookBinding.inflate(inflater, container, false)
        return views.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the username passed from LoginFragment using Bundle
        username = arguments?.getString("username")
        init()
    }

    private fun init() {
        if (username == null) {
            Log.e("PhoneBookFragment", "Username is null, finishing activity.")
            requireActivity().finish()
            return
        }
        Log.d("PhoneBookFragment", "Username: $username")

        // Observe user status and start service
        subscribeObservers()

        getCameraAndMicPermission { startMyService() }
    }


    private fun subscribeObservers() {
        setupRecyclerView()
        MainService.listener = this
        mainRepository.observeUsersStatus {
            Log.d(TAG, "subscribeObservers: $it")
            mainAdapter?.updateList(it)
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainRecyclerViewAdapter(this)
        val layoutManager = LinearLayoutManager(requireContext())
        views.mainRecyclerView.apply {
            setLayoutManager(layoutManager)
            adapter = mainAdapter
        }
    }

    private fun startMyService() {

        username?.let {
            mainServiceRepository.startService(it)
        } ?: kotlin.run {
            Log.e(TAG, "startMyService: username is null")
        }
    }

    override fun onVideoCallClicked(username: String) {
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, true) {
                if (it) {
                    val bundle = Bundle().apply {
                        putString("target", username)
                        putBoolean("isVideoCall", true)
                        putBoolean("isCaller", true)
                    }
                    findNavController().navigate(
                        R.id.action_phoneBookFragment_to_callFragment,
                        bundle
                    )
                }
            }
        }
    }

    override fun onAudioCallClicked(username: String) {
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, false) {
                if (it) {
                    // Start audio call
                    val bundle = Bundle().apply {
                        putString("target", username)
                        putBoolean("isVideoCall", false)
                        putBoolean("isCaller", false)
                    }
                    findNavController().navigate(
                        R.id.action_phoneBookFragment_to_callFragment,
                        bundle
                    )
                }

            }
        }
    }

    override fun onCallReceived(model: DataModel) {
        requireActivity().runOnUiThread {
            views.apply {
                val isVideoCall = model.type == DataModelType.StartVideoCall
                val isVideoCallText = if (isVideoCall) "Video" else "Audio"
                incomingCallTitleTv.text = "${model.sender} is $isVideoCallText Calling you"
                incomingCallLayout.isVisible = true
                acceptButton.setOnClickListener {
                    getCameraAndMicPermission {
                        incomingCallLayout.isVisible = false
                        // Create an intent to go to video call activity
                        val bundle = Bundle().apply {
                            putString("target", model.sender)
                            putBoolean("isVideoCall", isVideoCall)
                            putBoolean("isCaller", false)
                        }

                        findNavController().navigate(
                            R.id.action_phoneBookFragment_to_callFragment,
                            bundle
                        )
                    }
                }
                declineButton.setOnClickListener {
                    incomingCallLayout.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _views = null
    }
}
