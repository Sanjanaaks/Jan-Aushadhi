package com.janaushadhi.finder.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.janaushadhi.finder.databinding.ActivityRegisterBinding
import com.janaushadhi.finder.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.loginText.setOnClickListener { finish() }
        binding.registerButton.setOnClickListener {
            viewModel.register(
                binding.nameInput.text?.toString().orEmpty().trim(),
                binding.emailInput.text?.toString().orEmpty().trim(),
                binding.phoneInput.text?.toString().orEmpty().trim(),
                binding.passwordInput.text?.toString().orEmpty(),
                binding.confirmPasswordInput.text?.toString().orEmpty()
            )
        }

        viewModel.authState.observe(this) { state ->
            binding.progressBar.visibility = if (state is AuthUiState.Loading) View.VISIBLE else View.GONE
            binding.registerButton.isEnabled = state !is AuthUiState.Loading
            when (state) {
                AuthUiState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthUiState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                is AuthUiState.Message -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                AuthUiState.Loading -> Unit
            }
        }
    }
}
