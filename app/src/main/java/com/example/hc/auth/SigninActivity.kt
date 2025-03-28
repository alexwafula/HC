package com.example.hc.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import com.example.hc.models.UserProfile
import com.example.hc.api.ApiService
import com.example.hc.artists.ArtistSelectionActivity
import com.example.hc.network.RetrofitInstance
import java.util.*

class SigninActivity : AppCompatActivity() {

    private lateinit var dobInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var createAccountButton: Button
    private lateinit var progressBar: ProgressBar

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

        dobInput.setOnClickListener {
            showDatePickerDialog()
        }

        createAccountButton.setOnClickListener {
            // Show the progress bar
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
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val maxCalendar = Calendar.getInstance()
                maxCalendar.set(year - 13, month, day)

                if (selectedDate.timeInMillis <= maxCalendar.timeInMillis) {
                    val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                    dobInput.setText(formattedDate)
                } else {
                    Toast.makeText(this, "You must be at least 13 years old to register.", Toast.LENGTH_SHORT).show()
                }
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
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
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userProfile = UserProfile(
                            user_id = userId,
                            email = email,
                            dob = dob,
                            gender = gender
                        )

                        // Save to Firestore
                        db.collection("users").document(userId)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User profile saved to Firestore!", Toast.LENGTH_SHORT).show()

                                // Call API to save the user profile
                                val apiService = RetrofitInstance.getHarmonyCollectiveRetrofit().create(ApiService::class.java)
                                apiService.createUserProfile(userProfile).enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(this@SigninActivity, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                            progressBar.visibility = View.GONE

                                            // Navigate to ArtistSelectionActivity
                                            val intent = Intent(this@SigninActivity, ArtistSelectionActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this@SigninActivity, "API failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                                            progressBar.visibility = View.GONE
                                        }
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                        Toast.makeText(this@SigninActivity, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
                                        progressBar.visibility = View.GONE
                                    }
                                })
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                progressBar.visibility = View.GONE
                            }

                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }
}


