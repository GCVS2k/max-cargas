package com.example.max_cargas.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.max_cargas.R
import com.example.max_cargas.data.local.AppDatabase
import com.example.max_cargas.data.model.ChargerSpot
import com.example.max_cargas.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private var isEditMode = false
    private val SYSTEM_USER = "Sistema"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.fabToggleEditMode.setOnClickListener {
            toggleEditMode()
        }
        
        // Cor do FAB
        binding.fabToggleEditMode.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.eco_green_primary)
        binding.fabToggleEditMode.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        if (isEditMode) {
            binding.fabToggleEditMode.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_close_clear_cancel))
            binding.textEditModeIndicator.visibility = View.VISIBLE
            binding.textEditModeIndicator.text = "MODO EDIÇÃO: Toque no mapa p/ criar ou no pino p/ apagar"
            binding.textEditModeIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.eco_accent))
            binding.textEditModeIndicator.setTextColor(ContextCompat.getColor(requireContext(), R.color.eco_green_dark))
            Toast.makeText(context, "Modo de Edição ATIVADO", Toast.LENGTH_SHORT).show()
        } else {
            binding.fabToggleEditMode.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_input_add))
            binding.textEditModeIndicator.visibility = View.GONE
            Toast.makeText(context, "Modo de Edição DESATIVADO", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val brasilia = LatLng(-15.7975, -47.8919)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(brasilia, 12f))

        checkAndAddInitialSpots()

        mMap.setOnMapClickListener { latLng ->
            if (isEditMode) {
                showAddSpotDialog(latLng)
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val spot = marker.tag as? ChargerSpot
            
            if (isEditMode) {
                if (spot != null) {
                    if (spot.addedBy == SYSTEM_USER) {
                        Toast.makeText(context, "Este é um ponto fixo do sistema e não pode ser removido.", Toast.LENGTH_LONG).show()
                    } else {
                        showDeleteSpotDialog(spot, marker)
                    }
                }
                true 
            } else {
                false 
            }
        }
    }
    
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun checkAndAddInitialSpots() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val spots = db.chargerSpotDao().getAllSpots()
            if (spots.isEmpty()) {
                val initialSpots = listOf(
                    ChargerSpot(name = "Aeroporto Internacional de Brasília", address = "Lago Sul", latitude = -15.8697, longitude = -47.9172, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Estacionamento Premium", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Shopping Conjunto Nacional", address = "SDN CNB", latitude = -15.7924, longitude = -47.8824, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Piso G2", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "ParkShopping", address = "SAI/SO Área 6580", latitude = -15.8353, longitude = -47.9557, connectorType = "CCS2", price = 1.50, isAvailable = true, description = "Eletroposto Volvo", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Pontão do Lago Sul", address = "SHIS QL 10", latitude = -15.8267, longitude = -47.8708, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Próximo aos restaurantes", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Estádio Mané Garrincha", address = "SRPN", latitude = -15.7837, longitude = -47.8990, connectorType = "CHAdeMO", price = 0.0, isAvailable = true, description = "Estacionamento Norte", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Torre de TV", address = "Eixo Monumental", latitude = -15.7906, longitude = -47.8928, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Feira da Torre", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Brasília Shopping", address = "SCN Quadra 5", latitude = -15.7860, longitude = -47.8885, connectorType = "Tipo 2", price = 2.00, isAvailable = true, description = "G1 - Vaga Verde", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Catedral Metropolitana", address = "Esplanada", latitude = -15.7975, longitude = -47.8624, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Acesso Lateral", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Terraço Shopping", address = "SHC AOS", latitude = -15.8038, longitude = -47.9362, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Entrada Principal", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Pátio Brasil Shopping", address = "SCS Quadra 7", latitude = -15.7958, longitude = -47.8922, connectorType = "Tipo 2", price = 1.00, isAvailable = true, description = "Estacionamento VIP", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Iguatemi Brasília", address = "SHIN CA 4", latitude = -15.7185, longitude = -47.8845, connectorType = "CCS2", price = 0.0, isAvailable = true, description = "Deck Parking", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Pier 21", address = "SCES Trecho 2", latitude = -15.8211, longitude = -47.8676, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Próximo ao Cinema", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Centro de Convenções Ulysses", address = "SDC", latitude = -15.7856, longitude = -47.9030, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Entrada Sul", addedBy = SYSTEM_USER),
                    ChargerSpot(name = "Esplanada dos Ministérios", address = "Bloco A", latitude = -15.7966, longitude = -47.8765, connectorType = "Tipo 2", price = 0.0, isAvailable = true, description = "Ministério da Economia", addedBy = SYSTEM_USER)
                )
                
                initialSpots.forEach { db.chargerSpotDao().insert(it) }
                loadSavedSpots()
            } else {
                loadSavedSpots()
            }
        }
    }

    private fun showAddSpotDialog(latLng: LatLng) {
        val editText = EditText(context)
        editText.hint = "Nome do Ponto de Carregamento"

        AlertDialog.Builder(context)
            .setTitle("Adicionar Novo Ponto")
            .setView(editText)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotEmpty()) {
                    saveSpot(name, latLng)
                } else {
                    Toast.makeText(context, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteSpotDialog(spot: ChargerSpot, marker: Marker) {
        AlertDialog.Builder(context)
            .setTitle("Excluir Ponto")
            .setMessage("Deseja remover o ponto '${spot.name}'?")
            .setPositiveButton("Excluir") { _, _ ->
                deleteSpot(spot, marker)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveSpot(name: String, latLng: LatLng) {
        val db = AppDatabase.getDatabase(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        lifecycleScope.launch {
            var creatorName = "Anônimo"
            if (userId != -1) {
                val user = db.userDao().getUserById(userId)
                if (user != null && user.name.isNotEmpty()) {
                    creatorName = user.name
                }
            }

            val newSpot = ChargerSpot(
                name = name,
                address = "Endereço a definir",
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                connectorType = "Tipo 2",
                price = 0.0,
                isAvailable = true,
                description = "Adicionado por usuário",
                addedBy = creatorName
            )

            db.chargerSpotDao().insert(newSpot)
            
            mMap.clear()
            loadSavedSpots()
            
            Toast.makeText(context, "Ponto adicionado por $creatorName!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteSpot(spot: ChargerSpot, marker: Marker) {
         val db = AppDatabase.getDatabase(requireContext())
         lifecycleScope.launch {
             db.chargerSpotDao().delete(spot)
             marker.remove()
             Toast.makeText(context, "Ponto removido.", Toast.LENGTH_SHORT).show()
         }
    }

    private fun loadSavedSpots() {
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            val spots = db.chargerSpotDao().getAllSpots()
            spots.forEach { spot ->
                val position = LatLng(spot.latitude, spot.longitude)
                val markerOptions = MarkerOptions()
                        .position(position)
                        .title(spot.name)
                        .snippet("Adicionado por: ${spot.addedBy}")
                
                // Adiciona ícone personalizado
                val icon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_charger_marker)
                if (icon != null) {
                    markerOptions.icon(icon)
                }

                val marker = mMap.addMarker(markerOptions)
                marker?.tag = spot
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}