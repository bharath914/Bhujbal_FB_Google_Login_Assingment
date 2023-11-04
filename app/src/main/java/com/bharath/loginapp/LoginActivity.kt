package com.bharath.loginapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bharath.loginapp.signInClient.GoogleAuthClient
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {


    private lateinit var progressBar: ProgressBar
    private lateinit var loginConstraint: ConstraintLayout
    private val email = "email"
    private val publicProfile = "public-profile"
    private val GoogleSignInCode = 9001

    private lateinit var faceBookLoginButton: LoginButton
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var googleAuthClient: GoogleAuthClient
    lateinit var googleSignInButton: MaterialCardView
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        setContentView(R.layout.activity_login)

        callbackManager = CallbackManager.Factory.create()
        googleAuthClient = GoogleAuthClient(this, Identity.getSignInClient(this))
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]






        setLayouts()
        setVisibilityOfProgressBar(GONE)
        facebookSignIn()
        onclickListeners()


    }

    private fun onclickListeners() {


        googleSignInButton.setOnClickListener {
            setVisibilityOfProgressBar(VISIBLE)
            loginViewModel.viewModelScope.launch {
                val intentSender = googleAuthClient.signIn()
                if (intentSender != null) {
                    try {
                        startIntentSenderForResult(
                            intentSender,
                            GoogleSignInCode, null,
                            0, 0, 0
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@LoginActivity,
                            "Failed to start sign-in flow",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    Toast.makeText(
                        this@LoginActivity,
                        "Failed to initialize sign-in flow",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


//
        if (requestCode == GoogleSignInCode) {
            if (data != null) {
                loginViewModel.viewModelScope.launch {

                    val result: GoogleAuthClient.SignInResult =
                        googleAuthClient.signInWithInten(data)

                    if (result.errorMessage == null) {

                        val userData = result.data
                        if (userData != null) {
                            Toast.makeText(
                                this@LoginActivity,
                                "SignInSuccessful : ${userData.userName}",
                                Toast.LENGTH_SHORT
                            ).show()

                            startmain()

                        }
                    }
                    setVisibilityOfProgressBar(GONE)
                }
            }
        } else {


            callbackManager.onActivityResult(requestCode, resultCode, data)

        }
        super.onActivityResult(requestCode, resultCode, data)

//        } else {

//        }
    }

    private fun setLayouts() {
        googleSignInButton = findViewById(R.id.loginBtnGoogle)
        faceBookLoginButton = findViewById(R.id.loginBtnFacebook)
        progressBar = findViewById(R.id.Progress_Indicator)
        loginConstraint = findViewById(R.id.constraint2)
    }

    private fun facebookSignIn() {
        faceBookLoginButton.setOnClickListener {
            setVisibilityOfProgressBar(VISIBLE)
        }
        faceBookLoginButton.setReadPermissions(listOf("email,public_profile"))

        faceBookLoginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d("FaceBook Login", "onCancel: cancelled ")

                }

                override fun onError(error: FacebookException) {
                    Log.d(
                        "FaceBook Login ",
                        "onError: ${error.message}  \n ${error.localizedMessage} "
                    )
                }

                override fun onSuccess(result: LoginResult) {

                    handleFacebookAccessToken(result.accessToken)
                    Log.d(
                        "FaceBook Login ",
                        "Success \n  "
                    )
                }

            }
        )

    }

    private fun startmain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun handleFacebookAccessToken(token: AccessToken) {
        auth = FirebaseAuth.getInstance()
        val cred = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(cred)

            .addOnFailureListener {
                it.printStackTrace()
                Log.d(
                    "failure",
                    "handleFacebookAccessToken: ${it.message} \n ${it.localizedMessage} "
                )
                setVisibilityOfProgressBar(GONE)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    startmain()
                } else {
                    Toast.makeText(this@LoginActivity, "SignIn Failed $}", Toast.LENGTH_SHORT)
                        .show()
                }

            }

    }

    private fun setVisibilityOfProgressBar(visibility: Int) {
        progressBar.visibility = visibility
        if (visibility ==VISIBLE) {
            loginConstraint.alpha = 0.2f
        }else{
            loginConstraint.alpha = 1f
        }
    }
}