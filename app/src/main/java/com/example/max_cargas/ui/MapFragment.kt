package com.example.max_cargas.ui

import android.app.AlertDialog
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private var isEditMode = false

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
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        if (isEditMode) {
            binding.fabToggleEditMode.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_close_clear_cancel))
            binding.textEditModeIndicator.visibility = View.VISIBLE
            Toast.makeText(context, "Modo de Edição ATIVADO: Toque no mapa para adicionar", Toast.LENGTH_SHORT).show()
        } else {
            binding.fabToggleEditMode.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_input_add))
            binding.textEditModeIndicator.visibility = View.GONE
            Toast.makeText(context, "Modo de Edição DESATIVADO", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configura o mapa inicial em Brasília
        val brasilia = LatLng(-15.7975, -47.8919)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(brasilia, 12f))

        // Carrega os pontos salvos
        loadSavedSpots()

        // Configura o clique no mapa
        mMap.setOnMapClickListener { latLng ->
            if (isEditMode) {
                showAddSpotDialog(latLng)
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

    private fun saveSpot(name: String, latLng: LatLng) {
        val newSpot = ChargerSpot(
            name = name,
            address = "Endereço a definir", // Simplificação
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            connectorType = "Tipo 2", // Padrão
            price = 0.0,
            isAvailable = true,
            description = "Adicionado pelo usuário"
        )

        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            db.chargerSpotDao().insert(newSpot)
            
            // Adiciona o marcador no mapa visualmente
            mMap.addMarker(MarkerOptions().position(latLng).title(name))
            Toast.makeText(context, "Ponto adicionado com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedSpots() {
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            val spots = db.chargerSpotDao().getAllSpots()
            spots.forEach { spot ->
                val position = LatLng(spot.latitude, spot.longitude)
                mMap.addMarker(MarkerOptions().position(position).title(spot.name))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}