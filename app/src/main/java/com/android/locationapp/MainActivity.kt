package com.android.locationapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel : LocationViewModel = viewModel()
            LocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)

    LocationDisplay(locationUtils = locationUtils, viewModel, context = context)
}

@Composable
fun LocationDisplay(locationUtils: LocationUtils, viewModel: LocationViewModel, context: Context) {
    val location = viewModel.location.value

    val address = location?.let {
        locationUtils.reverseGeocodeLocation(location)
    }


    val requestPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
        permission ->
            if(permission[Manifest.permission.ACCESS_COARSE_LOCATION] == true && permission[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // I have access to the location

                locationUtils.requestLocationUpdates(viewModel = viewModel)
            } else {
                // Ask for location permission

                val rationaleRequired =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                if(rationaleRequired) {
                    Toast.makeText(
                        context,
                        "Location Permission is required for this feature to work.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Permission denied.\nPlease enable through Settings...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if(location != null) {
            Text(text = "Address ${location.latitude} ${location.longitude} \n $address", textAlign = TextAlign.Center, fontSize = 10.sp, modifier = Modifier.padding(8.dp))
        } else {
            Text(text = "Location not available", textAlign = TextAlign.Center)
        }

        Button(onClick = {
            if(locationUtils.hasLocationPermission(context)) {
                // Permission already granted
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                // Request location permission
                requestPermissionLauncher
                    .launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
            }
        }) {
            Text(text = "Get Location")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
    LocationAppTheme {
        MyApp(viewModel = LocationViewModel())
    }
}
