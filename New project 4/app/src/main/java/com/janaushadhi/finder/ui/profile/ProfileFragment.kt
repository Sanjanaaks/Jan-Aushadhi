package com.janaushadhi.finder.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.janaushadhi.finder.R
import com.janaushadhi.finder.data.model.User
import com.janaushadhi.finder.databinding.FragmentProfileBinding
import com.janaushadhi.finder.ui.auth.LoginActivity
import com.janaushadhi.finder.utils.SavingsCalculator

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.changePasswordButton.setOnClickListener { showPasswordResetDialog() }
        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            requireActivity().finish()
        }
        viewModel.profile.observe(viewLifecycleOwner) { renderProfile(it) }
        viewModel.message.observe(viewLifecycleOwner) { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        viewModel.loadProfile()
    }

    private fun renderProfile(user: User) = with(binding) {
        initialsText.text = user.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "JA" }
        nameText.text = user.name
        emailText.text = user.email
        phoneText.text = "+91 ${user.phone}"
        totalSavingsText.text = "₹${SavingsCalculator.calculateTotalSavings(user.prescriptions)} saved this month"
        prescriptionStatsText.text = "${user.prescriptions.size} medicines | 3 stores visited"
        prescriptionsText.text = if (user.prescriptions.isEmpty()) {
            "No prescriptions saved yet."
        } else {
            user.prescriptions.joinToString(separator = "\n") { "${it.brandName} → ${it.genericName}" }
        }
    }

    private fun showPasswordResetDialog() {
        val currentUser = viewModel.profile.value ?: return
        val email = currentUser.email
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Password")
            .setMessage("Send password reset link to:\n\n$email")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Send Link") { _, _ ->
                viewModel.sendChangePassword()
            }
            .setNeutralButton("Troubleshoot") { _, _ ->
                showPasswordResetTroubleshoot(email)
            }
            .show()
    }

    private fun showPasswordResetTroubleshoot(email: String) {
        val troubleshootingText = """
            Password Reset Troubleshooting:
            
            1. Check your email address is correct: $email
            2. Look in your Spam/Junk folder
            3. Wait 5-10 minutes for delivery
            4. Check if email provider blocks automated emails
            5. Try with a different email address if needed
            
            Common issues:
            - Gmail: Check Promotions and Spam tabs
            - Yahoo: Check Spam folder
            - Corporate emails: May be blocked by IT policies
            
            If you still don't receive the email, contact support.
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Troubleshooting")
            .setMessage(troubleshootingText)
            .setPositiveButton("Got it", null)
            .setNeutralButton("Try Again") { _, _ ->
                viewModel.sendChangePassword()
            }
            .show()
    }

    private fun showEditProfileDialog() {
        val currentUser = viewModel.profile.value ?: return
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.nameEditText)
        val phoneEditText = dialogView.findViewById<TextInputEditText>(R.id.phoneEditText)
        
        nameEditText.setText(currentUser.name)
        phoneEditText.setText(currentUser.phone)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { dialog, _ ->
                val newName = nameEditText.text.toString().trim()
                val newPhone = phoneEditText.text.toString().trim()
                
                if (newName.isBlank()) {
                    Snackbar.make(binding.root, "Name cannot be empty", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                
                if (newPhone.isBlank() || newPhone.length != 10) {
                    Snackbar.make(binding.root, "Please enter a valid 10-digit phone number", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                
                viewModel.updateProfile(newName, newPhone)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
