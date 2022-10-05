package com.rcappstudio.indoorfarming.views.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.ActivitySignInBinding
import com.rcappstudio.indoorfarming.utils.Constants

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var oneTapClient: SignInClient? = null
    private var signInRequest: BeginSignInRequest? = null
    private lateinit var binding : ActivitySignInBinding

    private val oneTapResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        try {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    val credentials = oneTapClient?.getSignInCredentialFromIntent(it.data)
                    val idToken = credentials?.googleIdToken
                    firebaseAuthWithGoogle(idToken!!)
                } catch (e: ApiException) {
                    Log.w("Sign", "Google sign in failed", e)
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                }
            }else{
                Log.w("Sign", "Google sign in failed with status code: ${it.resultCode}")
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.w("Sign", "Google sign in failed", e)
            Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root )
        auth = FirebaseAuth.getInstance()
        googleSign()

        binding.loginBtn.setOnClickListener {
            displaySignIn()
        }

    }

    private fun googleSign(){

        oneTapClient = Identity.getSignInClient(this)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.your_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(false)
            .build()

    }

    private fun displaySignIn() = oneTapClient?.beginSignIn(signInRequest!!)?.addOnSuccessListener { performAuthentication(it) }?.addOnFailureListener { e -> Log.d("Authentication", "Error: $e") }


    private fun performAuthentication(result: BeginSignInResult) {
        try {
            oneTapResult.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
        } catch (ex: IntentSender.SendIntentException) {

        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Login Success
                    Toast.makeText(this , "Login successful", Toast.LENGTH_LONG).show()
                    checkPlantAvailable()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("CreateProfileActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkPlantAvailable(){
        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")
            .get().addOnSuccessListener {
                if(it.exists()){
                    startActivity(Intent(this , MainActivity::class.java))
                    finish()
                } else {
                    val intent = Intent(this, AddPlantActivity::class.java)
                    intent.putExtra("from",1)
                    startActivity(intent)
                    finish()
                }
            }
    }

    override fun onStart() {
        if(FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        } else{
            super.onStart()
        }
    }
}