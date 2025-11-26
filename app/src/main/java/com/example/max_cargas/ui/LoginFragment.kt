package com.example.max_cargas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.max_cargas.R
import com.example.max_cargas.data.local.AppDatabase
import com.example.max_cargas.data.model.User
import com.example.max_cargas.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val userDao = db.userDao()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = userDao.login(email, password)
                    if (user != null) {
                        // Login bem sucedido
                        findNavController().navigate(R.id.action_LoginFragment_to_MapFragment)
                    } else {
                        Toast.makeText(context, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val existingUser = userDao.getUserByEmail(email)
                    if (existingUser == null) {
                        val newUser = User(email = email, password = password, name = "Usuário")
                        userDao.insert(newUser)
                        Toast.makeText(context, "Cadastro realizado! Faça login.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Usuário já existe.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Preencha todos os campos para cadastrar", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonSkip?.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_MapFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}