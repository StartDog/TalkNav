package com.example.getsomethingworking

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.example.getsomethingworking.databinding.ActivityMainBinding
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.*
import android.widget.Toast
import android.os.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
//==================================
//import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.collection.emptyLongSet
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.content.SharedPreferences

private var pstate = 3
private var domile = 1
private var verbose = 0
var oispeed: Int = 0
var miles: Int = 0
var kspeed: Float = 0.0F
var seth = 0
var busyc = 0

// Define constants for your keys
const val PREFS_NAME = "MyFloatPrefs"
const val KEY_FLOAT_1 = "first_lat"
const val KEY_FLOAT_2 = "second_lon"
const val KEY_FLOAT_3 = "third_alt"

class MainActivity : AppCompatActivity() {

    private var glat: Double = 0.0
    private var glati: Double = 0.0
    private var glatif: Float = 0f

    private var glon: Double = 0.0
    private var gloni: Double = 0.0
    private var glonif: Float = 0f

    private var galt: Double = 0.0
    private var alti: Double = 0.0
    private var galti: Double = 0.0
    private var galtif: Float = 0f

    private var ldif: Double = 0.0
    private lateinit var tts: TextToSpeech
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var sharedPrefs: SharedPreferences

    private val locationPermissionRequest = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    when {
        permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            // Precise location access granted.
            startLocationUpdates()
        }
        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            // Only approximate location access granted.
            startLocationUpdates()
        }
        else -> {
            // No location access granted. Handle the case where the user denies permission.
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}  // careful do not delete me!!

