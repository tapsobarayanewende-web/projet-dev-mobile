package com.example.studentagenda.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.studentagenda.data.entity.Course;
import java.util.List;

@Dao
public interface CourseDao {

    // Récupère tous les cours triés par jour puis par heure de début
    @Query("SELECT * FROM courses ORDER BY dayOfWeek, startTime")
    LiveData<List<Course>> getAllCourses();

    // Récupère tous les cours d’un jour précis
    @Query("SELECT * FROM courses WHERE dayOfWeek = :day ORDER BY startTime")
    LiveData<List<Course>> getCoursesByDay(int day);

    // Recherche par nom, professeur ou salle
    @Query("SELECT * FROM courses WHERE name LIKE '%' || :query || '%' OR professor LIKE '%' || :query || '%' OR room LIKE '%' || :query || '%'")
    LiveData<List<Course>> searchCourses(String query);

    // Insère un nouveau cours
    @Insert
    void insert(Course course);

    // Met à jour un cours existant
    @Update
    void update(Course course);

    // Supprime un cours
    @Delete
    void delete(Course course);

    // Récupère un cours par son ID (méthode synchrone)
    @Query("SELECT * FROM courses WHERE id = :courseId")
    Course getCourseById(long courseId);

    // Supprime tous les cours
    @Query("DELETE FROM courses")
    void deleteAllCourses();
}
