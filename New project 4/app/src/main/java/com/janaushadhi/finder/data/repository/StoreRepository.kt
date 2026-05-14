package com.janaushadhi.finder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.janaushadhi.finder.data.model.Store
import com.janaushadhi.finder.utils.LocationHelper
import kotlinx.coroutines.tasks.await

class StoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getStores(): Result<List<Store>> = runCatching {
        seedSampleStoresIfNeeded()
        firestore.collection("stores").get().await().documents.map { doc ->
            doc.toObject(Store::class.java)?.copy(id = doc.id) ?: Store(id = doc.id)
        }.filter { it.name.isNotBlank() }
    }

    suspend fun requestStock(userId: String, store: Store, medicineName: String): Result<Unit> = runCatching {
        firestore.collection("stockRequests").add(
            mapOf(
                "userId" to userId,
                "storeName" to store.name,
                "medicineName" to medicineName,
                "timestamp" to Timestamp.now(),
                "status" to "pending"
            )
        ).await()
    }

    fun withinRadius(stores: List<Store>, lat: Double, lon: Double, radiusKm: Double, openOnly: Boolean): List<Store> {
        return stores.map { store ->
            store.copy(distanceKm = LocationHelper.haversineDistance(lat, lon, store.latitude, store.longitude))
        }.filter { it.distanceKm <= radiusKm && (!openOnly || it.isOpenNow) }
            .sortedBy { it.distanceKm }
    }

    private suspend fun seedSampleStoresIfNeeded() {
        val existing = firestore.collection("stores").limit(1).get().await()
        if (!existing.isEmpty) return
        sampleStores.forEach { store ->
            firestore.collection("stores").add(
                mapOf(
                    "name" to store.name,
                    "address" to store.address,
                    "latitude" to store.latitude,
                    "longitude" to store.longitude,
                    "phone" to store.phone,
                    "isOpenNow" to store.isOpenNow,
                    "district" to store.district,
                    "state" to store.state
                )
            ).await()
        }
    }

    private val sampleStores = listOf(
        // Delhi NCR
        Store(name = "PMBJP Jan-Aushadhi Kendra AIIMS Delhi", address = "AIIMS Metro Gate 2, Ansari Nagar, New Delhi", latitude = 28.5672, longitude = 77.2100, phone = "+91 9810010001", isOpenNow = true, district = "New Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra GTB Hospital", address = "GTB Hospital, Dilshad Garden, Delhi", latitude = 28.6808, longitude = 77.3194, phone = "+91 9810010002", isOpenNow = true, district = "East Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Safdarjung", address = "Safdarjung Hospital, New Delhi", latitude = 28.5623, longitude = 77.2091, phone = "+91 9810010003", isOpenNow = false, district = "New Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra LBS Hospital", address = "LBS Hospital, Gurgaon", latitude = 28.4595, longitude = 77.0266, phone = "+91 9810010004", isOpenNow = true, district = "Gurgaon", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Noida", address = "Sector 18, Noida", latitude = 28.5665, longitude = 77.3203, phone = "+91 9810010005", isOpenNow = true, district = "Gautam Buddha Nagar", state = "Uttar Pradesh"),

        // Maharashtra
        Store(name = "PMBJP Jan-Aushadhi Kendra Dadar Mumbai", address = "Dadar West, Mumbai", latitude = 19.0180, longitude = 72.8448, phone = "+91 9820010001", isOpenNow = true, district = "Mumbai", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Pune", address = "Shivajinagar, Pune", latitude = 18.5308, longitude = 73.8475, phone = "+91 9820010002", isOpenNow = true, district = "Pune", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nagpur", address = "Medical Square, Nagpur", latitude = 21.1458, longitude = 79.0882, phone = "+91 9820010003", isOpenNow = false, district = "Nagpur", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nashik", address = "College Road, Nashik", latitude = 19.9975, longitude = 73.7898, phone = "+91 9820010004", isOpenNow = true, district = "Nashik", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Aurangabad", address = "CIDCO Area, Aurangabad", latitude = 19.8762, longitude = 75.3433, phone = "+91 9820010005", isOpenNow = true, district = "Aurangabad", state = "Maharashtra"),

        // Karnataka
        Store(name = "PMBJP Jan-Aushadhi Kendra Bengaluru", address = "Jayanagar 4th Block, Bengaluru", latitude = 12.9250, longitude = 77.5938, phone = "+91 9830010001", isOpenNow = false, district = "Bengaluru Urban", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mysuru", address = "Devaraja Mohalla, Mysuru", latitude = 12.3105, longitude = 76.6571, phone = "+91 9830010002", isOpenNow = true, district = "Mysuru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Hubballi", address = "Keshwapur, Hubballi", latitude = 15.3647, longitude = 75.1240, phone = "+91 9830010003", isOpenNow = true, district = "Dharwad", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mangaluru", address = "Hampankatta, Mangaluru", latitude = 12.9141, longitude = 74.8560, phone = "+91 9830010004", isOpenNow = false, district = "Mangaluru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Belagavi", address = "Khanapur Road, Belagavi", latitude = 15.8496, longitude = 74.4977, phone = "+91 9830010005", isOpenNow = true, district = "Belagavi", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kalaburagi", address = "Super Market, Kalaburagi", latitude = 17.3297, longitude = 76.8364, phone = "+91 9830010006", isOpenNow = true, district = "Kalaburagi", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ballari", address = "Cantonment, Ballari", latitude = 15.1394, longitude = 76.9214, phone = "+91 9830010007", isOpenNow = false, district = "Ballari", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Shivamogga", address = "Gandhi Bazar, Shivamogga", latitude = 13.9490, longitude = 75.5644, phone = "+91 9830010008", isOpenNow = true, district = "Shivamogga", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Tumakuru", address = "Circle, Tumakuru", latitude = 13.3393, longitude = 77.1216, phone = "+91 9830010009", isOpenNow = false, district = "Tumakuru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Davanagere", address = "Market Road, Davanagere", latitude = 14.4663, longitude = 75.9215, phone = "+91 9830010010", isOpenNow = true, district = "Davanagere", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chitradurga", address = "Fort Area, Chitradurga", latitude = 14.1295, longitude = 76.3965, phone = "+91 9830010011", isOpenNow = false, district = "Chitradurga", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kolar", address = "Market, Kolar", latitude = 13.1365, longitude = 78.1308, phone = "+91 9830010012", isOpenNow = true, district = "Kolar", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bidar", address = "Gandhi Gunj, Bidar", latitude = 17.9129, longitude = 77.5176, phone = "+91 9830010013", isOpenNow = false, district = "Bidar", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Raichur", address = "Station Road, Raichur", latitude = 16.2075, longitude = 77.3436, phone = "+91 9830010014", isOpenNow = true, district = "Raichur", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayapura", address = "Bazar, Vijayapura", latitude = 16.8350, longitude = 75.7138, phone = "+91 9830010015", isOpenNow = false, district = "Vijayapura", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bagalkot", address = "Main Road, Bagalkot", latitude = 16.1853, longitude = 75.6944, phone = "+91 9830010016", isOpenNow = true, district = "Bagalkot", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dharwad", address = "KSD, Dharwad", latitude = 15.4590, longitude = 75.0079, phone = "+91 9830010017", isOpenNow = false, district = "Dharwad", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Uttara Kannada", address = "Karwar, Uttara Kannada", latitude = 14.8193, longitude = 74.1320, phone = "+91 9830010018", isOpenNow = true, district = "Uttara Kannada", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Udupi", address = "Car Street, Udupi", latitude = 13.3409, longitude = 74.7421, phone = "+91 9830010019", isOpenNow = false, district = "Udupi", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dakshina Kannada", address = "Puttur, Dakshina Kannada", latitude = 12.7720, longitude = 75.2026, phone = "+91 9830010020", isOpenNow = true, district = "Dakshina Kannada", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chikballapur", address = "Town, Chikballapur", latitude = 13.3322, longitude = 77.7266, phone = "+91 9830010021", isOpenNow = false, district = "Chikballapur", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chikkamagaluru", address = "Main Road, Chikkamagaluru", latitude = 13.3186, longitude = 75.7738, phone = "+91 9830010022", isOpenNow = true, district = "Chikkamagaluru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Hassan", address = "Market, Hassan", latitude = 13.0047, longitude = 76.0896, phone = "+91 9830010023", isOpenNow = false, district = "Hassan", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kodagu", address = "Madikeri, Kodagu", latitude = 12.4244, longitude = 75.7392, phone = "+91 9830010024", isOpenNow = true, district = "Kodagu", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mandya", address = "Bazar, Mandya", latitude = 12.5385, longitude = 76.8954, phone = "+91 9830010025", isOpenNow = false, district = "Mandya", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ramanagara", address = "Town, Ramanagara", latitude = 12.7996, longitude = 77.2756, phone = "+91 9830010026", isOpenNow = true, district = "Ramanagara", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chamarajanagara", address = "Bazar, Chamarajanagara", latitude = 12.1775, longitude = 76.9458, phone = "+91 9830010027", isOpenNow = false, district = "Chamarajanagara", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bengaluru Rural", address = "Nelamangala, Bengaluru Rural", latitude = 13.1036, longitude = 77.3826, phone = "+91 9830010028", isOpenNow = true, district = "Bengaluru Rural", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Yadgir", address = "Town, Yadgir", latitude = 16.7700, longitude = 77.1376, phone = "+91 9830010029", isOpenNow = false, district = "Yadgir", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Koppal", address = "Main Road, Koppal", latitude = 15.3520, longitude = 76.2506, phone = "+91 9830010030", isOpenNow = true, district = "Koppal", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haveri", address = "Town, Haveri", latitude = 14.7989, longitude = 75.0248, phone = "+91 9830010031", isOpenNow = false, district = "Haveri", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gadag", address = "Bazar, Gadag", latitude = 15.4266, longitude = 75.6308, phone = "+91 9830010032", isOpenNow = true, district = "Gadag", state = "Karnataka"),

        // Tamil Nadu
        Store(name = "PMBJP Jan-Aushadhi Kendra Chennai", address = "T Nagar, Chennai", latitude = 13.0418, longitude = 80.2341, phone = "+91 9860010001", isOpenNow = false, district = "Chennai", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Coimbatore", address = "Gandhipuram, Coimbatore", latitude = 11.0168, longitude = 76.9558, phone = "+91 9860010002", isOpenNow = true, district = "Coimbatore", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Madurai", address = "KK Nagar, Madurai", latitude = 9.9252, longitude = 78.1198, phone = "+91 9860010003", isOpenNow = true, district = "Madurai", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Tiruchirappalli", address = "Srirangam, Tiruchirappalli", latitude = 10.7905, longitude = 78.7047, phone = "+91 9860010004", isOpenNow = false, district = "Tiruchirappalli", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Salem", address = "Fairlands, Salem", latitude = 11.6665, longitude = 78.1460, phone = "+91 9860010005", isOpenNow = true, district = "Salem", state = "Tamil Nadu"),

        // Uttar Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Lucknow", address = "Hazratganj, Lucknow", latitude = 26.8467, longitude = 80.9462, phone = "+91 9900010001", isOpenNow = true, district = "Lucknow", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kanpur", address = "Mall Road, Kanpur", latitude = 26.4499, longitude = 80.3319, phone = "+91 9900010002", isOpenNow = false, district = "Kanpur Nagar", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ghaziabad", address = "Raj Nagar, Ghaziabad", latitude = 28.6692, longitude = 77.4538, phone = "+91 9900010003", isOpenNow = true, district = "Ghaziabad", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Agra", address = "Sanjay Place, Agra", latitude = 27.1767, longitude = 78.0081, phone = "+91 9900010004", isOpenNow = true, district = "Agra", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Varanasi", address = "Cantt, Varanasi", latitude = 25.3176, longitude = 82.9739, phone = "+91 9900010005", isOpenNow = false, district = "Varanasi", state = "Uttar Pradesh"),

        // West Bengal
        Store(name = "PMBJP Jan-Aushadhi Kendra Kolkata", address = "Salt Lake Sector V, Kolkata", latitude = 22.5726, longitude = 88.4332, phone = "+91 9870010001", isOpenNow = true, district = "Kolkata", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Siliguri", address = "Hill Cart Road, Siliguri", latitude = 26.7271, longitude = 88.3953, phone = "+91 9870010002", isOpenNow = true, district = "Darjeeling", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Durgapur", address = "City Centre, Durgapur", latitude = 23.5204, longitude = 87.3119, phone = "+91 9870010003", isOpenNow = false, district = "Paschim Bardhaman", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Asansol", address = "Burnpur Road, Asansol", latitude = 23.6833, longitude = 86.9660, phone = "+91 9870010004", isOpenNow = true, district = "Paschim Bardhaman", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Howrah", address = "Andul Road, Howrah", latitude = 22.5958, longitude = 88.2636, phone = "+91 9870010005", isOpenNow = true, district = "Howrah", state = "West Bengal"),

        // Telangana
        Store(name = "PMBJP Jan-Aushadhi Kendra Hyderabad", address = "Ameerpet Main Road, Hyderabad", latitude = 17.4375, longitude = 78.4483, phone = "+91 9840010001", isOpenNow = true, district = "Hyderabad", state = "Telangana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Warangal", address = "Kazipet, Warangal", latitude = 18.0004, longitude = 79.5876, phone = "+91 9840010002", isOpenNow = false, district = "Warangal", state = "Telangana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nizamabad", address = "Armoor Road, Nizamabad", latitude = 18.6725, longitude = 78.0942, phone = "+91 9840010003", isOpenNow = true, district = "Nizamabad", state = "Telangana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Karimnagar", address = "Manakondur Road, Karimnagar", latitude = 18.4386, longitude = 79.1282, phone = "+91 9840010004", isOpenNow = true, district = "Karimnagar", state = "Telangana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Khammam", address = "Wyra Road, Khammam", latitude = 17.2473, longitude = 80.1513, phone = "+91 9840010005", isOpenNow = false, district = "Khammam", state = "Telangana"),

        // Rajasthan
        Store(name = "PMBJP Jan-Aushadhi Kendra Jaipur", address = "MI Road, Jaipur", latitude = 26.9124, longitude = 75.7873, phone = "+91 9890010001", isOpenNow = false, district = "Jaipur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jodhpur", address = "Sardarpura, Jodhpur", latitude = 26.2389, longitude = 73.0243, phone = "+91 9890010002", isOpenNow = true, district = "Jodhpur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Udaipur", address = "Hiran Magri, Udaipur", latitude = 24.5854, longitude = 73.7128, phone = "+91 9890010003", isOpenNow = true, district = "Udaipur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kota", address = "Vigyan Nagar, Kota", latitude = 25.2138, longitude = 75.8648, phone = "+91 9890010004", isOpenNow = false, district = "Kota", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ajmer", address = "Kutchery Road, Ajmer", latitude = 26.4499, longitude = 74.6399, phone = "+91 9890010005", isOpenNow = true, district = "Ajmer", state = "Rajasthan"),

        // Gujarat
        Store(name = "PMBJP Jan-Aushadhi Kendra Ahmedabad", address = "Navrangpura, Ahmedabad", latitude = 23.0225, longitude = 72.5714, phone = "+91 9910010001", isOpenNow = true, district = "Ahmedabad", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Surat", address = "Adajan, Surat", latitude = 21.1702, longitude = 72.8311, phone = "+91 9910010002", isOpenNow = false, district = "Surat", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vadodara", address = "Alkapuri, Vadodara", latitude = 22.3072, longitude = 73.1812, phone = "+91 9910010003", isOpenNow = true, district = "Vadodara", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rajkot", address = "Kalawad Road, Rajkot", latitude = 22.3039, longitude = 70.8022, phone = "+91 9910010004", isOpenNow = true, district = "Rajkot", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gandhinagar", address = "Sector 21, Gandhinagar", latitude = 23.2156, longitude = 72.6369, phone = "+91 9910010005", isOpenNow = false, district = "Gandhinagar", state = "Gujarat"),

        // Andhra Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Amaravati", address = "Secretariat Area, Amaravati", latitude = 16.5062, longitude = 80.5167, phone = "+91 9920010001", isOpenNow = true, district = "Guntur", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Visakhapatnam", address = "Dwaraka Nagar, Visakhapatnam", latitude = 17.6868, longitude = 83.2185, phone = "+91 9920010002", isOpenNow = true, district = "Visakhapatnam", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayawada", address = "Benz Circle, Vijayawada", latitude = 16.5062, longitude = 80.6480, phone = "+91 9920010003", isOpenNow = false, district = "Krishna", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Guntur", address = "Arundalpet, Guntur", latitude = 16.3067, longitude = 80.4366, phone = "+91 9920010004", isOpenNow = true, district = "Guntur", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nellore", address = "Potti Sriramulu Road, Nellore", latitude = 14.4445, longitude = 79.9856, phone = "+91 9920010005", isOpenNow = true, district = "Nellore", state = "Andhra Pradesh"),

        // Kerala
        Store(name = "PMBJP Jan-Aushadhi Kendra Thiruvananthapuram", address = "Medical College, Thiruvananthapuram", latitude = 8.5241, longitude = 76.9366, phone = "+91 9930010001", isOpenNow = true, district = "Thiruvananthapuram", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kochi", address = "MG Road, Kochi", latitude = 9.9674, longitude = 76.2454, phone = "+91 9930010002", isOpenNow = false, district = "Ernakulam", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kozhikode", address = "Medical College, Kozhikode", latitude = 11.2588, longitude = 75.7804, phone = "+91 9930010003", isOpenNow = true, district = "Kozhikode", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Thrissur", address = "Sakthan Nagar, Thrissur", latitude = 10.5276, longitude = 76.2144, phone = "+91 9930010004", isOpenNow = true, district = "Thrissur", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kollam", address = "Chinnakada, Kollam", latitude = 8.8803, longitude = 76.6033, phone = "+91 9930010005", isOpenNow = false, district = "Kollam", state = "Kerala"),

        // Madhya Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhopal", address = "MP Nagar, Bhopal", latitude = 23.2599, longitude = 77.4126, phone = "+91 9940010001", isOpenNow = true, district = "Bhopal", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Indore", address = "Vijay Nagar, Indore", latitude = 22.7196, longitude = 75.8577, phone = "+91 9940010002", isOpenNow = false, district = "Indore", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gwalior", address = "City Center, Gwalior", latitude = 26.2124, longitude = 78.1772, phone = "+91 9940010003", isOpenNow = true, district = "Gwalior", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jabalpur", address = "Cantt, Jabalpur", latitude = 23.1815, longitude = 79.9864, phone = "+91 9940010004", isOpenNow = true, district = "Jabalpur", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ujjain", address = "Freeganj, Ujjain", latitude = 23.1828, longitude = 75.7778, phone = "+91 9940010005", isOpenNow = false, district = "Ujjain", state = "Madhya Pradesh"),

        // Punjab
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 17, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 9950010001", isOpenNow = true, district = "Chandigarh", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ludhiana", address = "Civil Lines, Ludhiana", latitude = 30.9010, longitude = 75.8573, phone = "+91 9950010002", isOpenNow = true, district = "Ludhiana", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Gate, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 9950010003", isOpenNow = false, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jalandhar", address = "Model Town, Jalandhar", latitude = 31.3260, longitude = 75.5762, phone = "+91 9950010004", isOpenNow = true, district = "Jalandhar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Patiala", address = "Lehal Bhawan, Patiala", latitude = 30.3398, longitude = 76.3869, phone = "+91 9950010005", isOpenNow = true, district = "Patiala", state = "Punjab"),

        // Haryana
        Store(name = "PMBJP Jan-Aushadhi Kendra Gurugram", address = "Sector 14, Gurugram", latitude = 28.4595, longitude = 77.0266, phone = "+91 9960010001", isOpenNow = true, district = "Gurugram", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Faridabad", address = "Sector 15, Faridabad", latitude = 28.4089, longitude = 77.3178, phone = "+91 9960010002", isOpenNow = false, district = "Faridabad", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Panipat", address = "Model Town, Panipat", latitude = 29.3909, longitude = 76.9635, phone = "+91 9960010003", isOpenNow = true, district = "Panipat", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ambala", address = "Ambala City, Ambala", latitude = 30.3782, longitude = 76.7806, phone = "+91 9960010004", isOpenNow = true, district = "Ambala", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Karnal", address = "Sector 12, Karnal", latitude = 29.6857, longitude = 76.9905, phone = "+91 9960010005", isOpenNow = false, district = "Karnal", state = "Haryana"),

        // Bihar
        Store(name = "PMBJP Jan-Aushadhi Kendra Patna", address = "Boring Road, Patna", latitude = 25.5941, longitude = 85.1376, phone = "+91 9970010001", isOpenNow = true, district = "Patna", state = "Bihar"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gaya", address = "Gandhi Maidan, Gaya", latitude = 24.7944, longitude = 85.0012, phone = "+91 9970010002", isOpenNow = true, district = "Gaya", state = "Bihar"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhagalpur", address = "Kadamkuan, Bhagalpur", latitude = 25.2420, longitude = 86.9250, phone = "+91 9970010003", isOpenNow = false, district = "Bhagalpur", state = "Bihar"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Muzaffarpur", address = "Saraiyaghat, Muzaffarpur", latitude = 26.1226, longitude = 85.3900, phone = "+91 9970010004", isOpenNow = true, district = "Muzaffarpur", state = "Bihar"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Purnia", address = "Kasba, Purnia", latitude = 25.7849, longitude = 87.4766, phone = "+91 9970010005", isOpenNow = true, district = "Purnia", state = "Bihar"),

        // Odisha
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhubaneswar", address = "Unit 1, Bhubaneswar", latitude = 20.2961, longitude = 85.8245, phone = "+91 9980010001", isOpenNow = true, district = "Khordha", state = "Odisha"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Cuttack", address = "Madhupatna, Cuttack", latitude = 20.4625, longitude = 85.8833, phone = "+91 9980010002", isOpenNow = false, district = "Cuttack", state = "Odisha"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rourkela", address = "Civil Township, Rourkela", latitude = 22.2579, longitude = 84.8494, phone = "+91 9980010003", isOpenNow = true, district = "Sundergarh", state = "Odisha"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Berhampur", address = "Biju Patnaik Road, Berhampur", latitude = 19.3047, longitude = 84.7974, phone = "+91 9980010004", isOpenNow = true, district = "Ganjam", state = "Odisha"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Sambalpur", address = "Ainthapali, Sambalpur", latitude = 21.4662, longitude = 83.9769, phone = "+91 9980010005", isOpenNow = false, district = "Sambalpur", state = "Odisha"),

        // Assam
        Store(name = "PMBJP Jan-Aushadhi Kendra Guwahati", address = "Dispur, Guwahati", latitude = 26.1445, longitude = 91.7362, phone = "+91 9990010001", isOpenNow = true, district = "Kamrup Metropolitan", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dibrugarh", address = "Mancotta Road, Dibrugarh", latitude = 27.4728, longitude = 94.9120, phone = "+91 9990010002", isOpenNow = true, district = "Dibrugarh", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Silchar", address = "Park Road, Silchar", latitude = 24.8317, longitude = 92.7781, phone = "+91 9990010003", isOpenNow = false, district = "Cachar", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jorhat", address = "Club Road, Jorhat", latitude = 26.7386, longitude = 94.2017, phone = "+91 9990010004", isOpenNow = true, district = "Jorhat", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nagaon", address = "Haibargaon, Nagaon", latitude = 26.3488, longitude = 92.6822, phone = "+91 9990010005", isOpenNow = true, district = "Nagaon", state = "Assam"),

        // Jharkhand
        Store(name = "PMBJP Jan-Aushadhi Kendra Ranchi", address = "Main Road, Ranchi", latitude = 23.3441, longitude = 85.3096, phone = "+91 991100001", isOpenNow = true, district = "Ranchi", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jamshedpur", address = "Sakchi, Jamshedpur", latitude = 22.8046, longitude = 86.2029, phone = "+91 991100002", isOpenNow = false, district = "East Singhbhum", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dhanbad", address = "Bank More, Dhanbad", latitude = 23.7957, longitude = 86.4304, phone = "+91 991100003", isOpenNow = true, district = "Dhanbad", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bokaro", address = "Sector 4, Bokaro", latitude = 23.7888, longitude = 85.9606, phone = "+91 991100004", isOpenNow = true, district = "Bokaro", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Deoghar", address = "Tower Chowk, Deoghar", latitude = 24.4889, longitude = 86.7042, phone = "+91 991100005", isOpenNow = false, district = "Deoghar", state = "Jharkhand"),

        // Chhattisgarh
        Store(name = "PMBJP Jan-Aushadhi Kendra Raipur", address = "Pandri, Raipur", latitude = 21.2514, longitude = 81.6296, phone = "+91 991200001", isOpenNow = true, district = "Raipur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bilaspur", address = "Link Road, Bilaspur", latitude = 22.0800, longitude = 82.1591, phone = "+91 991200002", isOpenNow = true, district = "Bilaspur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Durg", address = "Bhilai, Durg", latitude = 21.1901, longitude = 81.2849, phone = "+91 991200003", isOpenNow = false, district = "Durg", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhilai", address = "Sector 6, Bhilai", latitude = 21.2145, longitude = 81.4492, phone = "+91 991200004", isOpenNow = true, district = "Durg", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Korba", address = "GE Road, Korba", latitude = 22.3539, longitude = 82.6898, phone = "+91 991200005", isOpenNow = true, district = "Korba", state = "Chhattisgarh"),

        // Uttarakhand
        Store(name = "PMBJP Jan-Aushadhi Kendra Dehradun", address = "Rajpur Road, Dehradun", latitude = 30.3165, longitude = 78.0322, phone = "+91 991300001", isOpenNow = true, district = "Dehradun", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haridwar", address = "Jwalapur, Haridwar", latitude = 29.9456, longitude = 78.1632, phone = "+91 991300002", isOpenNow = false, district = "Haridwar", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Roorkee", address = "Civil Lines, Roorkee", latitude = 29.8465, longitude = 77.8873, phone = "+91 991300003", isOpenNow = true, district = "Haridwar", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haldwani", address = "Gandhi Colony, Haldwani", latitude = 29.2166, longitude = 79.5146, phone = "+91 991300004", isOpenNow = true, district = "Nainital", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kashipur", address = "Kashipur, Udham Singh Nagar", latitude = 29.2166, longitude = 78.9629, phone = "+91 991300005", isOpenNow = false, district = "Udham Singh Nagar", state = "Uttarakhand"),

        // Himachal Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Shimla", address = "The Mall, Shimla", latitude = 31.1048, longitude = 77.1734, phone = "+91 991400001", isOpenNow = true, district = "Shimla", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Solan", address = "Mall Road, Solan", latitude = 30.9045, longitude = 77.0990, phone = "+91 991400002", isOpenNow = false, district = "Solan", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mandi", address = "Indira Market, Mandi", latitude = 31.7206, longitude = 76.9184, phone = "+91 991400003", isOpenNow = true, district = "Mandi", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dharamshala", address = "Kotwali Bazar, Dharamshala", latitude = 32.2195, longitude = 76.3237, phone = "+91 991400004", isOpenNow = true, district = "Kangra", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Una", address = "Una Town, Una", latitude = 31.4709, longitude = 76.2782, phone = "+91 991400005", isOpenNow = false, district = "Una", state = "Himachal Pradesh"),

        // Jammu & Kashmir
        Store(name = "PMBJP Jan-Aushadhi Kendra Srinagar", address = "Lal Chowk, Srinagar", latitude = 34.0837, longitude = 74.7973, phone = "+91 991500001", isOpenNow = true, district = "Srinagar", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jammu", address = "BC Road, Jammu", latitude = 32.7266, longitude = 74.8570, phone = "+91 991500002", isOpenNow = false, district = "Jammu", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Anantnag", address = "Lal Chowk, Anantnag", latitude = 33.7351, longitude = 75.1465, phone = "+91 991500003", isOpenNow = true, district = "Anantnag", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Baramulla", address = "Main Market, Baramulla", latitude = 34.1980, longitude = 74.3617, phone = "+91 991500004", isOpenNow = true, district = "Baramulla", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kathua", address = "Kathua Town, Kathua", latitude = 32.3929, longitude = 75.5254, phone = "+91 991500005", isOpenNow = false, district = "Kathua", state = "Jammu & Kashmir"),

        // Goa
        Store(name = "PMBJP Jan-Aushadhi Kendra Panaji", address = "18th June Road, Panaji", latitude = 15.4909, longitude = 73.8278, phone = "+91 991600001", isOpenNow = true, district = "North Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Margao", address = "Margao Market, Margao", latitude = 15.2993, longitude = 73.9159, phone = "+91 991600002", isOpenNow = false, district = "South Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vasco", address = "Vasco Market, Vasco", latitude = 15.4042, longitude = 73.8356, phone = "+91 991600003", isOpenNow = true, district = "South Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mapusa", address = "Mapusa Market, Mapusa", latitude = 15.6005, longitude = 73.8252, phone = "+91 991600004", isOpenNow = true, district = "North Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ponda", address = "Ponda Market, Ponda", latitude = 15.3984, longitude = 73.9890, phone = "+91 991600005", isOpenNow = false, district = "North Goa", state = "Goa"),

        // Northeast States
        Store(name = "PMBJP Jan-Aushadhi Kendra Aizawl", address = "Zarkawt, Aizawl", latitude = 23.7271, longitude = 92.7176, phone = "+91 991700001", isOpenNow = true, district = "Aizawl", state = "Mizoram"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kohima", address = "Kohima Town, Kohima", latitude = 25.6701, longitude = 94.1078, phone = "+91 991700002", isOpenNow = false, district = "Kohima", state = "Nagaland"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Imphal", address = "Imphal Market, Imphal", latitude = 24.8170, longitude = 93.9368, phone = "+91 991700003", isOpenNow = true, district = "Imphal West", state = "Manipur"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Shillong", address = "Police Bazar, Shillong", latitude = 25.5788, longitude = 91.8933, phone = "+91 991700004", isOpenNow = true, district = "East Khasi Hills", state = "Meghalaya"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Agartala", address = "Agartala Market, Agartala", latitude = 23.8315, longitude = 91.2868, phone = "+91 991700005", isOpenNow = false, district = "West Tripura", state = "Tripura"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Itanagar", address = "Naharlagun, Itanagar", latitude = 27.0844, longitude = 93.6053, phone = "+91 991700006", isOpenNow = true, district = "Papum Pare", state = "Arunachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gangtok", address = "MG Marg, Gangtok", latitude = 27.3389, longitude = 88.6065, phone = "+91 991700007", isOpenNow = true, district = "East Sikkim", state = "Sikkim"),

        // Union Territories
        Store(name = "PMBJP Jan-Aushadhi Kendra Port Blair", address = "Aberdeen Bazar, Port Blair", latitude = 11.6415, longitude = 92.7298, phone = "+91 991800001", isOpenNow = true, district = "South Andaman", state = "Andaman & Nicobar Islands"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kavaratti", address = "Kavaratti Town, Kavaratti", latitude = 10.5667, longitude = 72.6417, phone = "+91 991800002", isOpenNow = false, district = "Lakshadweep", state = "Lakshadweep"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Silvassa", address = "Silvassa Town, Silvassa", latitude = 20.2775, longitude = 73.0164, phone = "+91 991800003", isOpenNow = true, district = "Dadra & Nagar Haveli", state = "Dadra & Nagar Haveli"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Daman", address = "Daman Town, Daman", latitude = 20.4283, longitude = 72.8397, phone = "+91 991800004", isOpenNow = true, district = "Daman", state = "Daman & Diu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Diu", address = "Diu Town, Diu", latitude = 20.7143, longitude = 70.9835, phone = "+91 991800005", isOpenNow = false, district = "Diu", state = "Daman & Diu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 22, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 991800006", isOpenNow = true, district = "Chandigarh", state = "Chandigarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Delhi", address = "Connaught Place, New Delhi", latitude = 28.6308, longitude = 77.2090, phone = "+91 991800007", isOpenNow = true, district = "New Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Puducherry", address = "Mission Street, Puducherry", latitude = 11.9416, longitude = 79.8083, phone = "+91 991800008", isOpenNow = false, district = "Puducherry", state = "Puducherry"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Karaikal", address = "Karaikal Town, Karaikal", latitude = 10.9253, longitude = 79.8394, phone = "+91 991800009", isOpenNow = true, district = "Karaikal", state = "Puducherry"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mahe", address = "Mahe Town, Mahe", latitude = 11.7081, longitude = 75.5318, phone = "+91 991800010", isOpenNow = true, district = "Mahe", state = "Puducherry"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Yanam", address = "Yanam Town, Yanam", latitude = 16.7330, longitude = 82.2138, phone = "+91 991800011", isOpenNow = false, district = "Yanam", state = "Puducherry"),

        // Additional Major Cities
        Store(name = "PMBJP Jan-Aushadhi Kendra Indore", address = "Palasia, Indore", latitude = 22.7196, longitude = 75.8577, phone = "+91 992001001", isOpenNow = true, district = "Indore", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nagpur", address = "Sitabuldi, Nagpur", latitude = 21.1458, longitude = 79.0882, phone = "+91 992001002", isOpenNow = false, district = "Nagpur", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Coimbatore", address = "Peelamedu, Coimbatore", latitude = 11.0168, longitude = 76.9558, phone = "+91 992001003", isOpenNow = true, district = "Coimbatore", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Thiruvananthapuram", address = "Pattom, Thiruvananthapuram", latitude = 8.5241, longitude = 76.9366, phone = "+91 992001004", isOpenNow = true, district = "Thiruvananthapuram", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhopal", address = "New Market, Bhopal", latitude = 23.2599, longitude = 77.4126, phone = "+91 992001005", isOpenNow = false, district = "Bhopal", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayawada", address = "Moghalrajapuram, Vijayawada", latitude = 16.5062, longitude = 80.6480, phone = "+91 992001006", isOpenNow = true, district = "Krishna", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Guwahati", address = "Pan Bazaar, Guwahati", latitude = 26.1445, longitude = 91.7362, phone = "+91 992001007", isOpenNow = true, district = "Kamrup Metropolitan", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kochi", address = "Ernakulam, Kochi", latitude = 9.9674, longitude = 76.2454, phone = "+91 992001008", isOpenNow = false, district = "Ernakulam", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jaipur", address = "Bani Park, Jaipur", latitude = 26.9124, longitude = 75.7873, phone = "+91 992001009", isOpenNow = true, district = "Jaipur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Lucknow", address = "Alambagh, Lucknow", latitude = 26.8467, longitude = 80.9462, phone = "+91 992001010", isOpenNow = true, district = "Lucknow", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Visakhapatnam", address = "Seethammadhara, Visakhapatnam", latitude = 17.6868, longitude = 83.2185, phone = "+91 992001011", isOpenNow = false, district = "Visakhapatnam", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nagpur", address = "Dharampeth, Nagpur", latitude = 21.1458, longitude = 79.0882, phone = "+91 992001012", isOpenNow = true, district = "Nagpur", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Indore", address = "Vijay Nagar, Indore", latitude = 22.7196, longitude = 75.8577, phone = "+91 992001013", isOpenNow = true, district = "Indore", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Thane", address = "Naupada, Thane", latitude = 19.1724, longitude = 72.9570, phone = "+91 992001014", isOpenNow = false, district = "Thane", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhopal", address = "MP Nagar, Bhopal", latitude = 23.2599, longitude = 77.4126, phone = "+91 992001015", isOpenNow = true, district = "Bhopal", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jodhpur", address = "Sardarpura, Jodhpur", latitude = 26.2389, longitude = 73.0243, phone = "+91 992001016", isOpenNow = true, district = "Jodhpur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Coimbatore", address = "RS Puram, Coimbatore", latitude = 11.0168, longitude = 76.9558, phone = "+91 992001017", isOpenNow = false, district = "Coimbatore", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Raipur", address = "Pandri, Raipur", latitude = 21.2514, longitude = 81.6296, phone = "+91 992001018", isOpenNow = true, district = "Raipur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ranchi", address = "Hinoo, Ranchi", latitude = 23.3441, longitude = 85.3096, phone = "+91 992001019", isOpenNow = true, district = "Ranchi", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vadodara", address = "Alkapuri, Vadodara", latitude = 22.3072, longitude = 73.1812, phone = "+91 992001020", isOpenNow = false, district = "Vadodara", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nashik", address = "College Road, Nashik", latitude = 19.9975, longitude = 73.7898, phone = "+91 992001021", isOpenNow = true, district = "Nashik", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Faridabad", address = "Sector 15, Faridabad", latitude = 28.4089, longitude = 77.3178, phone = "+91 992001022", isOpenNow = true, district = "Faridabad", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Meerut", address = "Civil Lines, Meerut", latitude = 28.9845, longitude = 77.7064, phone = "+91 992001023", isOpenNow = false, district = "Meerut", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Agra", address = "Sanjay Place, Agra", latitude = 27.1767, longitude = 78.0081, phone = "+91 992001024", isOpenNow = true, district = "Agra", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jabalpur", address = "Cantt, Jabalpur", latitude = 23.1815, longitude = 79.9864, phone = "+91 992001025", isOpenNow = true, district = "Jabalpur", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Asansol", address = "Burnpur Road, Asansol", latitude = 23.6833, longitude = 86.9660, phone = "+91 992001026", isOpenNow = false, district = "Paschim Bardhaman", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dhanbad", address = "Bank More, Dhanbad", latitude = 23.7957, longitude = 86.4304, phone = "+91 992001027", isOpenNow = true, district = "Dhanbad", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Aurangabad", address = "CIDCO, Aurangabad", latitude = 19.8762, longitude = 75.3433, phone = "+91 992001028", isOpenNow = true, district = "Aurangabad", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Gate, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 992001029", isOpenNow = false, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jalandhar", address = "Model Town, Jalandhar", latitude = 31.3260, longitude = 75.5762, phone = "+91 992001030", isOpenNow = true, district = "Jalandhar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gwalior", address = "City Center, Gwalior", latitude = 26.2124, longitude = 78.1772, phone = "+91 992001031", isOpenNow = true, district = "Gwalior", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayawada", address = "Benz Circle, Vijayawada", latitude = 16.5062, longitude = 80.6480, phone = "+91 992001032", isOpenNow = false, district = "Krishna", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Madurai", address = "KK Nagar, Madurai", latitude = 9.9252, longitude = 78.1198, phone = "+91 992001033", isOpenNow = true, district = "Madurai", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Varanasi", address = "Cantt, Varanasi", latitude = 25.3176, longitude = 82.9739, phone = "+91 992001034", isOpenNow = true, district = "Varanasi", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Patna", address = "Boring Road, Patna", latitude = 25.5941, longitude = 85.1376, phone = "+91 992001035", isOpenNow = false, district = "Patna", state = "Bihar"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ludhiana", address = "Civil Lines, Ludhiana", latitude = 30.9010, longitude = 75.8573, phone = "+91 992001036", isOpenNow = true, district = "Ludhiana", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kanpur", address = "Mall Road, Kanpur", latitude = 26.4499, longitude = 80.3319, phone = "+91 992001037", isOpenNow = true, district = "Kanpur Nagar", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nagpur", address = "Medical Square, Nagpur", latitude = 21.1458, longitude = 79.0882, phone = "+91 992001038", isOpenNow = false, district = "Nagpur", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Siliguri", address = "Hill Cart Road, Siliguri", latitude = 26.7271, longitude = 88.3953, phone = "+91 992001039", isOpenNow = true, district = "Darjeeling", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Tiruchirappalli", address = "Srirangam, Tiruchirappalli", latitude = 10.7905, longitude = 78.7047, phone = "+91 992001040", isOpenNow = true, district = "Tiruchirappalli", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kota", address = "Vigyan Nagar, Kota", latitude = 25.2138, longitude = 75.8648, phone = "+91 992001041", isOpenNow = false, district = "Kota", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Thane", address = "Vasant Vihar, Thane", latitude = 19.1724, longitude = 72.9570, phone = "+91 992001042", isOpenNow = true, district = "Thane", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhopal", address = "Habibganj, Bhopal", latitude = 23.2599, longitude = 77.4126, phone = "+91 992001043", isOpenNow = true, district = "Bhopal", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vishakhapatnam", address = "MVP Colony, Vishakhapatnam", latitude = 17.6868, longitude = 83.2185, phone = "+91 992001044", isOpenNow = false, district = "Visakhapatnam", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Indore", address = "Palasia, Indore", latitude = 22.7196, longitude = 75.8577, phone = "+91 992001045", isOpenNow = true, district = "Indore", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Salem", address = "Fairlands, Salem", latitude = 11.6665, longitude = 78.1460, phone = "+91 992001046", isOpenNow = true, district = "Salem", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Aligarh", address = "AMU, Aligarh", latitude = 27.8974, longitude = 78.0884, phone = "+91 992001047", isOpenNow = false, district = "Aligarh", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Warangal", address = "Kazipet, Warangal", latitude = 18.0004, longitude = 79.5876, phone = "+91 992001048", isOpenNow = true, district = "Warangal", state = "Telangana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Moradabad", address = "Civil Lines, Moradabad", latitude = 28.8386, longitude = 78.7733, phone = "+91 992001049", isOpenNow = true, district = "Moradabad", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gurgaon", address = "Sector 14, Gurgaon", latitude = 28.4595, longitude = 77.0266, phone = "+91 992001050", isOpenNow = false, district = "Gurgaon", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Bazar, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 992001051", isOpenNow = true, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ujjain", address = "Freeganj, Ujjain", latitude = 23.1828, longitude = 75.7778, phone = "+91 992001052", isOpenNow = true, district = "Ujjain", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jammu", address = "BC Road, Jammu", latitude = 32.7266, longitude = 74.8570, phone = "+91 992001053", isOpenNow = false, district = "Jammu", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Raipur", address = "Pandri, Raipur", latitude = 21.2514, longitude = 81.6296, phone = "+91 992001054", isOpenNow = true, district = "Raipur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kalyan", address = "Kalyan West, Kalyan", latitude = 19.2403, longitude = 73.1305, phone = "+91 992001055", isOpenNow = true, district = "Thane", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nashik", address = "Gangapur Road, Nashik", latitude = 19.9975, longitude = 73.7898, phone = "+91 992001056", isOpenNow = false, district = "Nashik", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bareilly", address = "Civil Lines, Bareilly", latitude = 28.3670, longitude = 79.4301, phone = "+91 992001057", isOpenNow = true, district = "Bareilly", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Hubballi", address = "Keshwapur, Hubballi", latitude = 15.3647, longitude = 75.1240, phone = "+91 992001058", isOpenNow = true, district = "Dharwad", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Guwahati", address = "Pan Bazaar, Guwahati", latitude = 26.1445, longitude = 91.7362, phone = "+91 992001059", isOpenNow = false, district = "Kamrup Metropolitan", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kochi", address = "MG Road, Kochi", latitude = 9.9674, longitude = 76.2454, phone = "+91 992001060", isOpenNow = true, district = "Ernakulam", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jodhpur", address = "Paota, Jodhpur", latitude = 26.2389, longitude = 73.0243, phone = "+91 992001061", isOpenNow = true, district = "Jodhpur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Tirupur", address = "Palladam, Tirupur", latitude = 11.1085, longitude = 77.3411, phone = "+91 992001062", isOpenNow = false, district = "Tirupur", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gwalior", address = "Lashkar, Gwalior", latitude = 26.2124, longitude = 78.1772, phone = "+91 992001063", isOpenNow = true, district = "Gwalior", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jabalpur", address = "Cantt, Jabalpur", latitude = 23.1815, longitude = 79.9864, phone = "+91 992001064", isOpenNow = true, district = "Jabalpur", state = "Madhya Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Coimbatore", address = "Gandhipuram, Coimbatore", latitude = 11.0168, longitude = 76.9558, phone = "+91 992001065", isOpenNow = false, district = "Coimbatore", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayawada", address = "Moghalrajapuram, Vijayawada", latitude = 16.5062, longitude = 80.6480, phone = "+91 992001066", isOpenNow = true, district = "Krishna", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jalandhar", address = "Civil Lines, Jalandhar", latitude = 31.3260, longitude = 75.5762, phone = "+91 992001067", isOpenNow = true, district = "Jalandhar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kota", address = "Vigyan Nagar, Kota", latitude = 25.2138, longitude = 75.8648, phone = "+91 992001068", isOpenNow = false, district = "Kota", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Madurai", address = "Goripalayam, Madurai", latitude = 9.9252, longitude = 78.1198, phone = "+91 992001069", isOpenNow = true, district = "Madurai", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mysuru", address = "Devaraja Mohalla, Mysuru", latitude = 12.3105, longitude = 76.6571, phone = "+91 992001070", isOpenNow = true, district = "Mysuru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Raipur", address = "Telibandha, Raipur", latitude = 21.2514, longitude = 81.6296, phone = "+91 992001071", isOpenNow = false, district = "Raipur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kozhikode", address = "Medical College, Kozhikode", latitude = 11.2588, longitude = 75.7804, phone = "+91 992001072", isOpenNow = true, district = "Kozhikode", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Thrissur", address = "Sakthan Nagar, Thrissur", latitude = 10.5276, longitude = 76.2144, phone = "+91 992001073", isOpenNow = true, district = "Thrissur", state = "Kerala"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rajkot", address = "Kalawad Road, Rajkot", latitude = 22.3039, longitude = 70.8022, phone = "+91 992001074", isOpenNow = false, district = "Rajkot", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Srinagar", address = "Lal Chowk, Srinagar", latitude = 34.0837, longitude = 74.7973, phone = "+91 992001075", isOpenNow = true, district = "Srinagar", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Gate, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 992001076", isOpenNow = true, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Allahabad", address = "Civil Lines, Allahabad", latitude = 25.4458, longitude = 81.8043, phone = "+91 992001077", isOpenNow = false, district = "Allahabad", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhubaneswar", address = "Unit 1, Bhubaneswar", latitude = 20.2961, longitude = 85.8245, phone = "+91 992001078", isOpenNow = true, district = "Khordha", state = "Odisha"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 17, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 992001079", isOpenNow = true, district = "Chandigarh", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haridwar", address = "Jwalapur, Haridwar", latitude = 29.9456, longitude = 78.1632, phone = "+91 992001080", isOpenNow = false, district = "Haridwar", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gandhinagar", address = "Sector 21, Gandhinagar", latitude = 23.2156, longitude = 72.6369, phone = "+91 992001081", isOpenNow = true, district = "Gandhinagar", state = "Gujarat"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Aurangabad", address = "CIDCO, Aurangabad", latitude = 19.8762, longitude = 75.3433, phone = "+91 992001082", isOpenNow = true, district = "Aurangabad", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dhanbad", address = "Bank More, Dhanbad", latitude = 23.7957, longitude = 86.4304, phone = "+91 992001083", isOpenNow = false, district = "Dhanbad", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Asansol", address = "Burnpur Road, Asansol", latitude = 23.6833, longitude = 86.9660, phone = "+91 992001084", isOpenNow = true, district = "Paschim Bardhaman", state = "West Bengal"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nashik", address = "College Road, Nashik", latitude = 19.9975, longitude = 73.7898, phone = "+91 992001085", isOpenNow = true, district = "Nashik", state = "Maharashtra"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bareilly", address = "Civil Lines, Bareilly", latitude = 28.3670, longitude = 79.4301, phone = "+91 992001086", isOpenNow = false, district = "Bareilly", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vellore", address = "Katpadi, Vellore", latitude = 12.9165, longitude = 79.1325, phone = "+91 992001087", isOpenNow = true, district = "Vellore", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ajmer", address = "Kutchery Road, Ajmer", latitude = 26.4499, longitude = 74.6399, phone = "+91 992001088", isOpenNow = true, district = "Ajmer", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Varanasi", address = "Cantt, Varanasi", latitude = 25.3176, longitude = 82.9739, phone = "+91 992001089", isOpenNow = false, district = "Varanasi", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dhanbad", address = "Hirapur, Dhanbad", latitude = 23.7957, longitude = 86.4304, phone = "+91 992001090", isOpenNow = true, district = "Dhanbad", state = "Jharkhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Bazar, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 992001091", isOpenNow = true, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vijayawada", address = "Benz Circle, Vijayawada", latitude = 16.5062, longitude = 80.6480, phone = "+91 992001092", isOpenNow = false, district = "Krishna", state = "Andhra Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Guwahati", address = "Dispur, Guwahati", latitude = 26.1445, longitude = 91.7362, phone = "+91 992001093", isOpenNow = true, district = "Kamrup Metropolitan", state = "Assam"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 22, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 992001094", isOpenNow = true, district = "Chandigarh", state = "Chandigarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Hubballi", address = "Vidyanagar, Hubballi", latitude = 15.3647, longitude = 75.1240, phone = "+91 992001095", isOpenNow = false, district = "Dharwad", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Raipur", address = "Pandri, Raipur", latitude = 21.2514, longitude = 81.6296, phone = "+91 992001096", isOpenNow = true, district = "Raipur", state = "Chhattisgarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jammu", address = "Gandhi Nagar, Jammu", latitude = 32.7266, longitude = 74.8570, phone = "+91 992001097", isOpenNow = true, district = "Jammu", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mysuru", address = "Saraswathipuram, Mysuru", latitude = 12.3105, longitude = 76.6571, phone = "+91 992001098", isOpenNow = false, district = "Mysuru", state = "Karnataka"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Tiruchirappalli", address = "Srirangam, Tiruchirappalli", latitude = 10.7905, longitude = 78.7047, phone = "+91 992001099", isOpenNow = true, district = "Tiruchirappalli", state = "Tamil Nadu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kota", address = "Talwandi, Kota", latitude = 25.2138, longitude = 75.8648, phone = "+91 992001100", isOpenNow = false, district = "Kota", state = "Rajasthan"),

        // Additional Kendras for better coverage
        Store(name = "PMBJP Jan-Aushadhi Kendra Solan", address = "Barog, Solan", latitude = 30.9074, longitude = 77.1034, phone = "+91 991400002", isOpenNow = true, district = "Solan", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mandi", address = "Sadar Bazar, Mandi", latitude = 31.7202, longitude = 76.9236, phone = "+91 991400003", isOpenNow = false, district = "Mandi", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dharamshala", address = "Kotwali Bazar, Dharamshala", latitude = 32.2190, longitude = 76.3234, phone = "+91 991400004", isOpenNow = true, district = "Kangra", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Una", address = "Una Town, Una", latitude = 31.4709, longitude = 76.2786, phone = "+91 991400005", isOpenNow = true, district = "Una", state = "Himachal Pradesh"),

        // Jammu & Kashmir Additional
        Store(name = "PMBJP Jan-Aushadhi Kendra Jammu", address = "Railway Road, Jammu", latitude = 32.7266, longitude = 74.8570, phone = "+91 991500002", isOpenNow = false, district = "Jammu", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Anantnag", address = "Lal Chowk, Anantnag", latitude = 33.7331, longitude = 75.1419, phone = "+91 991500003", isOpenNow = true, district = "Anantnag", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Baramulla", address = "Main Chowk, Baramulla", latitude = 34.1984, longitude = 74.3517, phone = "+91 991500004", isOpenNow = true, district = "Baramulla", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Sopore", address = "Main Bazar, Sopore", latitude = 34.3016, longitude = 74.4649, phone = "+91 991500005", isOpenNow = false, district = "Baramulla", state = "Jammu & Kashmir"),

        // Goa Additional
        Store(name = "PMBJP Jan-Aushadhi Kendra Margao", address = "Margao Market, Margao", latitude = 15.2993, longitude = 73.9580, phone = "+91 991600002", isOpenNow = true, district = "South Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Vasco", address = "Vasco da Gama, Goa", latitude = 15.3982, longitude = 73.8342, phone = "+91 991600003", isOpenNow = false, district = "South Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mapusa", address = "Mapusa Market, Mapusa", latitude = 15.5226, longitude = 73.8144, phone = "+91 991600004", isOpenNow = true, district = "North Goa", state = "Goa"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ponda", address = "Ponda Market, Ponda", latitude = 15.4031, longitude = 74.0180, phone = "+91 991600005", isOpenNow = true, district = "North Goa", state = "Goa"),

        // North India - Delhi NCR
        Store(name = "PMBJP Jan-Aushadhi Kendra Delhi", address = "Connaught Place, New Delhi", latitude = 28.6304, longitude = 77.2177, phone = "+91 995000001", isOpenNow = true, district = "New Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Nehru Place", address = "Nehru Place, New Delhi", latitude = 28.5485, longitude = 77.2519, phone = "+91 995000002", isOpenNow = false, district = "New Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rohini", address = "Sector 15, Rohini, Delhi", latitude = 28.7326, longitude = 77.1238, phone = "+91 995000003", isOpenNow = true, district = "North West Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dwarka", address = "Sector 12, Dwarka, Delhi", latitude = 28.5817, longitude = 77.0431, phone = "+91 995000004", isOpenNow = false, district = "South West Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Lajpat Nagar", address = "Lajpat Nagar, New Delhi", latitude = 28.5672, longitude = 77.2430, phone = "+91 995000005", isOpenNow = true, district = "South Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Karol Bagh", address = "Karol Bagh, New Delhi", latitude = 28.6479, longitude = 77.1904, phone = "+91 995000006", isOpenNow = false, district = "Central Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Janakpuri", address = "Janakpuri, New Delhi", latitude = 28.6219, longitude = 77.0910, phone = "+91 995000007", isOpenNow = true, district = "West Delhi", state = "Delhi"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Noida", address = "Sector 18, Noida", latitude = 28.5315, longitude = 77.3910, phone = "+91 995000008", isOpenNow = false, district = "Gautam Buddha Nagar", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gurgaon", address = "Sector 14, Gurgaon", latitude = 28.4595, longitude = 77.0266, phone = "+91 995000009", isOpenNow = true, district = "Gurugram", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ghaziabad", address = "Raj Nagar, Ghaziabad", latitude = 28.6692, longitude = 77.4538, phone = "+91 995000010", isOpenNow = false, district = "Ghaziabad", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Faridabad", address = "Sector 15, Faridabad", latitude = 28.4089, longitude = 77.3178, phone = "+91 995000011", isOpenNow = true, district = "Faridabad", state = "Haryana"),

        // North India - Uttar Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Lucknow", address = "Hazratganj, Lucknow", latitude = 26.8467, longitude = 80.9462, phone = "+91 996000001", isOpenNow = true, district = "Lucknow", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kanpur", address = "Mall Road, Kanpur", latitude = 26.4499, longitude = 80.3319, phone = "+91 996000002", isOpenNow = false, district = "Kanpur Nagar", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Agra", address = "Sanjay Place, Agra", latitude = 27.1767, longitude = 78.0081, phone = "+91 996000003", isOpenNow = true, district = "Agra", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Varanasi", address = "Cantt, Varanasi", latitude = 25.3176, longitude = 82.9739, phone = "+91 996000004", isOpenNow = false, district = "Varanasi", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Prayagraj", address = "Civil Lines, Prayagraj", latitude = 25.4358, longitude = 81.8463, phone = "+91 996000005", isOpenNow = true, district = "Prayagraj", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Meerut", address = "Civil Lines, Meerut", latitude = 28.9845, longitude = 77.7064, phone = "+91 996000006", isOpenNow = false, district = "Meerut", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Aligarh", address = "AMU Road, Aligarh", latitude = 27.8974, longitude = 78.0884, phone = "+91 996000007", isOpenNow = true, district = "Aligarh", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bareilly", address = "Civil Lines, Bareilly", latitude = 28.3670, longitude = 79.4301, phone = "+91 996000008", isOpenNow = false, district = "Bareilly", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gorakhpur", address = "Gorakhnath Temple, Gorakhpur", latitude = 26.7606, longitude = 83.3732, phone = "+91 996000009", isOpenNow = true, district = "Gorakhpur", state = "Uttar Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Noida Extension", address = "Greater Noida, UP", latitude = 28.4744, longitude = 77.5040, phone = "+91 996000010", isOpenNow = false, district = "Gautam Buddha Nagar", state = "Uttar Pradesh"),

        // North India - Punjab
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 22, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 997000001", isOpenNow = true, district = "Chandigarh", state = "Chandigarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Amritsar", address = "Hall Bazar, Amritsar", latitude = 31.6340, longitude = 74.8723, phone = "+91 997000002", isOpenNow = false, district = "Amritsar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ludhiana", address = "Civil Lines, Ludhiana", latitude = 30.9010, longitude = 75.8573, phone = "+91 997000003", isOpenNow = true, district = "Ludhiana", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jalandhar", address = "Civil Lines, Jalandhar", latitude = 31.3260, longitude = 75.5762, phone = "+91 997000004", isOpenNow = false, district = "Jalandhar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Patiala", address = "Patiala, Punjab", latitude = 30.3398, longitude = 76.3869, phone = "+91 997000005", isOpenNow = true, district = "Patiala", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bathinda", address = "Bathinda, Punjab", latitude = 30.2094, longitude = 74.9455, phone = "+91 997000006", isOpenNow = false, district = "Bathinda", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mohali", address = "Phase 7, Mohali", latitude = 30.7046, longitude = 76.7179, phone = "+91 997000007", isOpenNow = true, district = "Sahibzada Ajit Singh Nagar", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Pathankot", address = "Pathankot, Punjab", latitude = 32.2644, longitude = 75.6522, phone = "+91 997000008", isOpenNow = false, district = "Pathankot", state = "Punjab"),

        // North India - Haryana
        Store(name = "PMBJP Jan-Aushadhi Kendra Karnal", address = "Karnal, Haryana", latitude = 29.6857, longitude = 76.9905, phone = "+91 998000001", isOpenNow = true, district = "Karnal", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Panipat", address = "Panipat, Haryana", latitude = 29.3909, longitude = 76.9635, phone = "+91 998000002", isOpenNow = false, district = "Panipat", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Sonipat", address = "Sonipat, Haryana", latitude = 28.9907, longitude = 77.0220, phone = "+91 998000003", isOpenNow = true, district = "Sonipat", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ambala", address = "Ambala, Haryana", latitude = 30.3752, longitude = 76.7821, phone = "+91 998000004", isOpenNow = false, district = "Ambala", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rohtak", address = "Rohtak, Haryana", latitude = 28.8909, longitude = 76.5805, phone = "+91 998000005", isOpenNow = true, district = "Rohtak", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Hisar", address = "Hisar, Haryana", latitude = 29.1492, longitude = 75.7210, phone = "+91 998000006", isOpenNow = false, district = "Hisar", state = "Haryana"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kurukshetra", address = "Kurukshetra, Haryana", latitude = 29.9695, longitude = 76.8784, phone = "+91 998000007", isOpenNow = true, district = "Kurukshetra", state = "Haryana"),

        // North India - Rajasthan
        Store(name = "PMBJP Jan-Aushadhi Kendra Jaipur", address = "MI Road, Jaipur", latitude = 26.9124, longitude = 75.7873, phone = "+91 999000001", isOpenNow = false, district = "Jaipur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jodhpur", address = "Sardarpura, Jodhpur", latitude = 26.2389, longitude = 73.0243, phone = "+91 999000002", isOpenNow = true, district = "Jodhpur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Udaipur", address = "City Palace, Udaipur", latitude = 24.5788, longitude = 73.6863, phone = "+91 999000003", isOpenNow = false, district = "Udaipur", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kota", address = "Kota, Rajasthan", latitude = 25.2138, longitude = 75.8648, phone = "+91 999000004", isOpenNow = true, district = "Kota", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ajmer", address = "Ajmer, Rajasthan", latitude = 26.4499, longitude = 74.6399, phone = "+91 999000005", isOpenNow = false, district = "Ajmer", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bikaner", address = "Bikaner, Rajasthan", latitude = 28.0229, longitude = 73.3119, phone = "+91 999000006", isOpenNow = true, district = "Bikaner", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jaisalmer", address = "Jaisalmer, Rajasthan", latitude = 26.9157, longitude = 70.9083, phone = "+91 999000007", isOpenNow = false, district = "Jaisalmer", state = "Rajasthan"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Bharatpur", address = "Bharatpur, Rajasthan", latitude = 27.2155, longitude = 77.4938, phone = "+91 999000008", isOpenNow = true, district = "Bharatpur", state = "Rajasthan"),

        // North India - Uttarakhand
        Store(name = "PMBJP Jan-Aushadhi Kendra Dehradun", address = "Dehradun, Uttarakhand", latitude = 30.3165, longitude = 78.0322, phone = "+91 991000001", isOpenNow = true, district = "Dehradun", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haridwar", address = "Haridwar, Uttarakhand", latitude = 29.9457, longitude = 78.1642, phone = "+91 991000002", isOpenNow = false, district = "Haridwar", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Roorkee", address = "Roorkee, Uttarakhand", latitude = 29.8565, longitude = 77.8868, phone = "+91 991000003", isOpenNow = true, district = "Haridwar", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Haldwani", address = "Haldwani, Uttarakhand", latitude = 29.2185, longitude = 79.5119, phone = "+91 991000004", isOpenNow = false, district = "Nainital", state = "Uttarakhand"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Rishikesh", address = "Rishikesh, Uttarakhand", latitude = 30.0869, longitude = 78.2676, phone = "+91 991000005", isOpenNow = true, district = "Dehradun", state = "Uttarakhand"),

        // North India - Himachal Pradesh
        Store(name = "PMBJP Jan-Aushadhi Kendra Shimla", address = "The Mall, Shimla", latitude = 31.1048, longitude = 77.1734, phone = "+91 992000001", isOpenNow = true, district = "Shimla", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Solan", address = "Mall Road, Solan", latitude = 30.9045, longitude = 77.0990, phone = "+91 992000002", isOpenNow = false, district = "Solan", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Mandi", address = "Sadar Bazar, Mandi", latitude = 31.7202, longitude = 76.9236, phone = "+91 992000003", isOpenNow = true, district = "Mandi", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dharamshala", address = "Kotwali Bazar, Dharamshala", latitude = 32.2190, longitude = 76.3234, phone = "+91 992000004", isOpenNow = false, district = "Kangra", state = "Himachal Pradesh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Una", address = "Una Town, Una", latitude = 31.4709, longitude = 76.2786, phone = "+91 992000005", isOpenNow = true, district = "Una", state = "Himachal Pradesh"),

        // North India - Jammu & Kashmir
        Store(name = "PMBJP Jan-Aushadhi Kendra Srinagar", address = "Lal Chowk, Srinagar", latitude = 34.0837, longitude = 74.7973, phone = "+91 993000001", isOpenNow = true, district = "Srinagar", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Jammu", address = "Railway Road, Jammu", latitude = 32.7266, longitude = 74.8570, phone = "+91 993000002", isOpenNow = false, district = "Jammu", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Anantnag", address = "Lal Chowk, Anantnag", latitude = 33.7331, longitude = 75.1419, phone = "+91 993000003", isOpenNow = true, district = "Anantnag", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Baramulla", address = "Main Chowk, Baramulla", latitude = 34.1984, longitude = 74.3517, phone = "+91 993000004", isOpenNow = false, district = "Baramulla", state = "Jammu & Kashmir"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Sopore", address = "Main Bazar, Sopore", latitude = 34.3016, longitude = 74.4649, phone = "+91 993000005", isOpenNow = true, district = "Baramulla", state = "Jammu & Kashmir"),

        // North India - Punjab (Additional)
        Store(name = "PMBJP Jan-Aushadhi Kendra Bhatinda", address = "Bhatinda, Punjab", latitude = 30.2094, longitude = 74.9455, phone = "+91 997000009", isOpenNow = false, district = "Bathinda", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Firozpur", address = "Firozpur, Punjab", latitude = 30.9163, longitude = 74.6034, phone = "+91 997000010", isOpenNow = true, district = "Firozpur", state = "Punjab"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Moga", address = "Moga, Punjab", latitude = 30.8196, longitude = 75.1657, phone = "+91 997000011", isOpenNow = false, district = "Moga", state = "Punjab"),

        // Northeast States
        Store(name = "PMBJP Jan-Aushadhi Kendra Aizawl", address = "Zarkawt, Aizawl", latitude = 23.7271, longitude = 92.7186, phone = "+91 991700001", isOpenNow = true, district = "Aizawl", state = "Mizoram"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kohima", address = "Kohima Town, Kohima", latitude = 25.6701, longitude = 94.1078, phone = "+91 991700002", isOpenNow = false, district = "Kohima", state = "Nagaland"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Imphal", address = "Imphal East, Imphal", latitude = 24.8170, longitude = 93.9368, phone = "+91 991700003", isOpenNow = true, district = "Imphal East", state = "Manipur"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Agartala", address = "Agartala Town, Agartala", latitude = 23.8315, longitude = 91.2868, phone = "+91 991700004", isOpenNow = true, district = "West Tripura", state = "Tripura"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Gangtok", address = "MG Marg, Gangtok", latitude = 27.3364, longitude = 88.6065, phone = "+91 991700005", isOpenNow = false, district = "East Sikkim", state = "Sikkim"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Itanagar", address = "Naharlagun, Itanagar", latitude = 27.0844, longitude = 93.6053, phone = "+91 991700006", isOpenNow = true, district = "Papum Pare", state = "Arunachal Pradesh"),

        // Union Territories
        Store(name = "PMBJP Jan-Aushadhi Kendra Puducherry", address = "M.G. Road, Puducherry", latitude = 11.9416, longitude = 79.8083, phone = "+91 991800001", isOpenNow = true, district = "Puducherry", state = "Puducherry"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Chandigarh", address = "Sector 22, Chandigarh", latitude = 30.7333, longitude = 76.7794, phone = "+91 991800002", isOpenNow = false, district = "Chandigarh", state = "Chandigarh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Lakshadweep", address = "Kavaratti, Lakshadweep", latitude = 10.5560, longitude = 72.6369, phone = "+91 991800003", isOpenNow = true, district = "Lakshadweep", state = "Lakshadweep"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Andaman", address = "Port Blair, Andaman", latitude = 11.6234, longitude = 92.4622, phone = "+91 991800004", isOpenNow = true, district = "South Andaman", state = "Andaman & Nicobar Islands"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Dadra", address = "Silvassa, Dadra & Nagar Haveli", latitude = 20.2776, longitude = 73.0165, phone = "+91 991800005", isOpenNow = false, district = "Dadra & Nagar Haveli", state = "Dadra & Nagar Haveli and Daman & Diu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Daman", address = "Daman Town, Daman", latitude = 20.4283, longitude = 72.8397, phone = "+91 991800006", isOpenNow = true, district = "Daman", state = "Dadra & Nagar Haveli and Daman & Diu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Diu", address = "Diu Town, Diu", latitude = 20.7096, longitude = 70.9258, phone = "+91 991800007", isOpenNow = true, district = "Diu", state = "Dadra & Nagar Haveli and Daman & Diu"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Ladakh", address = "Leh Main Market, Ladakh", latitude = 34.1526, longitude = 77.5771, phone = "+91 991800008", isOpenNow = false, district = "Leh", state = "Ladakh"),
        Store(name = "PMBJP Jan-Aushadhi Kendra Kargil", address = "Kargil Town, Kargil", latitude = 34.3029, longitude = 76.1296, phone = "+91 991800009", isOpenNow = true, district = "Kargil", state = "Ladakh")
    )
}
