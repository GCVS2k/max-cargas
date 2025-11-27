package com.example.max_cargas.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.max_cargas.R
import com.example.max_cargas.data.local.AppDatabase
import com.example.max_cargas.data.model.User
import com.example.max_cargas.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esconde o menu de opções (3 pontinhos) nesta tela
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        val db = AppDatabase.getDatabase(requireContext())
        val userDao = db.userDao()

        binding.buttonRegisterConfirm.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val existingUser = userDao.getUserByEmail(email)
                    if (existingUser == null) {
                        val newUser = User(name = name, email = email, password = password)
                        val newId = userDao.insert(newUser)

                        // Salva login e redireciona
                        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("USER_ID", newId.toInt())
                            apply()
                        }
                        
                        Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_RegisterFragment_to_MapFragment)
                    } else {
                        Toast.makeText(context, "Email já cadastrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura o botão de seta para voltar
        binding.buttonBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}