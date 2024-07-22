package com.erdemyesilcicek.photoflow.view.view.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.erdemyesilcicek.photoflow.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener { registerButtonClicked(it) }
        binding.loginButton.setOnClickListener { loginButtonClicked(it) }

        val activeUser = auth.currentUser
        if(activeUser != null){
            val action = RegisterFragmentDirections.actionRegisterFragmentToFeedFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    fun registerButtonClicked(view : View){
        val email = binding.mailText.text.toString()
        val password = binding.passwordText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val action = RegisterFragmentDirections.actionRegisterFragmentToFeedFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                }
            }.addOnFailureListener {exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun loginButtonClicked(view: View){
        val email = binding.mailText.text.toString()
        val password = binding.passwordText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val action = RegisterFragmentDirections.actionRegisterFragmentToFeedFragment()
                Navigation.findNavController(requireView()).navigate(action)
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}