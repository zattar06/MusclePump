package com.example.musclepump.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musclepump.databinding.ActivityLoginBinding
import com.example.musclepump.home.HomeActivity
import com.example.musclepump.register.RegisterActivity
import com.example.musclepump.util.ValidatorUtil
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupRegister()
        setupLogin()
    }

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            binding.btnRegister.isEnabled = false
            it.isEnabled = false
            binding.cpiProgress.visibility = View.VISIBLE
            val emailLogin = binding.tilEmail.editText?.text.toString()
            val pwdLogin = binding.tilPwd.editText?.text.toString()
            if (inputsAreValid(emailLogin, pwdLogin)) {
                doLogin(emailLogin, pwdLogin)
            } else {
                showErrorMsg()
                binding.cpiProgress.visibility = View.GONE
                it.isEnabled = true
                binding.btnRegister.isEnabled = true
            }
        }
    }

    private fun inputsAreValid(emailLogin: String, pwdLogin: String): Boolean {
        return ValidatorUtil().emailIsValid(emailLogin) && ValidatorUtil().pwdIsValidForLogin(
            pwdLogin
        )
    }

    private fun doLogin(emailLogin: String, pwdLogin: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailLogin, pwdLogin)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    Handler().postDelayed({
                        goToHome(name = user?.displayName!!)
                    }, 2000)
                } else {
                    showErrorMsg()
                    binding.cpiProgress.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnRegister.isEnabled = true
                }
            }
    }

    private fun showErrorMsg() {
        Toast.makeText(
            this,
            "Login failed. Please check your credentials.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun goToHome(name: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("name", name)
        startActivity(intent)
    }

    private fun setupRegister() {
        binding.btnRegister.setOnClickListener {
            goToRegister()
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}