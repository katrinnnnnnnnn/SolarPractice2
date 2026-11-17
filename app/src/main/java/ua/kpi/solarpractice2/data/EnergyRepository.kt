package ua.kpi.solarpractice2.data

import kotlinx.coroutines.flow.Flow
import ua.kpi.solarpractice.core.EnergyCalculator

/** Repository із єдиним джерелом правди Room і кешем останнього прогнозу в пам'яті. */
class EnergyRepository(private val dao: EnergyDao) {
 val stations: Flow<List<StationEntity>> = dao.observeStations()
 val measurements: Flow<List<MeasurementEntity>> = dao.observeMeasurements()
 private var lastPreview: Double? = null
 fun preview(g: Double, eta: Double, area: Double): Double = EnergyCalculator.watts(g, eta, area).also { lastPreview = it }
 fun cachedPreview(): Double? = lastPreview
 suspend fun addStation(name: String, location: String): Long = dao.insertStation(StationEntity(name=name.trim(),location=location.trim()))
 suspend fun addReading(stationId: Long,g:Double,eta:Double,area:Double): Long = dao.insertMeasurement(MeasurementEntity(stationId=stationId,irradiance=g,efficiency=eta,area=area,power=preview(g,eta,area)))
 suspend fun delete(reading: MeasurementEntity) = dao.deleteMeasurement(reading)
 suspend fun update(reading: MeasurementEntity) = dao.updateMeasurement(reading.copy(power=preview(reading.irradiance,reading.efficiency,reading.area)))
 suspend fun average(stationId: Long) = dao.averagePower(stationId)
 suspend fun details() = dao.stationDetails()
}
