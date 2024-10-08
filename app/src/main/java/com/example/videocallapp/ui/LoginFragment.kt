package com.example.videocallapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.videocallapp.R
import com.example.videocallapp.databinding.FragmentLoginBinding
import com.example.videocallapp.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _views: FragmentLoginBinding? = null
    private val views get() = _views!!

    @Inject
    lateinit var mainRepository: MainRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _views = FragmentLoginBinding.inflate(inflater, container, false)
        return views.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        views.apply {
            loginButton.setOnClickListener {
                val enteredUsername = username.text.toString()
                val enteredPassword = password.text.toString()

                mainRepository.login(enteredUsername, enteredPassword) { isDone, reason ->
                    if (!isDone) {
                        Toast.makeText(requireContext(), reason, Toast.LENGTH_SHORT).show()
                    } else {
                        // Pass the username using Bundle
                        val bundle = Bundle().apply {
                            putString("username", enteredUsername)
                        }
                        findNavController().navigate(R.id.action_loginFragment_to_phoneBookFragment, bundle)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _views = null
    }
}
