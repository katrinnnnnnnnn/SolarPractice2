package ua.kpi.solarpractice2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.kpi.solarpractice2.data.MeasurementEntity
import ua.kpi.solarpractice2.viewmodel.EnergyViewModel

private object Routes { const val HOME="home"; const val NAV="navigation"; const val FORM="form"; const val MVVM="mvvm"; const val ROOM="room"; const val INFO="info/{station}" }

/** ПЗ 5: єдиний NavHost; передача station аргументом та підтримка Back. */
@Composable fun SolarPractice2App(vm:EnergyViewModel) {
 val nav=rememberNavController()
 val message by vm.message.collectAsStateWithLifecycle()
 val loading by vm.loading.collectAsStateWithLifecycle()
 Scaffold(topBar={ Surface(tonalElevation=2.dp) { Text("SolarPractice • ПР №2", Modifier.fillMaxWidth().padding(18.dp),style=MaterialTheme.typography.titleLarge) } }, bottomBar={ if (loading) LinearProgressIndicator(Modifier.fillMaxWidth()) }) { pads ->
  Column(Modifier.fillMaxSize().padding(pads).safeDrawingPadding()) {
   if (message.isNotBlank()) { Text(message, Modifier.padding(horizontal=18.dp,vertical=6.dp),color=MaterialTheme.colorScheme.primary); TextButton(onClick=vm::clearMessage) { Text("Закрити повідомлення") } }
   NavHost(navController=nav,startDestination=Routes.HOME,modifier=Modifier.weight(1f)) {
    composable(Routes.HOME) { HomeScreen(onOpen={nav.navigate(it)}) }
    composable(Routes.NAV) { Page(heading="ПЗ 5 · Навігація",onBack={nav.popBackStack()}) {
     Text("Головне меню, NavHost, NavController та аргумент маршруту.")
     Button(onClick={nav.navigate("info/SolarStation")}) { Text("Деталі станції (аргумент)") }
     Button(onClick={nav.navigate(Routes.FORM)}) { Text("Перейти до форми") }
    } }
    composable(Routes.INFO,arguments=listOf(navArgument("station") {type=NavType.StringType})) { entry -> Page("Деталі станції",onBack={nav.popBackStack()}) { Text("Назва з маршруту: ${entry.arguments?.getString("station") ?: "—"}") } }
    composable(Routes.FORM) { FormScreen(vm,onBack={nav.popBackStack()}) }
    composable(Routes.MVVM) { MvvmScreen(vm,onBack={nav.popBackStack()}) }
    composable(Routes.ROOM) { RoomScreen(vm,onBack={nav.popBackStack()}) }
   }
  }
 }
}

/** Повторно використовуваний адаптивний контейнер для всіх екранів. */
@Composable private fun Page(heading:String,onBack:()->Unit,content:@Composable ColumnScope.()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
  Column(Modifier.widthIn(max=650.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically) { TextButton(onClick=onBack) { Text("← Меню") }; Text(heading, style=MaterialTheme.typography.titleLarge) }
   content()
  }
 }
}

/** Головне меню з переходами на ПЗ 5–8. */
@Composable private fun HomeScreen(onOpen:(String)->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
  Column(Modifier.fillMaxWidth().widthIn(max=550.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
   Text("Сонячна електростанція",style=MaterialTheme.typography.headlineSmall)
   Text("Навчальна система для прогнозу й збереження енергетичних показників")
   listOf(Triple("ПЗ 5 · Головне меню","Navigation Compose",Routes.NAV),Triple("ПЗ 6 · Форми","Валідація та підказки",Routes.FORM),Triple("ПЗ 7 · MVVM","ViewModel + StateFlow",Routes.MVVM),Triple("ПЗ 8 · Room","База даних та історія",Routes.ROOM)).forEach { (name,description,dest) ->
    ElevatedCard(onClick={onOpen(dest)},modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(name,style=MaterialTheme.typography.titleMedium);Text(description) } }
   }
  }
 }
}

/** Повторно використовуване поле вводу з helper/error текстом. */
@Composable private fun Field(value:String,onValueChange:(String)->Unit,label:String,hint:String,error:String?=null,secret:Boolean=false) {
 OutlinedTextField(value=value,onValueChange=onValueChange,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=true,isError=error!=null,visualTransformation=if(secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,supportingText={Text(error?:hint)})
}

private fun validEmail(email:String)=Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)

/** ПЗ 6: динамічна перевірка назви/email/пароля та числових параметрів. */
@Composable private fun FormScreen(vm:EnergyViewModel,onBack:()->Unit) {
 var name by rememberSaveable { mutableStateOf("") }
 var location by rememberSaveable { mutableStateOf("") }
 var email by rememberSaveable { mutableStateOf("") }
 var password by rememberSaveable { mutableStateOf("") }
 var touched by rememberSaveable { mutableStateOf(false) }
 val stations by vm.stations.collectAsStateWithLifecycle()
 val nameError=if(touched && name.trim().length<3) "Мінімум 3 символи" else null
 val emailError=if(touched && !validEmail(email)) "Некоректний email" else null
 val passError=if(touched && (password.length<8 || !password.any(Char::isDigit) || !password.any(Char::isLetter))) "8+ символів, літера та цифра" else null
 Page("ПЗ 6 · Реєстрація станції",onBack) {
  Text("Валідація виконується безпосередньо при введенні; дані станції зберігаються локально.")
  Field(name,{name=it; touched=true},"Назва станції","Мінімум 3 символи",nameError)
  Field(location,{location=it},"Місце розташування","Необов'язково")
  Field(email,{email=it; touched=true},"Email оператора","name@example.com",emailError)
  Field(password,{password=it; touched=true},"Пароль (демонстрація)","8+ символів, літера та цифра",passError,true)
  Text("Пароль використовується лише для перевірки форми; не зберігається в Room.",style=MaterialTheme.typography.bodySmall)
  Button(onClick={vm.saveStation(name,location)},enabled=name.trim().length>=3 && validEmail(email) && password.length>=8 && password.any(Char::isDigit) && password.any(Char::isLetter),modifier=Modifier.fillMaxWidth()) { Text("Зберегти станцію") }
  Text("Збережених станцій: ${stations.size}")
 }
}

