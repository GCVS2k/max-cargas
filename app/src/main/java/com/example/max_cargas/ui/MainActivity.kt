package com.example.max_cargas.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_user_profile -> {
                // Navega para o perfil do usuário
                try {
                    // Se estiver no mapa, usa a ação definida no grafo
                    if (navController.currentDestination?.id == R.id.MapFragment) {
                        navController.navigate(R.id.action_MapFragment_to_UserFragment)
                    } else if (navController.currentDestination?.id != R.id.UserFragment) {
                        // Se estiver em outro lugar, tenta navegar direto
                        navController.navigate(R.id.UserFragment)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }
            R.id.action_back_to_map -> {
                // Voltar ao Mapa
                try {
                    if (navController.currentDestination?.id == R.id.UserFragment) {
                        // Se estiver no perfil, voltar (popBackStack) deve levar ao mapa
                        navController.popBackStack()
                    } else if (navController.currentDestination?.id != R.id.MapFragment) {
                        // Caso contrário, tenta navegar explicitamente
                        navController.navigate(R.id.MapFragment)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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