package com.janaushadhi.finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.janaushadhi.finder.data.model.Medicine
import kotlinx.coroutines.tasks.await

class MedicineRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMedicines(): Result<List<Medicine>> = runCatching {
        seedSampleMedicinesIfNeeded()
        firestore.collection("medicines").get().await().documents.map { doc ->
            doc.toObject(Medicine::class.java)?.copy(id = doc.id) ?: Medicine(id = doc.id)
        }.filter { it.brandName.isNotBlank() }
    }

    suspend fun addPrescription(userId: String, medicine: Medicine): Result<Unit> = runCatching {
        val prescription = mapOf(
            "brandName" to medicine.brandName,
            "genericName" to medicine.genericName,
            "qty" to 1,
            "refillDate" to com.google.firebase.Timestamp.now(),
            "brandPrice" to medicine.brandPrice,
            "genericPrice" to medicine.genericPrice,
            "enabled" to true
        )
        firestore.collection("users").document(userId)
            .update("prescriptions", com.google.firebase.firestore.FieldValue.arrayUnion(prescription))
            .await()
    }

    private suspend fun seedSampleMedicinesIfNeeded() {
        val existing = firestore.collection("medicines").limit(1).get().await()
        if (!existing.isEmpty) return
        sampleMedicines.forEach { medicine ->
            firestore.collection("medicines").add(
                mapOf(
                    "brandName" to medicine.brandName,
                    "genericName" to medicine.genericName,
                    "brandPrice" to medicine.brandPrice,
                    "genericPrice" to medicine.genericPrice,
                    "category" to medicine.category,
                    "manufacturer" to medicine.manufacturer
                )
            ).await()
        }
    }

    private val sampleMedicines = listOf(
        // Painkillers & Anti-inflammatory
        Medicine(brandName = "Crocin", genericName = "Paracetamol", brandPrice = 30.0, genericPrice = 5.0, category = "Painkiller", manufacturer = "GSK"),
        Medicine(brandName = "Dolo 650", genericName = "Paracetamol", brandPrice = 34.0, genericPrice = 5.0, category = "Painkiller", manufacturer = "Micro Labs"),
        Medicine(brandName = "Combiflam", genericName = "Ibuprofen+Paracetamol", brandPrice = 42.0, genericPrice = 9.0, category = "Painkiller", manufacturer = "Sanofi"),
        Medicine(brandName = "Voveran", genericName = "Diclofenac Sodium", brandPrice = 68.0, genericPrice = 12.0, category = "Painkiller", manufacturer = "Novartis"),
        Medicine(brandName = "Brufen", genericName = "Ibuprofen", brandPrice = 45.0, genericPrice = 8.0, category = "Painkiller", manufacturer = "Abbott"),
        Medicine(brandName = "Nimulid", genericName = "Nimesulide", brandPrice = 38.0, genericPrice = 7.0, category = "Painkiller", manufacturer = "Panacea"),
        Medicine(brandName = "Ketorol", genericName = "Ketorolac", brandPrice = 55.0, genericPrice = 10.0, category = "Painkiller", manufacturer = "Dr Reddy's"),
        Medicine(brandName = "Dynapar", genericName = "Diclofenac", brandPrice = 62.0, genericPrice = 11.0, category = "Painkiller", manufacturer = "Troikaa"),
        Medicine(brandName = "Zerodol", genericName = "Aceclofenac", brandPrice = 72.0, genericPrice = 13.0, category = "Painkiller", manufacturer = "Ipca"),
        Medicine(brandName = "Myospaz", genericName = "Chlorzoxazone+Paracetamol", brandPrice = 48.0, genericPrice = 9.0, category = "Painkiller", manufacturer = "Mankind"),
        Medicine(brandName = "Calpol", genericName = "Paracetamol", brandPrice = 31.0, genericPrice = 5.0, category = "Painkiller", manufacturer = "GSK"),
        Medicine(brandName = "Paracip", genericName = "Paracetamol", brandPrice = 28.0, genericPrice = 4.0, category = "Painkiller", manufacturer = "Cipla"),
        Medicine(brandName = "Sumo", genericName = "Nimesulide+Paracetamol", brandPrice = 52.0, genericPrice = 10.0, category = "Painkiller", manufacturer = "Lupin"),
        Medicine(brandName = "Nuromol", genericName = "Ibuprofen+Paracetamol", brandPrice = 46.0, genericPrice = 9.0, category = "Painkiller", manufacturer = "Reckitt"),
        Medicine(brandName = "Saridon", genericName = "Paracetamol+Propyphenazone+Caffeine", brandPrice = 35.0, genericPrice = 7.0, category = "Painkiller", manufacturer = "Searle"),

        // Antibiotics
        Medicine(brandName = "Augmentin", genericName = "Amoxicillin+Clavulanic Acid", brandPrice = 220.0, genericPrice = 45.0, category = "Antibiotic", manufacturer = "GSK"),
        Medicine(brandName = "Azithral", genericName = "Azithromycin", brandPrice = 132.0, genericPrice = 28.0, category = "Antibiotic", manufacturer = "Alembic"),
        Medicine(brandName = "Cifran", genericName = "Ciprofloxacin", brandPrice = 76.0, genericPrice = 16.0, category = "Antibiotic", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Taxim-O", genericName = "Cefixime", brandPrice = 145.0, genericPrice = 31.0, category = "Antibiotic", manufacturer = "Alkem"),
        Medicine(brandName = "Moxikind-CV", genericName = "Amoxicillin+Clavulanic Acid", brandPrice = 198.0, genericPrice = 45.0, category = "Antibiotic", manufacturer = "Mankind"),
        Medicine(brandName = "Zifi", genericName = "Cefixime", brandPrice = 138.0, genericPrice = 30.0, category = "Antibiotic", manufacturer = "FDC"),
        Medicine(brandName = "Levofox", genericName = "Levofloxacin", brandPrice = 156.0, genericPrice = 34.0, category = "Antibiotic", manufacturer = "Lupin"),
        Medicine(brandName = "Gatiflox", genericName = "Gatifloxacin", brandPrice = 142.0, genericPrice = 29.0, category = "Antibiotic", manufacturer = "Cipla"),
        Medicine(brandName = "Oflomac", genericName = "Ofloxacin", brandPrice = 118.0, genericPrice = 22.0, category = "Antibiotic", manufacturer = "Macleods"),
        Medicine(brandName = "Norflox", genericName = "Norfloxacin", brandPrice = 86.0, genericPrice = 15.0, category = "Antibiotic", manufacturer = "Cipla"),
        Medicine(brandName = "Zenflox", genericName = "Ofloxacin", brandPrice = 125.0, genericPrice = 24.0, category = "Antibiotic", manufacturer = "Mankind"),
        Medicine(brandName = "Ceftas", genericName = "Cefuroxime", brandPrice = 168.0, genericPrice = 35.0, category = "Antibiotic", manufacturer = "Intas"),
        Medicine(brandName = "Roxid", genericName = "Roxithromycin", brandPrice = 98.0, genericPrice = 18.0, category = "Antibiotic", manufacturer = "Lupin"),
        Medicine(brandName = "Clarithro", genericName = "Clarithromycin", brandPrice = 186.0, genericPrice = 38.0, category = "Antibiotic", manufacturer = "Abbott"),
        Medicine(brandName = "Doxy", genericName = "Doxycycline", brandPrice = 78.0, genericPrice = 14.0, category = "Antibiotic", manufacturer = "Cipla"),

        // Cardiac Medicines
        Medicine(brandName = "Ecosprin", genericName = "Aspirin", brandPrice = 25.0, genericPrice = 4.0, category = "Cardiac", manufacturer = "USV"),
        Medicine(brandName = "Atorva", genericName = "Atorvastatin", brandPrice = 145.0, genericPrice = 28.0, category = "Cardiac", manufacturer = "Zydus"),
        Medicine(brandName = "Telma", genericName = "Telmisartan", brandPrice = 112.0, genericPrice = 24.0, category = "Cardiac", manufacturer = "Glenmark"),
        Medicine(brandName = "Amlong", genericName = "Amlodipine", brandPrice = 58.0, genericPrice = 10.0, category = "Cardiac", manufacturer = "Micro Labs"),
        Medicine(brandName = "Concor", genericName = "Bisoprolol", brandPrice = 96.0, genericPrice = 18.0, category = "Cardiac", manufacturer = "Merck"),
        Medicine(brandName = "Rosuvas", genericName = "Rosuvastatin", brandPrice = 174.0, genericPrice = 36.0, category = "Cardiac", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Storvas", genericName = "Atorvastatin", brandPrice = 138.0, genericPrice = 27.0, category = "Cardiac", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Lipikind", genericName = "Atorvastatin", brandPrice = 125.0, genericPrice = 25.0, category = "Cardiac", manufacturer = "Mankind"),
        Medicine(brandName = "Clopitab", genericName = "Clopidogrel", brandPrice = 145.0, genericPrice = 28.0, category = "Cardiac", manufacturer = "Lupin"),
        Medicine(brandName = "Deplatt", genericName = "Clopidogrel", brandPrice = 152.0, genericPrice = 30.0, category = "Cardiac", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Cilacar", genericName = "Cilnidipine", brandPrice = 118.0, genericPrice = 23.0, category = "Cardiac", manufacturer = "Intas"),
        Medicine(brandName = "Amlokind", genericName = "Amlodipine", brandPrice = 52.0, genericPrice = 9.0, category = "Cardiac", manufacturer = "Mankind"),
        Medicine(brandName = "Telsartan", genericName = "Telmisartan", brandPrice = 108.0, genericPrice = 22.0, category = "Cardiac", manufacturer = "Lupin"),
        Medicine(brandName = "Olmesar", genericName = "Olmesartan", brandPrice = 125.0, genericPrice = 26.0, category = "Cardiac", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Cardivas", genericName = "Carvedilol", brandPrice = 142.0, genericPrice = 29.0, category = "Cardiac", manufacturer = "Sun Pharma"),

        // Diabetes Medicines
        Medicine(brandName = "Metformin SR", genericName = "Metformin Hydrochloride", brandPrice = 60.0, genericPrice = 12.0, category = "Diabetes", manufacturer = "USV"),
        Medicine(brandName = "Gluconorm", genericName = "Glimepiride+Metformin", brandPrice = 110.0, genericPrice = 22.0, category = "Diabetes", manufacturer = "Lupin"),
        Medicine(brandName = "Januvia", genericName = "Sitagliptin", brandPrice = 420.0, genericPrice = 95.0, category = "Diabetes", manufacturer = "MSD"),
        Medicine(brandName = "Galvus Met", genericName = "Vildagliptin+Metformin", brandPrice = 310.0, genericPrice = 72.0, category = "Diabetes", manufacturer = "Novartis"),
        Medicine(brandName = "Trajenta", genericName = "Linagliptin", brandPrice = 390.0, genericPrice = 88.0, category = "Diabetes", manufacturer = "Boehringer"),
        Medicine(brandName = "Glycomet", genericName = "Metformin", brandPrice = 55.0, genericPrice = 10.0, category = "Diabetes", manufacturer = "USV"),
        Medicine(brandName = "Rybelsus", genericName = "Semaglutide", brandPrice = 2850.0, genericPrice = 680.0, category = "Diabetes", manufacturer = "Novo Nordisk"),
        Medicine(brandName = "Jardiance", genericName = "Empagliflozin", brandPrice = 450.0, genericPrice = 102.0, category = "Diabetes", manufacturer = "Boehringer"),
        Medicine(brandName = "Forxiga", genericName = "Dapagliflozin", brandPrice = 380.0, genericPrice = 85.0, category = "Diabetes", manufacturer = "AstraZeneca"),
        Medicine(brandName = "Voglibose", genericName = "Voglibose", brandPrice = 125.0, genericPrice = 28.0, category = "Diabetes", manufacturer = "Torrent"),
        Medicine(brandName = "Glizid", genericName = "Gliclazide", brandPrice = 85.0, genericPrice = 16.0, category = "Diabetes", manufacturer = "Pfizer"),
        Medicine(brandName = "Diamicron", genericName = "Gliclazide", brandPrice = 92.0, genericPrice = 18.0, category = "Diabetes", manufacturer = "Servier"),
        Medicine(brandName = "Amaryl", genericName = "Glimepiride", brandPrice = 145.0, genericPrice = 32.0, category = "Diabetes", manufacturer = "Sanofi"),
        Medicine(brandName = "Glyciphage", genericName = "Metformin", brandPrice = 58.0, genericPrice = 11.0, category = "Diabetes", manufacturer = "Franco Indian"),
        Medicine(brandName = "Xigduo", genericName = "Dapagliflozin+Metformin", brandPrice = 420.0, genericPrice = 95.0, category = "Diabetes", manufacturer = "AstraZeneca"),

        // Gastrointestinal Medicines
        Medicine(brandName = "Pan-D", genericName = "Pantoprazole+Domperidone", brandPrice = 95.0, genericPrice = 18.0, category = "Gastro", manufacturer = "Alkem"),
        Medicine(brandName = "Omez", genericName = "Omeprazole", brandPrice = 72.0, genericPrice = 12.0, category = "Gastro", manufacturer = "Dr Reddy's"),
        Medicine(brandName = "Rantac", genericName = "Ranitidine", brandPrice = 38.0, genericPrice = 7.0, category = "Gastro", manufacturer = "JB Pharma"),
        Medicine(brandName = "Normaxin", genericName = "Clidinium+Chlordiazepoxide", brandPrice = 128.0, genericPrice = 30.0, category = "Gastro", manufacturer = "Systopic"),
        Medicine(brandName = "Digene", genericName = "Magaldrate+Simethicone", brandPrice = 68.0, genericPrice = 14.0, category = "Gastro", manufacturer = "Abbott"),
        Medicine(brandName = "Gelusil", genericName = "Aluminium Hydroxide+Magnesium Hydroxide", brandPrice = 45.0, genericPrice = 8.0, category = "Gastro", manufacturer = "Pfizer"),
        Medicine(brandName = "Aciloc", genericName = "Ranitidine", brandPrice = 42.0, genericPrice = 8.0, category = "Gastro", manufacturer = "Cipla"),
        Medicine(brandName = "Pantocid", genericName = "Pantoprazole", brandPrice = 88.0, genericPrice = 16.0, category = "Gastro", manufacturer = "Cipla"),
        Medicine(brandName = "Sompraz", genericName = "Esomeprazole", brandPrice = 125.0, genericPrice = 24.0, category = "Gastro", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Nexpro", genericName = "Esomeprazole", brandPrice = 118.0, genericPrice = 22.0, category = "Gastro", manufacturer = "Torrent"),
        Medicine(brandName = "Librax", genericName = "Clidinium+Chlordiazepoxide", brandPrice = 135.0, genericPrice = 31.0, category = "Gastro", manufacturer = "Abbott"),
        Medicine(brandName = "Ganaton", genericName = "Itopride", brandPrice = 95.0, genericPrice = 18.0, category = "Gastro", manufacturer = "Abbott"),
        Medicine(brandName = "Rablet", genericName = "Rabeprazole", brandPrice = 85.0, genericPrice = 15.0, category = "Gastro", manufacturer = "Lupin"),
        Medicine(brandName = "Zintac", genericName = "Ranitidine", brandPrice = 35.0, genericPrice = 6.0, category = "Gastro", manufacturer = "Cipla"),
        Medicine(brandName = "Ursocol", genericName = "Ursodeoxycholic Acid", brandPrice = 185.0, genericPrice = 42.0, category = "Gastro", manufacturer = "Sun Pharma"),

        // Vitamins & Supplements
        Medicine(brandName = "Shelcal", genericName = "Calcium Carbonate+Vitamin D3", brandPrice = 180.0, genericPrice = 35.0, category = "Vitamins", manufacturer = "Torrent"),
        Medicine(brandName = "Becosules", genericName = "Vitamin B Complex", brandPrice = 52.0, genericPrice = 11.0, category = "Vitamins", manufacturer = "Pfizer"),
        Medicine(brandName = "Neurobion Forte", genericName = "Methylcobalamin+Vitamin B Complex", brandPrice = 42.0, genericPrice = 9.0, category = "Vitamins", manufacturer = "P&G"),
        Medicine(brandName = "Zincovit", genericName = "Multivitamin+Zinc", brandPrice = 110.0, genericPrice = 26.0, category = "Vitamins", manufacturer = "Apex"),
        Medicine(brandName = "Supradyn", genericName = "Multivitamin", brandPrice = 85.0, genericPrice = 18.0, category = "Vitamins", manufacturer = "Sanofi"),
        Medicine(brandName = "Revital", genericName = "Multivitamin+Ginseng", brandPrice = 125.0, genericPrice = 28.0, category = "Vitamins", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Evion", genericName = "Vitamin E", brandPrice = 45.0, genericPrice = 8.0, category = "Vitamins", manufacturer = "Merck"),
        Medicine(brandName = "Nurokind", genericName = "Methylcobalamin", brandPrice = 38.0, genericPrice = 7.0, category = "Vitamins", manufacturer = "Mankind"),
        Medicine(brandName = "Celin", genericName = "Vitamin C", brandPrice = 28.0, genericPrice = 5.0, category = "Vitamins", manufacturer = "Cipla"),
        Medicine(brandName = "Calcikind", genericName = "Calcium Carbonate", brandPrice = 65.0, genericPrice = 12.0, category = "Vitamins", manufacturer = "Mankind"),
        Medicine(brandName = "Folvite", genericName = "Folic Acid", brandPrice = 25.0, genericPrice = 4.0, category = "Vitamins", manufacturer = "Abbott"),
        Medicine(brandName = "Bion", genericName = "Vitamin B Complex", brandPrice = 35.0, genericPrice = 7.0, category = "Vitamins", manufacturer = "Cipla"),
        Medicine(brandName = "Vibact", genericName = "Multivitamin+Probiotics", brandPrice = 95.0, genericPrice = 20.0, category = "Vitamins", manufacturer = "Lupin"),
        Medicine(brandName = "A to Z", genericName = "Multivitamin", brandPrice = 75.0, genericPrice = 15.0, category = "Vitamins", manufacturer = "Cipla"),
        Medicine(brandName = "Seven Seas", genericName = "Cod Liver Oil", brandPrice = 145.0, genericPrice = 32.0, category = "Vitamins", manufacturer = "Merck"),

        // Respiratory Medicines
        Medicine(brandName = "Asthalin", genericName = "Salbutamol", brandPrice = 85.0, genericPrice = 15.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Budecort", genericName = "Budesonide", brandPrice = 180.0, genericPrice = 38.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Foracort", genericName = "Formoterol+Budesonide", brandPrice = 285.0, genericPrice = 62.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Seroflo", genericName = "Salmeterol+Fluticasone", brandPrice = 320.0, genericPrice = 68.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Montair", genericName = "Montelukast", brandPrice = 125.0, genericPrice = 26.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Deriphyllin", genericName = "Etofylline+Theophylline", brandPrice = 45.0, genericPrice = 8.0, category = "Respiratory", manufacturer = "USV"),
        Medicine(brandName = "Bambudil", genericName = "Bambuterol", brandPrice = 95.0, genericPrice = 18.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Aerocort", genericName = "Beclomethasone+Levosalbutamol", brandPrice = 165.0, genericPrice = 35.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Levolin", genericName = "Levosalbutamol", brandPrice = 75.0, genericPrice = 14.0, category = "Respiratory", manufacturer = "Cipla"),
        Medicine(brandName = "Tiova", genericName = "Tiotropium", brandPrice = 285.0, genericPrice = 62.0, category = "Respiratory", manufacturer = "Cipla"),

        // Skin Care Medicines
        Medicine(brandName = "Betnovate", genericName = "Betamethasone", brandPrice = 85.0, genericPrice = 16.0, category = "Skin", manufacturer = "GSK"),
        Medicine(brandName = "Candid", genericName = "Clotrimazole", brandPrice = 95.0, genericPrice = 18.0, category = "Skin", manufacturer = "Glenmark"),
        Medicine(brandName = "Luliflam", genericName = "Luliconazole", brandPrice = 125.0, genericPrice = 28.0, category = "Skin", manufacturer = "Lupin"),
        Medicine(brandName = "Fourderm", genericName = "Clobetasol+Miconazole+Neomycin", brandPrice = 145.0, genericPrice = 32.0, category = "Skin", manufacturer = "Mankind"),
        Medicine(brandName = "Ketoconazole", genericName = "Ketoconazole", brandPrice = 75.0, genericPrice = 14.0, category = "Skin", manufacturer = "Cipla"),
        Medicine(brandName = "Soframycin", genericName = "Framycetin", brandPrice = 65.0, genericPrice = 12.0, category = "Skin", manufacturer = "Aventis"),
        Medicine(brandName = "Bactroban", genericName = "Mupirocin", brandPrice = 185.0, genericPrice = 42.0, category = "Skin", manufacturer = "GSK"),
        Medicine(brandName = "Nadifloxacin", genericName = "Nadifloxacin", brandPrice = 115.0, genericPrice = 25.0, category = "Skin", manufacturer = "Almirall"),
        Medicine(brandName = "Mupimet", genericName = "Mupirocin", brandPrice = 125.0, genericPrice = 28.0, category = "Skin", manufacturer = "Mankind"),
        Medicine(brandName = "Terbicip", genericName = "Terbinafine", brandPrice = 135.0, genericPrice = 30.0, category = "Skin", manufacturer = "Cipla"),

        // Eye Care Medicines
        Medicine(brandName = "Tobramycin", genericName = "Tobramycin", brandPrice = 85.0, genericPrice = 16.0, category = "Eye", manufacturer = "Alcon"),
        Medicine(brandName = "Gatifloxacin", genericName = "Gatifloxacin", brandPrice = 75.0, genericPrice = 14.0, category = "Eye", manufacturer = "Allergan"),
        Medicine(brandName = "Moxifloxacin", genericName = "Moxifloxacin", brandPrice = 95.0, genericPrice = 18.0, category = "Eye", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Lumigan", genericName = "Bimatoprost", brandPrice = 1250.0, genericPrice = 285.0, category = "Eye", manufacturer = "Allergan"),
        Medicine(brandName = "Tobrasist", genericName = "Tobramycin", brandPrice = 78.0, genericPrice = 15.0, category = "Eye", manufacturer = "Intas"),
        Medicine(brandName = "Zoxan", genericName = "Ciprofloxacin", brandPrice = 65.0, genericPrice = 12.0, category = "Eye", manufacturer = "FDC"),
        Medicine(brandName = "Gentamicin", genericName = "Gentamicin", brandPrice = 45.0, genericPrice = 8.0, category = "Eye", manufacturer = "Cipla"),
        Medicine(brandName = "Fluorometholone", genericName = "Fluorometholone", brandPrice = 85.0, genericPrice = 16.0, category = "Eye", manufacturer = "Allergan"),
        Medicine(brandName = "Predmet", genericName = "Prednisolone", brandPrice = 95.0, genericPrice = 18.0, category = "Eye", manufacturer = "Mankind"),
        Medicine(brandName = "Ketorol", genericName = "Ketorolac", brandPrice = 55.0, genericPrice = 10.0, category = "Eye", manufacturer = "Sun Pharma"),

        // Allergy Medicines
        Medicine(brandName = "Cetrizine", genericName = "Cetirizine", brandPrice = 35.0, genericPrice = 6.0, category = "Allergy", manufacturer = "Cipla"),
        Medicine(brandName = "Allegra", genericName = "Fexofenadine", brandPrice = 85.0, genericPrice = 18.0, category = "Allergy", manufacturer = "Sanofi"),
        Medicine(brandName = "Levocet", genericName = "Levocetirizine", brandPrice = 45.0, genericPrice = 8.0, category = "Allergy", manufacturer = "Cipla"),
        Medicine(brandName = "Avil", genericName = "Pheniramine", brandPrice = 25.0, genericPrice = 4.0, category = "Allergy", manufacturer = "Aventis"),
        Medicine(brandName = "Zyrtec", genericName = "Cetirizine", brandPrice = 38.0, genericPrice = 7.0, category = "Allergy", manufacturer = "UCB"),
        Medicine(brandName = "Alerid", genericName = "Cetirizine", brandPrice = 32.0, genericPrice = 6.0, category = "Allergy", manufacturer = "Cipla"),
        Medicine(brandName = "Okacet", genericName = "Cetirizine", brandPrice = 30.0, genericPrice = 5.0, category = "Allergy", manufacturer = "Mankind"),
        Medicine(brandName = "Histakind", genericName = "Fexofenadine", brandPrice = 78.0, genericPrice = 16.0, category = "Allergy", manufacturer = "Mankind"),
        Medicine(brandName = "Lupizyr", genericName = "Levocetirizine", brandPrice = 42.0, genericPrice = 8.0, category = "Allergy", manufacturer = "Lupin"),
        Medicine(brandName = "Rozat", genericName = "Fexofenadine", brandPrice = 82.0, genericPrice = 17.0, category = "Allergy", manufacturer = "Intas"),

        // Neurological Medicines
        Medicine(brandName = "Rivotril", genericName = "Clonazepam", brandPrice = 85.0, genericPrice = 16.0, category = "Neurology", manufacturer = "Roche"),
        Medicine(brandName = "Gabapin", genericName = "Gabapentin", brandPrice = 125.0, genericPrice = 28.0, category = "Neurology", manufacturer = "Intas"),
        Medicine(brandName = "Pregabid", genericName = "Pregabalin", brandPrice = 185.0, genericPrice = 42.0, category = "Neurology", manufacturer = "Lupin"),
        Medicine(brandName = "Stugeron", genericName = "Cinnarizine", brandPrice = 65.0, genericPrice = 12.0, category = "Neurology", manufacturer = "Janssen"),
        Medicine(brandName = "Vertin", genericName = "Betahistine", brandPrice = 75.0, genericPrice = 14.0, category = "Neurology", manufacturer = "Abbott"),
        Medicine(brandName = "Topamac", genericName = "Topiramate", brandPrice = 145.0, genericPrice = 32.0, category = "Neurology", manufacturer = "Janssen"),
        Medicine(brandName = "Encorate", genericName = "Sodium Valproate", brandPrice = 165.0, genericPrice = 38.0, category = "Neurology", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Eptoin", genericName = "Phenytoin", brandPrice = 45.0, genericPrice = 8.0, category = "Neurology", manufacturer = "Cipla"),
        Medicine(brandName = "Frisium", genericName = "Clobazam", brandPrice = 95.0, genericPrice = 18.0, category = "Neurology", manufacturer = "Sanofi"),
        Medicine(brandName = "Lametec", genericName = "Lamotrigine", brandPrice = 125.0, genericPrice = 28.0, category = "Neurology", manufacturer = "Intas"),

        // Hormonal Medicines
        Medicine(brandName = "Thyronorm", genericName = "Thyroxine", brandPrice = 85.0, genericPrice = 16.0, category = "Hormonal", manufacturer = "Abbott"),
        Medicine(brandName = "Eltroxin", genericName = "Thyroxine", brandPrice = 92.0, genericPrice = 18.0, category = "Hormonal", manufacturer = "GSK"),
        Medicine(brandName = "Thyrox", genericName = "Thyroxine", brandPrice = 78.0, genericPrice = 15.0, category = "Hormonal", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Wysolone", genericName = "Prednisolone", brandPrice = 65.0, genericPrice = 12.0, category = "Hormonal", manufacturer = "Wyeth"),
        Medicine(brandName = "Dexamethasone", genericName = "Dexamethasone", brandPrice = 45.0, genericPrice = 8.0, category = "Hormonal", manufacturer = "Cipla"),
        Medicine(brandName = "Hydrocort", genericName = "Hydrocortisone", brandPrice = 55.0, genericPrice = 10.0, category = "Hormonal", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Cortel", genericName = "Betamethasone", brandPrice = 75.0, genericPrice = 14.0, category = "Hormonal", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Deflazacort", genericName = "Deflazacort", brandPrice = 125.0, genericPrice = 28.0, category = "Hormonal", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Medrol", genericName = "Methylprednisolone", brandPrice = 145.0, genericPrice = 32.0, category = "Hormonal", manufacturer = "Pfizer"),
        Medicine(brandName = "Betnesol", genericName = "Betamethasone", brandPrice = 85.0, genericPrice = 16.0, category = "Hormonal", manufacturer = "GSK"),

        // Anti-hypertensive
        Medicine(brandName = "Losar", genericName = "Losartan", brandPrice = 95.0, genericPrice = 18.0, category = "Cardiac", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Cosart", genericName = "Losartan", brandPrice = 88.0, genericPrice = 16.0, category = "Cardiac", manufacturer = "Cipla"),
        Medicine(brandName = "Zaroxolyn", genericName = "Metolazone", brandPrice = 125.0, genericPrice = 28.0, category = "Cardiac", manufacturer = "Umedica"),
        Medicine(brandName = "Frusenex", genericName = "Furosemide", brandPrice = 45.0, genericPrice = 8.0, category = "Cardiac", manufacturer = "FDC"),
        Medicine(brandName = "Lasix", genericName = "Furosemide", brandPrice = 52.0, genericPrice = 9.0, category = "Cardiac", manufacturer = "Sanofi"),
        Medicine(brandName = "Dytor", genericName = "Torsemide", brandPrice = 85.0, genericPrice = 16.0, category = "Cardiac", manufacturer = "Cipla"),
        Medicine(brandName = "Minipress", genericName = "Prazosin", brandPrice = 65.0, genericPrice = 12.0, category = "Cardiac", manufacturer = "Pfizer"),
        Medicine(brandName = "Cardura", genericName = "Doxazosin", brandPrice = 95.0, genericPrice = 18.0, category = "Cardiac", manufacturer = "Pfizer"),
        Medicine(brandName = "Alphadopa", genericName = "Methyldopa", brandPrice = 75.0, genericPrice = 14.0, category = "Cardiac", manufacturer = "Wyeth"),
        Medicine(brandName = "Aldactone", genericName = "Spironolactone", brandPrice = 85.0, genericPrice = 16.0, category = "Cardiac", manufacturer = "GSK"),

        // Antidepressants
        Medicine(brandName = "Serenace", genericName = "Haloperidol", brandPrice = 65.0, genericPrice = 12.0, category = "Psychiatry", manufacturer = "RPG"),
        Medicine(brandName = "Sizodon", genericName = "Risperidone", brandPrice = 125.0, genericPrice = 28.0, category = "Psychiatry", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Oleanz", genericName = "Olanzapine", brandPrice = 185.0, genericPrice = 42.0, category = "Psychiatry", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Quetiapine", genericName = "Quetiapine", brandPrice = 145.0, genericPrice = 32.0, category = "Psychiatry", manufacturer = "Intas"),
        Medicine(brandName = "Arpizol", genericName = "Aripiprazole", brandPrice = 285.0, genericPrice = 65.0, category = "Psychiatry", manufacturer = "Intas"),
        Medicine(brandName = "Prodep", genericName = "Fluoxetine", brandPrice = 95.0, genericPrice = 18.0, category = "Psychiatry", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Serenlift", genericName = "Sertraline", brandPrice = 125.0, genericPrice = 28.0, category = "Psychiatry", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Nexito", genericName = "Escitalopram", brandPrice = 145.0, genericPrice = 32.0, category = "Psychiatry", manufacturer = "Sun Pharma"),
        Medicine(brandName = "Stalopam", genericName = "Escitalopram", brandPrice = 138.0, genericPrice = 30.0, category = "Psychiatry", manufacturer = "Intas"),
        Medicine(brandName = "Venlor", genericName = "Venlafaxine", brandPrice = 165.0, genericPrice = 38.0, category = "Psychiatry", manufacturer = "Sun Pharma"),

        // Anti-emetics
        Medicine(brandName = "Vomikind", genericName = "Ondansetron", brandPrice = 85.0, genericPrice = 16.0, category = "Gastro", manufacturer = "Mankind"),
        Medicine(brandName = "Emeset", genericName = "Ondansetron", brandPrice = 92.0, genericPrice = 18.0, category = "Gastro", manufacturer = "Cipla"),
        Medicine(brandName = "Zofran", genericName = "Ondansetron", brandPrice = 125.0, genericPrice = 28.0, category = "Gastro", manufacturer = "GSK"),
        Medicine(brandName = "Perinorm", genericName = "Metoclopramide", brandPrice = 35.0, genericPrice = 6.0, category = "Gastro", manufacturer = "Ipca"),
        Medicine(brandName = "Reglan", genericName = "Metoclopramide", brandPrice = 38.0, genericPrice = 7.0, category = "Gastro", manufacturer = "Wyeth"),
        Medicine(brandName = "Domstal", genericName = "Domperidone", brandPrice = 45.0, genericPrice = 8.0, category = "Gastro", manufacturer = "Torrent"),
        Medicine(brandName = "Domperidone", genericName = "Domperidone", brandPrice = 42.0, genericPrice = 8.0, category = "Gastro", manufacturer = "Cipla"),
        Medicine(brandName = "Gravol", genericName = "Dimenhydrinate", brandPrice = 55.0, genericPrice = 10.0, category = "Gastro", manufacturer = "Webber"),
        Medicine(brandName = "Avomine", genericName = "Promethazine", brandPrice = 65.0, genericPrice = 12.0, category = "Gastro", manufacturer = "Aventis"),
        Medicine(brandName = "Phenergan", genericName = "Promethazine", brandPrice = 58.0, genericPrice = 11.0, category = "Gastro", manufacturer = "Wyeth"),

        // Anti-fungal
        Medicine(brandName = "Fluconazole", genericName = "Fluconazole", brandPrice = 125.0, genericPrice = 28.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Forcan", genericName = "Fluconazole", brandPrice = 135.0, genericPrice = 30.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Fungisome", genericName = "Amphotericin B", brandPrice = 850.0, genericPrice = 185.0, category = "Anti-fungal", manufacturer = "Lifecare"),
        Medicine(brandName = "Nystatin", genericName = "Nystatin", brandPrice = 95.0, genericPrice = 18.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Itraconazole", genericName = "Itraconazole", brandPrice = 285.0, genericPrice = 62.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Canditral", genericName = "Itraconazole", brandPrice = 295.0, genericPrice = 65.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Voriconazole", genericName = "Voriconazole", brandPrice = 1250.0, genericPrice = 285.0, category = "Anti-fungal", manufacturer = "Pfizer"),
        Medicine(brandName = "Amphotericin", genericName = "Amphotericin B", brandPrice = 750.0, genericPrice = 165.0, category = "Anti-fungal", manufacturer = "Cipla"),
        Medicine(brandName = "Terbinaforce", genericName = "Terbinafine", brandPrice = 145.0, genericPrice = 32.0, category = "Anti-fungal", manufacturer = "Lupin"),
        Medicine(brandName = "Onabet", genericName = "Sertaconazole", brandPrice = 185.0, genericPrice = 42.0, category = "Anti-fungal", manufacturer = "Lupin"),

        // Anti-viral
        Medicine(brandName = "Acivir", genericName = "Acyclovir", brandPrice = 125.0, genericPrice = 28.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Zovirax", genericName = "Acyclovir", brandPrice = 145.0, genericPrice = 32.0, category = "Anti-viral", manufacturer = "GSK"),
        Medicine(brandName = "Valcivir", genericName = "Valacyclovir", brandPrice = 185.0, genericPrice = 42.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Famtrex", genericName = "Famciclovir", brandPrice = 285.0, genericPrice = 62.0, category = "Anti-viral", manufacturer = "FDC"),
        Medicine(brandName = "Oseltamivir", genericName = "Oseltamivir", brandPrice = 450.0, genericPrice = 102.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Ribavirin", genericName = "Ribavirin", brandPrice = 385.0, genericPrice = 85.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Interferon", genericName = "Interferon", brandPrice = 1250.0, genericPrice = 285.0, category = "Anti-viral", manufacturer = "Roche"),
        Medicine(brandName = "Lamivudine", genericName = "Lamivudine", brandPrice = 165.0, genericPrice = 38.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Tenofovir", genericName = "Tenofovir", brandPrice = 285.0, genericPrice = 65.0, category = "Anti-viral", manufacturer = "Cipla"),
        Medicine(brandName = "Efavirenz", genericName = "Efavirenz", brandPrice = 225.0, genericPrice = 52.0, category = "Anti-viral", manufacturer = "Cipla"),

        // Anti-parasitic
        Medicine(brandName = "Albendazole", genericName = "Albendazole", brandPrice = 45.0, genericPrice = 8.0, category = "Anti-parasitic", manufacturer = "Cipla"),
        Medicine(brandName = "Bandy", genericName = "Albendazole", brandPrice = 52.0, genericPrice = 9.0, category = "Anti-parasitic", manufacturer = "Mankind"),
        Medicine(brandName = "Zentel", genericName = "Albendazole", brandPrice = 58.0, genericPrice = 11.0, category = "Anti-parasitic", manufacturer = "GSK"),
        Medicine(brandName = "Ivermectin", genericName = "Ivermectin", brandPrice = 85.0, genericPrice = 16.0, category = "Anti-parasitic", manufacturer = "Cipla"),
        Medicine(brandName = "Mectizan", genericName = "Ivermectin", brandPrice = 95.0, genericPrice = 18.0, category = "Anti-parasitic", manufacturer = "Merck"),
        Medicine(brandName = "Pyrantel", genericName = "Pyrantel", brandPrice = 35.0, genericPrice = 6.0, category = "Anti-parasitic", manufacturer = "Cipla"),
        Medicine(brandName = "Mebendazole", genericName = "Mebendazole", brandPrice = 38.0, genericPrice = 7.0, category = "Anti-parasitic", manufacturer = "Cipla"),
        Medicine(brandName = "Vermox", genericName = "Mebendazole", brandPrice = 45.0, genericPrice = 8.0, category = "Anti-parasitic", manufacturer = "Janssen"),
        Medicine(brandName = "Niclosamide", genericName = "Niclosamide", brandPrice = 65.0, genericPrice = 12.0, category = "Anti-parasitic", manufacturer = "Cipla"),
        Medicine(brandName = "Praziquantel", genericName = "Praziquantel", brandPrice = 125.0, genericPrice = 28.0, category = "Anti-parasitic", manufacturer = "Cipla")
    )
}
