package project.ggtime

import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import project.ggtime.ui.theme.GGTimeTheme



class MainActivity : ComponentActivity() {
    private lateinit var credentialManager: CredentialManager
    private val WEB_CLIENT_ID = "1:214908109636:android:6d19421c6e621998498d11"


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        credentialManager = CredentialManager.create(this)

        setContent {
            GGTimeTheme {
                AppNavigation { navController -> signInWithGoogle(navController) }
            }
        }
    }

    private fun signInWithGoogle(navController: NavController) {
        val googleSignInOption = GetSignInWithGoogleOption.Builder(
            serverClientId = "214908109636-fm2nobbrckhdohmk31gdu32rju56e56e.apps.googleusercontent.com"
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleSignInOption)
            .build()

        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@MainActivity
                    )
                    handleSignIn(result, navController) // Pasamos el navController
                } catch (e: GetCredentialException) {
                    Log.e("GoogleSignIn", "Error en login", e)
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse, navController: NavController) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("GoogleSignIn", "ID Token: ${googleIdTokenCredential.idToken}")

                // Si el login es exitoso, navegar a la pantalla de éxito
                navController.navigate("login_success") {
                    popUpTo("home") { inclusive = true } // Esto evita que el usuario vuelva atrás al login
                }
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleSignIn", "Error en el token", e)
            }
        } else {
            Log.e("GoogleSignIn", "Tipo de credencial desconocido")
        }
    }

    @Composable
    fun AppNavigation(onSignInClick: (NavController) -> Unit) {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "home") {
            composable("home") { HomeScreen(navController, onSignInClick) }
            composable("login_success") { LoginSuccessScreen(navController) }
        }
    }



    @Composable
    fun LoginSuccessScreen(navController: NavHostController) {
        val user = FirebaseAuth.getInstance()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login Success!", style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para volver atrás
                Button(
                    onClick = { user.signOut() }, // Volver a la pantalla anterior
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Volver")
                }
            }
        }
    }



    @Composable
    fun HomeScreen(navController: NavController, onSignInClick: (NavController) -> Unit) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bienvenido a GGTime", style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSignInClick(navController) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Iniciar sesión con Google")
                }

            }
        }
    }

}
