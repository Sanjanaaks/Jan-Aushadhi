# 💊 Jan Aushadhi Finder

Jan Aushadhi Finder is an Android application built using Kotlin that helps users locate nearby Jan Aushadhi stores and compare branded medicines with affordable generic alternatives.

The app aims to improve healthcare accessibility by promoting cost-effective medicine choices.

---

## 🚀 Features

### 🔐 User Authentication
- Secure login & registration using Firebase Authentication  
- Email/password authentication with validation  
- Password reset via email  

### 💊 Medicine Search (Fuzzy Matching)
- Intelligent search using FuzzyWuzzy algorithm  
- Supports partial & misspelled inputs  
- Displays:
  - Brand name  
  - Generic equivalent  
  - Price comparison  
  - Savings amount  

### 📍 Nearby Store Finder
- Integrated with Google Maps API  
- Uses Fused Location Provider for real-time location  
- Finds stores within a 50 km radius  
- Filters:
  - Karnataka / South India / All India  
  - Open Now  

### 🧾 Prescription Management
- Add, update, and delete medicines  
- Store refill dates and quantities  
- Enable/disable reminders  
- Data stored in Firebase Firestore  

### 💰 Savings Calculator
- Calculates total savings when switching to generic medicines  

### 🔔 Refill Reminder System
- Implemented using WorkManager  
- Sends notifications for upcoming or overdue refills  

### 👤 User Profile
- Update personal details  
- Reset password  
- Secure logout  

---

## 🏗️ Architecture

- Model → Data classes (Medicine, Store, Prescription, User)  
- View → Activities/Fragments  
- ViewModel → Business logic & UI state  
- Repository → Firebase operations  

---

## 🛠️ Tech Stack

- Kotlin  
- Android Studio  
- Firebase (Authentication, Firestore, Storage)  
- Google Maps API  
- Fused Location Provider  
- MVVM Architecture  
- WorkManager  
- RecyclerView, LiveData, ViewModel  

---

## 📊 How It Works

1. User logs in  
2. Searches medicines  
3. Compares prices  
4. Saves prescriptions  
5. Finds nearby stores  
6. Tracks savings  
7. Gets reminders  

---

## 🌍 Social Impact

- Promotes generic medicines  
- Reduces healthcare costs  
- Improves accessibility  

---

## 📌 Future Enhancements

- AI-based recommendations  
- Online availability tracking  
- Multilingual support  
- Offline mode  

---

## ⚙️ Installation

1. Clone the repo:
  git clone https://github.com/Sanjanaaks/Jan-Aushadhi.git

2. Open in Android Studio  

3. Add:
   - google-services.json  
   - Google Maps API key  

4. Run the app  

---

## 👨‍💻 Developed By

  Sanjana K S
