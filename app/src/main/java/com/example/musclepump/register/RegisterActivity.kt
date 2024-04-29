package com.example.musclepump.register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musclepump.databinding.ActivityRegisterBinding
import com.example.musclepump.home.HomeActivity
import com.example.musclepump.model.User
import com.example.musclepump.util.ValidatorUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        databaseReference = Firebase.database.reference
        database = Firebase.database

        setupRegister()
    }

    private fun setupRegister() {

        binding.btnRegister.setOnClickListener {
            binding.cpiProgress.visibility = View.VISIBLE
            it.isEnabled = false

            val nameInput = binding.tilName.editText?.text.toString()
            val emailInput = binding.tilEmail.editText?.text.toString()
            val pwdInput = binding.tilPwd.editText?.text.toString()
            val pwdConfirmedInput = binding.tilConfirmedPwd.editText?.text.toString()
            if (inputsAreValid(nameInput, emailInput, pwdInput, pwdConfirmedInput)) {
                doRegister(nameInput, emailInput, pwdInput)
            } else {
                val toast = Toast(this)
                it.isEnabled = true
                toast.apply {
                    setText("Form is invalid!")
                    show()
                }
                binding.cpiProgress.visibility = View.GONE
            }
        }
    }

    private fun inputsAreValid(
        nameInput: String,
        emailInput: String,
        pwdInput: String,
        pwdConfirmedInput: String
    ): Boolean {


        return ValidatorUtil().nameIsValid(nameInput)
                && ValidatorUtil().emailIsValid(emailInput)
                && ValidatorUtil().pwdIsValidForRegister(pwdInput, pwdConfirmedInput)
    }

    private fun doRegister(nameInput: String, emailInput: String, pwdInput: String) {
        createNewUser(email = emailInput, name = nameInput, password = pwdInput)
    }

    private fun getUser() {
        databaseReference.child("users").child("abc").get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.getValue(User::class.java)?.let { user ->
                Log.i("firebase", "Got value ${user}")
            } ?: run {
                Log.i("firebase", "No user found")
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun writeNewUser(name: String, email: String, password: String) {
        val userReference = databaseReference.child("users").push()
        val userId = userReference.key
        val user = User(name, email, password)
        databaseReference.child("users").child(userId!!).setValue(user)
    }

    private fun createNewUser(email: String, password: String, name: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Handler().postDelayed({
                                    goToHome(name)
                                }, 2000)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error on update user.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.cpiProgress.visibility = View.GONE
                                binding.btnRegister.isEnabled = true
                            }
                        }
                } else {
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        "Error on create new user. Try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.cpiProgress.visibility = View.GONE
                }
            }
    }

    private fun goToHome(name: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("name", name)
        startActivity(intent)
    }
}