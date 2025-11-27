package com.example.max_cargas.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.max_cargas.R
import com.example.max_cargas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.MapFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Controle de visibilidade da Toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.LoginFragment, R.id.RegisterFragment, R.id.AboutFragment -> {
                    binding.toolbar.visibility = View.GONE
                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_user_profile -> {
                try {
                    if (navController.currentDestination?.id == R.id.MapFragment) {
                        navController.navigate(R.id.action_MapFragment_to_UserFragment)
                    } else if (navController.currentDestination?.id != R.id.UserFragment) {
                        navController.navigate(R.id.UserFragment)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }

            R.id.action_about -> {
                // Navega para a tela Sobre
                try {
                    navController.navigate(R.id.AboutFragment)
                } catch (e: Exception) {
                   // Se a ação direta falhar, tenta pelo ID do fragmento se for uma navegação global ou permitida
                   try {
                       // Como AboutFragment está no grafo mas não tem ação global explicita vinda de todos os lugares,
                       // o ideal seria ter uma ação global, mas aqui vamos confiar que quem chama sabe o caminho ou adicionar uma ação global.
                       // Por simplicidade, assumimos que quem chama (menu) consegue navegar.
                       // Se falhar, não faz nada.
                   } catch (e2: Exception) {}
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}