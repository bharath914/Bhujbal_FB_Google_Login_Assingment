package com.bharath.loginapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bharath.loginapp.signInClient.GoogleAuthClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.AccessToken
import com.facebook.LoginStatusCallback
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {
    private val user = Firebase.auth.currentUser

    private var checkIfFacebook :Boolean = false
    private lateinit var logoutButton: MaterialButton
    private lateinit var displayPicture: CircleImageView
    private lateinit var displayName: TextView
    private lateinit var accessToken: AccessToken

    private lateinit var providerName :TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkIfIsFacebook()
        logoutButton = findViewById(R.id.LogOut)
        displayName = findViewById(R.id.displayname)
        displayPicture = findViewById(R.id.profilePicture)
        providerName = findViewById(R.id.ProviderInfo)
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {




            user.photoUrl?.toString()?.let { setImage(it) }

            displayName.text = getString(R.string.name, user.displayName)
            val  providerData  = user.providerData
            if (providerData.isNotEmpty()){
                val primary = providerData[0].providerId
                if (primary == GoogleAuthProvider.PROVIDER_ID){
                    showNotification(false)
                    providerName.text = getString(R.string.platform_google)
                }else{
                    showNotification(true)
                    providerName.text = getString(R.string.platform_facebook)
                }
            }
              val client = GoogleAuthClient(this, Identity.getSignInClient(this))
            logoutButton.setOnClickListener {

                if (checkIfFacebook){

                signOutOfFacebook()
                }
                client.signOut()
                startActivity(
                    Intent(
                        this, LoginActivity::class.java
                    )
                )
            }

        }



    }

    private fun showNotification(isFacebook:Boolean){

        val notificationBuilder = NotificationCompat.Builder(this, "11111112")
            .setContentText("New User")
            .setContentTitle(if (isFacebook)"Facebook Login " else "Google Sign In")
            .setContentText("Hellow Mr. ${user?.displayName}")
            .setSmallIcon(if (isFacebook) R.drawable.icons8_facebook else R.drawable.icons8_google)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel on Android Oreo and above
            val channel = NotificationChannel(
                "11111112",
                "Login",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1, notificationBuilder.build())

    }

    private fun signOutOfFacebook() {
        try {

            LoginManager.getInstance().logOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setImage(url: String) {

        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.icons8_facebook)
            .into(displayPicture)

    }

    private fun checkIfIsFacebook() {
        LoginManager.getInstance().retrieveLoginStatus(
            this,
            object : LoginStatusCallback {
                override fun onCompleted(accessToken: AccessToken) {
                    checkIfFacebook= true

                }

                override fun onError(exception: Exception) {
                    checkIfFacebook = false
                }

                override fun onFailure() {

                }

            }
        )
    }
}