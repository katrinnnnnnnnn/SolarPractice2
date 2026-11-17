package ua.kpi.solarpractice2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.kpi.solarpractice2.data.*

/** MVVM: модель і БД поза Compose; View читає StateFlow. */
class EnergyViewModel(private val repo: EnergyRepository): ViewModel() {
 val stations = repo.stations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 val measurements = repo.measurements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 private val _message = MutableStateFlow("")
 val message = _message.asStateFlow()
 private val _loading = MutableStateFlow(false)
 val loading = _loading.asStateFlow()
 private val _preview = MutableStateFlow(0.0)
 val preview = _preview.asStateFlow()
 private val _filter = MutableStateFlow("")
 val filter = _filter.asStateFlow()
 private val _sortDescending = MutableStateFlow(true)
 val sortDescending = _sortDescending.asStateFlow()
 val visibleMeasurements: StateFlow<List<MeasurementEntity>> = combine(measurements, filter, sortDescending) { rows, search, descending ->
  val selected = rows.filter { search.isBlank() || it.power.toInt().toString().contains(search.trim()) || it.stationId.toString().contains(search.trim()) }
  if (descending) selected.sortedByDescending { it.timestamp } else selected.sortedBy { it.timestamp }
 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 fun search(value:String) { _filter.value=value }
 fun toggleSort() { _sortDescending.value=!_sortDescending.value }
 fun clearMessage() { _message.value="" }
 fun calculate(g:String,eta:String,area:String) { runTask { val (a,b,c)=numbers(g,eta,area); _preview.value=repo.preview(a,b,c); _message.value="Розраховано: %.1f Вт".format(_preview.value) } }
 fun saveStation(name:String,location:String) { runTask { require(name.isNotBlank()) { "Введіть назву станції" }; repo.addStation(name, location); _message.value="Станцію збережено" } }
 fun saveReading(stationId:Long?,g:String,eta:String,area:String) { runTask { require(stationId!=null) { "Спочатку додайте станцію" }; val (a,b,c)=numbers(g,eta,area); repo.addReading(stationId,a,b,c); _message.value="Показник додано до Room" } }
 fun deleteReading(item:MeasurementEntity) { runTask { repo.delete(item); _message.value="Запис видалено" } }
 fun updateReading(item:MeasurementEntity) { runTask { repo.update(item.copy(irradiance=(item.irradiance+25.0).coerceAtMost(1500.0))); _message.value="Запис оновлено" } }
 private fun numbers(g:String,eta:String,area:String):Triple<Double,Double,Double> {
  val a=g.replace(',','.').toDoubleOrNull() ?: error("Некоректна радіація")
  val b=eta.replace(',','.').toDoubleOrNull() ?: error("Некоректний ККД")
  val c=area.replace(',','.').toDoubleOrNull() ?: error("Некоректна площа")
  require(a in 0.0..1500.0 && b in 0.0..100.0 && c>0.0) { "Перевірте діапазони: 0–1500, 0–100, площа > 0" }
  return Triple(a,b,c)
 }
 private fun runTask(action:suspend ()->Unit) { viewModelScope.launch { _loading.value=true; _message.value=""; try { delay(250); action() } catch(e:Exception) { _message.value=e.message ?: "Помилка операції" } finally { _loading.value=false } } }
}
class EnergyViewModelFactory(private val repo:EnergyRepository):ViewModelProvider.Factory {
 @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T = EnergyViewModel(repo) as T
}
