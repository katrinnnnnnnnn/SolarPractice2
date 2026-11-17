package ua.kpi.solarpractice2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** Перша таблиця: станції. */
@Entity(tableName = "stations")
data class StationEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val location: String)

/** Друга таблиця: заміри з FK станції (один-до-багатьох). */
@Entity(tableName = "measurements", foreignKeys = [ForeignKey(entity = StationEntity::class, parentColumns = ["id"], childColumns = ["stationId"], onDelete = ForeignKey.CASCADE)], indices = [Index("stationId")])
data class MeasurementEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val stationId: Long, val irradiance: Double, val efficiency: Double, val area: Double, val power: Double, val timestamp: Long = System.currentTimeMillis())

data class StationWithMeasurements(@Embedded val station: StationEntity, @Relation(parentColumn = "id", entityColumn = "stationId") val measurements: List<MeasurementEntity>)

@Dao
interface EnergyDao {
 @Query("SELECT * FROM stations ORDER BY id DESC") fun observeStations(): Flow<List<StationEntity>>
 @Query("SELECT * FROM measurements ORDER BY timestamp DESC, id DESC") fun observeMeasurements(): Flow<List<MeasurementEntity>>
 @Transaction @Query("SELECT * FROM stations") suspend fun stationDetails(): List<StationWithMeasurements>
 @Insert suspend fun insertStation(station: StationEntity): Long
 @Insert suspend fun insertMeasurement(measurement: MeasurementEntity): Long
 @Update suspend fun updateMeasurement(measurement: MeasurementEntity)
 @Delete suspend fun deleteMeasurement(measurement: MeasurementEntity)
 @Query("DELETE FROM measurements WHERE stationId=:stationId") suspend fun clearStation(stationId: Long)
 @Query("SELECT COALESCE(AVG(power), 0.0) FROM measurements WHERE stationId=:stationId") suspend fun averagePower(stationId: Long): Double
}

@Database(entities = [StationEntity::class, MeasurementEntity::class], version = 1, exportSchema = false)
abstract class EnergyDatabase: RoomDatabase() { abstract fun energyDao(): EnergyDao }
