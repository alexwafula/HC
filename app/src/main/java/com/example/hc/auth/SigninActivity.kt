package com.example.hc.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SigninActivity : AppCompatActivity() {

    private lateinit var dobInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var createAccountButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var dimmingOverlay: View

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sigin)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        dobInput = findViewById(R.id.dobInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        createAccountButton = findViewById(R.id.createAccountButton)
        progressBar = findViewById(R.id.progressBar)
        dimmingOverlay = findViewById(R.id.dimmingOverlay)

        dobInput.setOnClickListener {
            showDatePickerDialog()
        }

        createAccountButton.setOnClickListener {
            // Show the dimming overlay and progress bar
            dimmingOverlay.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE

            registerUser()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                dobInput.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val dob = dobInput.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && dob.isNotEmpty() && gender.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Store user data in Firestore
                        val userId = auth.currentUser?.uid
                        val user = hashMapOf(
                            "email" to email,
                            "date_of_birth" to dob,
                            "gender" to gender
                        )

                        userId?.let {
                            db.collection("users").document(it).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                    // Hide the dimming overlay and progress bar
                                    dimmingOverlay.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                    // Navigate to MainActivity or perform other actions
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    // Hide the dimming overlay and progress bar on failure
                                    dimmingOverlay.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        // Hide the dimming overlay and progress bar on failure
                        dimmingOverlay.visibility = View.GONE
                        progressBar.visibility = View.GONE
                    }
                }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            // Hide the dimming overlay and progress bar if inputs are incomplete
            dimmingOverlay.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }
}

