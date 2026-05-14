package com.janaushadhi.finder.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.janaushadhi.finder.R
import com.janaushadhi.finder.data.model.Store
import com.janaushadhi.finder.databinding.FragmentMapBinding
import com.janaushadhi.finder.ui.main.MainActivity
import com.janaushadhi.finder.utils.LocationHelper

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private var selectedStore: Store? = null
    private val markerStores = mutableMapOf<Marker, Store>()
    private val fusedLocation by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) moveToUserLocation() else Snackbar.make(binding.root, "Location permission is needed to show nearby stores", Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (childFragmentManager.findFragmentById(R.id.mapViewFragment) as SupportMapFragment).getMapAsync(this)
        val medicine = (activity as? MainActivity)?.consumePendingMedicineName().orEmpty()
        if (medicine.isNotBlank()) {
            viewModel.setMedicine(medicine)
            binding.medicineSearchText.text = "Searching for $medicine in nearby stores"
            binding.medicineSearchText.visibility = View.VISIBLE
        }
        binding.openNowSwitch.setOnCheckedChangeListener { _, checked -> viewModel.setOpenOnly(checked) }
        binding.myLocationFab.setOnClickListener { requestOrMoveToLocation() }
        binding.callStoreButton.setOnClickListener { selectedStore?.let { callStore(it.phone) } }
        binding.requestStockButton.setOnClickListener { selectedStore?.let { showRequestDialog(it) } }
        binding.southIndiaButton.setOnClickListener { showSouthIndiaKendras() }
        binding.northIndiaButton.setOnClickListener { showNorthIndiaKendras() }
        
        // Add Show All India button (using existing FAB or creating new action)
        binding.myLocationFab.setOnLongClickListener {
            showAllIndiaKendras()
            true
        }
        viewModel.stores.observe(viewLifecycleOwner) { renderStores(it) }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is MapUiState.Error) Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false
        map.setOnMarkerClickListener { marker ->
            markerStores[marker]?.let { showStore(it) }
            true
        }
        requestOrMoveToLocation()
    }

    private fun requestOrMoveToLocation() {
        if (LocationHelper.hasFineLocationPermission(requireContext())) moveToUserLocation()
        else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun moveToUserLocation() {
        if (!LocationHelper.hasFineLocationPermission(requireContext())) return
        googleMap?.isMyLocationEnabled = true
        fusedLocation.lastLocation.addOnSuccessListener { location ->
            val latLng = if (location != null) LatLng(location.latitude, location.longitude) else LatLng(28.6139, 77.2090)
            viewModel.setLocation(latLng.latitude, latLng.longitude)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        }
    }

    private fun renderStores(stores: List<Store>) {
        val map = googleMap ?: return
        map.clear()
        markerStores.clear()
        stores.forEach { store ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(store.latitude, store.longitude))
                    .title(store.name)
                    .snippet(store.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            if (marker != null) markerStores[marker] = store
        }
        if (stores.isNotEmpty()) showStore(stores.first())
    }

    private fun showStore(store: Store) {
        selectedStore = store
        viewModel.viewStore(store)
        binding.storeCard.visibility = View.VISIBLE
        binding.storeNameText.text = store.name
        binding.storeAddressText.text = store.address
        binding.storeDistanceText.text = String.format("%.1f km away • %s", store.distanceKm, if (store.isOpenNow) "Open now" else "Closed")
        binding.storePhoneText.text = store.phone
    }

    private fun callStore(phone: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    private fun showRequestDialog(store: Store) {
        val medicine = viewModel.medicineName.ifBlank { "selected medicine" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Request stock")
            .setMessage("Request sent to ${store.name} for $medicine. They will contact you within 24 hours.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Send") { _, _ ->
                viewModel.requestStock(store) { message -> Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show() }
            }
            .show()
    }

    private fun showAllIndiaKendras() {
        // Show all kendras regardless of location
        viewModel.showAllStores()
        // Center map on India
        val indiaCenter = LatLng(20.5937, 78.9629)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(indiaCenter, 5f))
        Snackbar.make(binding.root, "Showing all Jan Aushadhi Kendras across India", Snackbar.LENGTH_LONG).show()
    }

    private fun showKarnatakaKendras() {
        // Show only Karnataka kendras
        viewModel.showKarnatakaStores()
        // Center map on Karnataka
        val karnatakaCenter = LatLng(15.3173, 75.7138)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(karnatakaCenter, 7f))
        Snackbar.make(binding.root, "Showing all Jan Aushadhi Kendras in Karnataka", Snackbar.LENGTH_LONG).show()
    }

    private fun showSouthIndiaKendras() {
        // Show South Indian kendras
        viewModel.showSouthIndiaStores()
        // Center map on South India
        val southIndiaCenter = LatLng(15.3173, 76.7138)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(southIndiaCenter, 6f))
        Snackbar.make(binding.root, "Showing all Jan Aushadhi Kendras in South India (Karnataka, Kerala, Tamil Nadu, Andhra Pradesh, Telangana, Goa, Maharashtra)", Snackbar.LENGTH_LONG).show()
    }

    private fun showNorthIndiaKendras() {
        // Show North Indian kendras
        viewModel.showNorthIndiaStores()
        // Center map on North India
        val northIndiaCenter = LatLng(28.6139, 77.2090)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(northIndiaCenter, 6f))
        Snackbar.make(binding.root, "Showing all Jan Aushadhi Kendras in North India (Delhi, Uttar Pradesh, Punjab, Haryana, Rajasthan, Uttarakhand, Himachal Pradesh, Jammu & Kashmir, Chandigarh)", Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
