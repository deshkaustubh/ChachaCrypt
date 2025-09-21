package tech.kaustubhdeshpande.chachacrypt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.kaustubhdeshpande.chachacrypt.ui.DecryptScreen
import tech.kaustubhdeshpande.chachacrypt.ui.EncryptScreen
import tech.kaustubhdeshpande.chachacrypt.viewmodel.DecryptViewModel
import tech.kaustubhdeshpande.chachacrypt.viewmodel.EncryptViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            MaterialTheme(
                colorScheme = if (false) darkColorScheme() else lightColorScheme()
            ) {
                NavHost(navController = nav, startDestination = "encrypt") {
                    composable("encrypt") {
                        val vm: EncryptViewModel = viewModel()
                        EncryptScreen(viewModel = vm) { nav.navigate("decrypt") }
                    }
                    composable("decrypt") {
                        val vm: DecryptViewModel = viewModel()
                        DecryptScreen(viewModel = vm) { nav.navigate("encrypt") }
                    }
                }
            }
        }
    }
}