/*      cannot delete but is not used!!!
    // Create the ActivityResultLauncher for requesting permissions
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if both location permissions are granted
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        }
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    // 1. Set a pure black background
    // Use the ID 'content' to get the root view of the activity window
    findViewById<View>(android.R.id.content).setBackgroundColor(Color.BLACK)

    sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 2. Adjust the window brightness
    val layoutParams: WindowManager.LayoutParams = window.attributes
    layoutParams.screenBrightness = 0.5f // Set brightness to 50%
    window.attributes = layoutParams

    Toast.makeText(this,"init one please wait...", Toast.LENGTH_SHORT).show()

    val defaultFloat = 0.0f
    glati = sharedPrefs.getFloat(KEY_FLOAT_1, defaultFloat).toDouble()
    gloni = sharedPrefs.getFloat(KEY_FLOAT_2, defaultFloat).toDouble()
    galti = sharedPrefs.getFloat(KEY_FLOAT_3, defaultFloat).toDouble()
    if (glati.toInt() != 0) {
        Toast.makeText(this,"Home already set", Toast.LENGTH_SHORT).show()
    }

        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Toast.makeText(this, "TTS INIT one", Toast.LENGTH_SHORT).show()
                val result = tts!!.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ready To Go!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to initialize TTS", Toast.LENGTH_SHORT).show()
            }
        })
        Toast.makeText(this,"init two please wait...", Toast.LENGTH_SHORT).show()
        SystemClock.sleep(5000)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)  // puts TalkNav at the top

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            //Toast.makeText(this, "Mode Change", Toast.LENGTH_SHORT).show()
            //Snackbar.make(view, "Mode Change", Snackbar.LENGTH_LONG)
            //    .setAction("Action", null)
            //    .show()
            onInit(pstate++)
        }

        binding.baf.setOnClickListener { view ->
            Toast.makeText(this, "Save New Home", Toast.LENGTH_SHORT).show()
            //Snackbar.make(view, "Save Home", Snackbar.LENGTH_LONG)
            //    .setAction("Action", null)
            // .show()
            onPress(pstate++)
         }

    binding.btf.setOnClickListener { view ->
     //   Snackbar.make(view, "Visual mode", Snackbar.LENGTH_LONG)
     //       .setAction("Action", null)
     //       .show()
        onVerb()
    }

    binding.btg.setOnClickListener { view ->
        //   Snackbar.make(view, "Visual mode", Snackbar.LENGTH_LONG)
        //       .setAction("Action", null)
        //       .show()
        onDestroy()
    }

        SystemClock.sleep(10000)
        Toast.makeText(this, "Location Services", Toast.LENGTH_SHORT).show()
        //requestLocationUpdates()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkLocationPermissions()

        SystemClock.sleep(5000)
        Toast.makeText(this,"Mode Changed", Toast.LENGTH_SHORT).show()
    }

    fun onInit(mstatus: Int) {
            talkdata()
    }
    fun onPress(mstatus: Int) {
        if (seth == 1) {
            glati = glat
            gloni = glon
            galti = alti
            sharedPrefs.edit().apply {
                putFloat(KEY_FLOAT_1, glati.toFloat())
                putFloat(KEY_FLOAT_2, gloni.toFloat())
                putFloat(KEY_FLOAT_3, galti.toFloat())
            }.apply()

            tts.speak("Setting current location to home", TextToSpeech.QUEUE_FLUSH, null, null)
            seth = 0
        } else {
            tts.speak("Press again to set home", TextToSpeech.QUEUE_FLUSH, null, null)
            seth++
        }
    }

    fun onVerb() {
        seth = 0
        if (verbose == 0) {
            verbose = 1
            Toast.makeText(this, "Visual ON", Toast.LENGTH_SHORT).show()
            tts.speak("Visual mode is on", TextToSpeech.QUEUE_FLUSH, null, null)
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = 1.0f // Set brightness to 0%
            window.attributes = layoutParams

        } else {
            verbose = 0
            Toast.makeText(this, "Visual OFF", Toast.LENGTH_SHORT).show()
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = 0.04f // Set brightness to 0%
            window.attributes = layoutParams
            tts.speak("Visual mode is off", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun talkdata() {
        seth = 0
        if (domile == 1) {
            domile = 5280
            Toast.makeText(this, "Feet", Toast.LENGTH_SHORT).show()
            tts.speak("Setting to feet", TextToSpeech.QUEUE_FLUSH, null, null)
            //SystemClock.sleep(4000)
        } else {
            domile = 1
            Toast.makeText(this, "Miles", Toast.LENGTH_SHORT).show()
            tts.speak("Setting to miles", TextToSpeech.QUEUE_FLUSH, null, null)
            //SystemClock.sleep(4000)
        }
    }

    override fun onDestroy() {
        // Don't forget to shut down the TextToSpeech engine to release resources
        if (::tts.isInitialized && tts.isSpeaking) {
            tts.stop()
        }
// why you need to hit twice - first disables the talk thing maybe
        if (::tts.isInitialized) {
            tts.shutdown()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.menu_main, menu)
        //setup thing apparently
        return true
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this, "Background", Toast.LENGTH_SHORT).show()
    }
    override fun onResume() {
        super.onResume()
        showCustomToast("FG")
        Toast.makeText(this, "Foreground", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return false
        //return when (item.itemId) {
          //  R.id.action_settings -> true
          //  else -> super.onOptionsItemSelected(item)
        //}
    }

// -------------------------------------------
private fun checkLocationPermissions() {
    when {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED -> {
            // Permissions are already granted, start location updates.
            startLocationUpdates()
        }
        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            // Explain to the user why the permission is needed.
            Toast.makeText(this, "We need your location to proceed.", Toast.LENGTH_LONG).show()
            arequestPermissions()
        }
        else -> {
            // Request permissions directly.
            arequestPermissions()
        }
    }
}
    // abcdef
    //@SuppressLint("MissingPermission") // Suppress lint, as permission is checked above
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        // Create a listener to receive location updates.
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                var athing = location.accuracy
                seth = 0

                busyc++
                if (busyc > 30) { // 45 minutes of idle - maybe shutdown?
                    tts.speak("We seem to be idle", TextToSpeech.QUEUE_FLUSH, null, null)
                    val layoutParams: WindowManager.LayoutParams = window.attributes
                    layoutParams.screenBrightness = 0.0f // Set brightness to 0%
                    window.attributes = layoutParams
                    busyc = 20  // every 15 minutes
                }

                Toast.makeText(this@MainActivity,"AC $athing", Toast.LENGTH_LONG).show()
                //showCustomToast("A $athing")
                //if (athing < 5)
                //   Toast.makeText(this@MainActivity,"AC LT 5", Toast.LENGTH_LONG).show()
                //if (athing > 13)
                //    Toast.makeText(this@MainActivity,"AC GT 13", Toast.LENGTH_LONG).show()
                //Toast.makeText(this@MainActivity, "UPDATE", Toast.LENGTH_SHORT).show()
                // value is in km  change to feet

                val latitude = location.latitude
                glat = latitude

                val longitude = location.longitude
                glon = longitude

                alti = location.altitude*3.28084
                var altif = alti.toInt()

                // speed is in km/h change to mi/h
                var speed: Float = location.speed
                kspeed = speed
                var dspeed: Double = speed*2.23694
                var ispeed = dspeed.toInt()
                var bearing = location.bearing

                var mdir = ""
                
                if ((ispeed > 11 && domile == 1) || (ispeed > 3 && domile == 5280)) {
                    busyc = 0
                    if (bearing > 21.0 && bearing < 68.0) {
                        tts.speak("You are heading north east.",TextToSpeech.QUEUE_FLUSH,null,null)
                        SystemClock.sleep(4000)
                        mdir = "N East"
                    }
                    if (bearing > 201.0 && bearing < 247.0) {
                        tts.speak("You are heading south west.",TextToSpeech.QUEUE_FLUSH,null,null)
                        SystemClock.sleep(4000)
                        mdir = "S West"
                    }
                    if (bearing > 291.0 && bearing < 338.0) {
                        tts.speak("You are heading north west.",TextToSpeech.QUEUE_FLUSH,null,null)
                        SystemClock.sleep(4000)
                        mdir = "N West"
                    }
                    if (bearing > 111.0 && bearing < 158.0) {
                        tts.speak("You are heading south east.",TextToSpeech.QUEUE_FLUSH,null,null)
                        SystemClock.sleep(4000)
                        mdir = "S East"
                    }
                //Toast.makeText(this@MainActivity,"Bearing: $bearing< ", Toast.LENGTH_LONG).show()
                if (bearing < 21.0 && bearing > 339.0)  // north
                {
                    tts.speak("You are heading north.",TextToSpeech.QUEUE_FLUSH,null,null)
                    SystemClock.sleep(4000)
                    mdir = " North"
                }
                if (bearing > 69.0 && bearing < 111.0)
                {
                    tts.speak("You are heading east.",TextToSpeech.QUEUE_FLUSH,null,null)
                    SystemClock.sleep(4000)
                    mdir = " East"
                }

                if (bearing < 201.0 && bearing > 159.0)  // done
                {
                    tts.speak("You are heading south.",TextToSpeech.QUEUE_FLUSH,null,null)
                    SystemClock.sleep(4000)
                    mdir = " South"
                }
                if (bearing < 291.0 && bearing > 249.0) // done
                {
                    tts.speak("You are heading west.",TextToSpeech.QUEUE_FLUSH,null,null)
                    SystemClock.sleep(4000)
                    mdir = " West"
                }

                    //if (verbose == 1) showCustomToast("S $ispeed")
                    tts.speak("Your speed is $ispeed miles per hour.",TextToSpeech.QUEUE_FLUSH,null,null)
                    SystemClock.sleep(5400)

                //    if (oispeed == ispeed) {
                //        tts.speak("consistent speed",TextToSpeech.QUEUE_FLUSH,null,null)
                //        SystemClock.sleep(5000)
                //    }
                    oispeed = ispeed
                }     // if (ispeed > 11 && thing)
                if ( glati > 0) {
                    ldif = haversine(glati,gloni,glat,glon,unit = "mi")
                    ldif = ldif*domile
                    miles = ldif.toInt()
                    //if (verbose == 1 && miles > 1) showCustomToast("D $miles")
                    if (miles > 1) {
                        if (domile == 1) {// miles mode
                            //showCustomToast("D $miles")
                            tts.speak("Distance from home is $miles miles.", TextToSpeech.QUEUE_FLUSH,
                                null, null)
                        }
                        else {  // domile != 1
                            if (miles > 700) // feet  mode
                                 tts.speak("Distance from home is $miles feet.", TextToSpeech.QUEUE_FLUSH,null, null
                            )
                        }
                        SystemClock.sleep(5000)
                    }
                    //  tts.speak("your latitude is $glat ", TextToSpeech.QUEUE_FLUSH, null, null)
                } else
                {    // below if never run before - need to really set up
                    if (gloni == 0.0 && glon != 0.0) {
                        //SystemClock.sleep(4000)
                        tts.speak("Starting up.",TextToSpeech.QUEUE_FLUSH, null, null)
                        SystemClock.sleep(5000)
                        Toast.makeText(this@MainActivity,"Setting initial coordinates ", Toast.LENGTH_LONG).show()
                        //Toast.makeText(this@MainActivity,">$glon<>$glat<", Toast.LENGTH_LONG).show()
                        gloni = glon
                        glati = glat
                        galti = altif.toDouble()  // altitude is stupid on cell phones ive seen
                    }
                }
                //tts.speak("You are at Latitude: $latitude, Longitude: $longitude", TextToSpeech.QUEUE_FLUSH, null, null)
                //Toast.makeText(this@MainActivity, "Lat: $latitude, Lng: $longitude", Toast.LENGTH_SHORT).show()
                if (verbose == 1){
                    //if (bearing > 0.0) showCustomToast("B $bearing")
                    if (ispeed > 2) showCustomToast("S $ispeed")
                    //if (abs(altd) > 300) showCustomToast("AC $altd")
                    if (mdir != "") showCustomToast(mdir)
                    if (domile == 1) showCustomToast("D $miles")
                    if (altif > 0) showCustomToast("A $altif")
                }
            }
        }

        // Request updates from the GPS provider.
        // The parameters are: provider, minTime, minDistance, and listener.
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            90000L, // Minimum time in milliseconds between updates
            2f,   // Minimum distance in meters between updates
            locationListener
        )
    }

    // Step 3: Launch the permission request dialog
    private fun arequestPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    //=================================================
    private fun showCustomToast(message: String) {
        val inflater = layoutInflater
        //val layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.custom_toast_container))
        val layout = inflater.inflate(R.layout.custom_toast_layout,null)

        val textView: TextView = layout.findViewById(R.id.custom_toast_text)
        textView.text = message

        with(Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER, 0, 0) // Optional: set gravity
            show()
        }
    }
// -------------------------------------------
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double, unit: String = "km"): Double {
    // Earth's radius in kilometers and miles
    val R_km = 6371.0
    val R_mi = 3956.0
    val R = if (unit == "mi") R_mi else R_km

    // Convert degrees to radians
    val lat1Rad = Math.toRadians(lat1)
    val lon1Rad = Math.toRadians(lon1)
    val lat2Rad = Math.toRadians(lat2)
    val lon2Rad = Math.toRadians(lon2)

    // Calculate the differences in coordinates
    val dlon = lon2Rad - lon1Rad
    val dlat = lat2Rad - lat1Rad

    // Apply the Haversine formula
    val a = sin(dlat / 2.0).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlon / 2.0).pow(2)
    val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

    return R * c
}
// ------------------------------------------
}