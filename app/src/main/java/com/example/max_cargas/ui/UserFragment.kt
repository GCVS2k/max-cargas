package com.example.max_cargas.ui

import android.content.Context
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
import com.example.max_cargas.databinding.FragmentUserBinding
import kotlinx.coroutines.launch

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()

        binding.buttonSave.setOnClickListener {
            saveChanges()
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                currentUser = db.userDao().getUserById(userId)
                currentUser?.let { user ->
                    binding.editTextName.setText(user.name)
                    binding.textEmail.text = user.email
                }
            }
        } else {
            // Se não tiver usuário logado, volta pro login
            findNavController().navigate(R.id.action_UserFragment_to_LoginFragment)
        }
    }

    private fun saveChanges() {
        currentUser?.let { user ->
            val newName = binding.editTextName.text.toString()
            if (newName.isNotEmpty()) {
                val updatedUser = user.copy(name = newName)
                val db = AppDatabase.getDatabase(requireContext())
                
                lifecycleScope.launch {
                    db.userDao().update(updatedUser)
                    currentUser = updatedUser
                    Toast.makeText(context, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        findNavController().navigate(R.id.action_UserFragment_to_LoginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}