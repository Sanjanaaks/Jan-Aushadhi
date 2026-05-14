package com.janaushadhi.finder.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.janaushadhi.finder.databinding.ActivityLoginBinding
import com.janaushadhi.finder.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            clearErrors()
            viewModel.login(
                binding.emailInput.text?.toString().orEmpty().trim(),
                binding.passwordInput.text?.toString().orEmpty()
            )
        }

        binding.forgotPasswordText.setOnClickListener {
            viewModel.resetPassword(binding.emailInput.text?.toString().orEmpty().trim())
        }

        binding.createAccountButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.authState.observe(this) { state ->
            binding.progressBar.visibility = if (state is AuthUiState.Loading) View.VISIBLE else View.GONE
            binding.loginButton.isEnabled = state !is AuthUiState.Loading
            when (state) {
                AuthUiState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthUiState.Error -> showError(state.message)
                is AuthUiState.Message -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                AuthUiState.Loading -> Unit
            }
        }
    }

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
    }

    private fun showError(message: String) {
        when (message) {
            "Invalid email", "User not found" -> binding.emailLayout.error = message
            "Wrong password" -> binding.passwordLayout.error = message
            else -> Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