/** Параметри, які можна використовувати в ПЗ 7 та ПЗ 8. */
@Composable private fun PowerFields(g:String,onG:(String)->Unit,eta:String,onEta:(String)->Unit,area:String,onArea:(String)->Unit) {
 Field(g,onG,"Сонячна радіація, Вт/м²","0–1500")
 Field(eta,onEta,"ККД, %","0–100")
 Field(area,onArea,"Площа панелей, м²","Більше нуля")
}

/** ПЗ 7: стан UI надходить із ViewModel через StateFlow. */
@Composable private fun MvvmScreen(vm:EnergyViewModel,onBack:()->Unit) {
 var g by rememberSaveable { mutableStateOf("800") };var eta by rememberSaveable { mutableStateOf("20") };var area by rememberSaveable { mutableStateOf("10") }
 val preview by vm.preview.collectAsStateWithLifecycle();val loading by vm.loading.collectAsStateWithLifecycle()
 Page("ПЗ 7 · MVVM / StateFlow",onBack) {
  PowerFields(g,{g=it},eta,{eta=it},area,{area=it})
  Button(onClick={vm.calculate(g,eta,area)},enabled=!loading,modifier=Modifier.fillMaxWidth()) {Text(if(loading) "Обробка…" else "Асинхронно розрахувати")}
  ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Прогноз потужності"); Text("%.1f Вт".format(preview),style=MaterialTheme.typography.headlineMedium); Text("P = G × η / 100 × A") } }
 }
}

/** ПЗ 8: Room CRUD та реактивний список із фільтром і сортуванням. */
@Composable private fun RoomScreen(vm:EnergyViewModel,onBack:()->Unit) {
 var g by rememberSaveable { mutableStateOf("800") };var eta by rememberSaveable { mutableStateOf("20") };var area by rememberSaveable { mutableStateOf("10") }
 val stations by vm.stations.collectAsStateWithLifecycle();val rows by vm.visibleMeasurements.collectAsStateWithLifecycle();val search by vm.filter.collectAsStateWithLifecycle();val sortDesc by vm.sortDescending.collectAsStateWithLifecycle();val loading by vm.loading.collectAsStateWithLifecycle()
 var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
 LaunchedEffect(stations) { if(selectedId==null || stations.none {it.id==selectedId}) selectedId=stations.firstOrNull()?.id }
 Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
  Column(Modifier.widthIn(max=650.dp).fillMaxWidth()) {
   Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick=onBack){Text("← Меню")};Text("ПЗ 8 · Room Database",style=MaterialTheme.typography.titleLarge)}
   Text("Показники зберігаються після закриття застосунку")
   if(stations.isEmpty()) Text("Спочатку додайте станцію в ПЗ 6.") else {
    Text("Станція: ${stations.firstOrNull {it.id==selectedId}?.name ?: "—"}")
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) { stations.take(3).forEach { station -> FilterChip(selected=selectedId==station.id,onClick={selectedId=station.id},label={Text(station.name.take(12))}) } }
   }
   PowerFields(g,{g=it},eta,{eta=it},area,{area=it})
   Button(onClick={vm.saveReading(selectedId,g,eta,area)},enabled=!loading && selectedId!=null,modifier=Modifier.fillMaxWidth()) { Text("Додати показник") }
   OutlinedTextField(value=search,onValueChange=vm::search,label={Text("Фільтр за потужністю або ID станції")},modifier=Modifier.fillMaxWidth())
   TextButton(onClick=vm::toggleSort){Text(if(sortDesc) "Сортування: нові → старі" else "Сортування: старі → нові")}
   HorizontalDivider()
  }
  LazyColumn(Modifier.widthIn(max=650.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   items(rows,key={it.id}) { item -> ReadingCard(item,onDelete={vm.deleteReading(item)},onUpdate={vm.updateReading(item)}) }
  }
 }
}

/** Повторно використовувана картка запису з операціями Update та Delete. */
@Composable private fun ReadingCard(item:MeasurementEntity,onDelete:()->Unit,onUpdate:()->Unit) {
 ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("Станція #${item.stationId} · %.1f Вт".format(item.power),style=MaterialTheme.typography.titleMedium); Text("G = ${item.irradiance}; η = ${item.efficiency}%; A = ${item.area} м²"); Row { TextButton(onClick=onUpdate){Text("Оновити +25 G")}; TextButton(onClick=onDelete){Text("Видалити")}} } }
}
