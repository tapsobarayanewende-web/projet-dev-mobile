package com.example.studentagenda.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.example.studentagenda.data.entity.Course;

@Database(
        entities = {Course.class},   // Entités gérées par la base
        version = 1,                 // Version de la base Room
        exportSchema = false         // Désactive l’export du schéma
)
@TypeConverters({CourseTypeConverter.class}) // Convertisseur utilisé pour certains types
public abstract class CourseDatabase extends RoomDatabase {

    // DAO accessible depuis la base
    public abstract CourseDao courseDao();

    // Instance unique (Singleton)
    private static volatile CourseDatabase INSTANCE;

    // Retourne l’instance unique de la base
    public static CourseDatabase getInstance(Context context) {
        if (INSTANCE == null) { // Première vérification
            synchronized (CourseDatabase.class) { // Sécurisation multithread
                if (INSTANCE == null) { // Deuxième vérification
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CourseDatabase.class,
                                    "course_database"          // Nom du fichier de base de données
                            )
                            // Permet de recréer la base si migration manquante
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .fallbackToDestructiveMigration()
                            .build(); // Création de la base
                }
            }
        }
        return INSTANCE;
    }
}
