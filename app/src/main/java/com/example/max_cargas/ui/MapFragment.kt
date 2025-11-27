package com.example.max_cargas.ui

import android.app.AlertDialog
import android.content.Context
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
import com.google.android.gms.maps.model.Marker
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
            binding.textEditModeIndicator.text = "MODO EDIÇÃO: Toque no mapa p/ criar ou no pino p/ apagar"
            Toast.makeText(context, "Modo de Edição ATIVADO", Toast.LENGTH_SHORT).show()
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

        // Configura o clique no mapa (Criar Ponto)
        mMap.setOnMapClickListener { latLng ->
            if (isEditMode) {
                showAddSpotDialog(latLng)
            }
        }

        // Configura o clique no marcador (Deletar Ponto)
        mMap.setOnMarkerClickListener { marker ->
            if (isEditMode) {
                showDeleteSpotDialog(marker)
                true // Consome o evento (não abre a info window padrão)
            } else {
                false // Deixa o comportamento padrão (abrir info window com nome/autor)
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

    private fun showDeleteSpotDialog(marker: Marker) {
        val spot = marker.tag as? ChargerSpot
        if (spot != null) {
            AlertDialog.Builder(context)
                .setTitle("Excluir Ponto")
                .setMessage("Deseja remover o ponto '${spot.name}'?")
                .setPositiveButton("Excluir") { _, _ ->
                    deleteSpot(spot, marker)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
             // Caso o marcador não tenha tag (ex: marcadores estáticos ou erro de carga), remove só visualmente se desejar, 
             // mas aqui vamos assumir que só deletamos o que está no banco.
             Toast.makeText(context, "Não é possível remover este ponto.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSpot(name: String, latLng: LatLng) {
        val db = AppDatabase.getDatabase(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        lifecycleScope.launch {
            // Busca o nome do usuário criador
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
            
            // Recarrega os pontos para garantir que temos o ID correto na tag do marcador
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
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(spot.name)
                        .snippet("Adicionado por: ${spot.addedBy}")
                )
                // Salva o objeto ChargerSpot dentro do marcador para podermos deletá-lo depois
                marker?.tag = spot
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}