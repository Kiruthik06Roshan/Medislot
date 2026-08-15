package com.medislot.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.medislot.app.data.local.dao.*
import com.medislot.app.data.local.entity.*

@Database(
    entities = [
        LocalPatientProfile::class,
        LocalDoctorProfile::class,
        LocalAppointment::class,
        LocalStaffSchedule::class,
        LocalLeaveRequest::class,
        LocalInventoryItem::class,
        LocalOperationalAlert::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun staffScheduleDao(): StaffScheduleDao
    abstract fun leaveRequestDao(): LeaveRequestDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun operationalAlertDao(): OperationalAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medislot_hms_local_cache_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
