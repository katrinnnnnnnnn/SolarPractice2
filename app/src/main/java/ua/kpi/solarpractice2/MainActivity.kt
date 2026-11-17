package ua.kpi.solarpractice2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import ua.kpi.solarpractice2.data.*
import ua.kpi.solarpractice2.viewmodel.*
import ua.kpi.solarpractice2.ui.SolarPractice2App

class MainActivity:ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val db=Room.databaseBuilder(applicationContext,EnergyDatabase::class.java,"solar_practice2.db").build()
  val factory=EnergyViewModelFactory(EnergyRepository(db.energyDao()))
  setContent {
   val vm: EnergyViewModel = viewModel(factory=factory)
   MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF176B59),background=Color(0xFFF3F8F6))) { SolarPractice2App(vm) }
  }
 }
}
