package tech.kaustubhdeshpande.chachacrypt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("encrypt") }) {
            Text("Encrypt Message")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("decrypt") }) {
            Text("Decrypt Message")
        }
    }
}