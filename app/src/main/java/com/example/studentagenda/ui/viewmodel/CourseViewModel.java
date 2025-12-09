package com.example.studentagenda.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentagenda.data.CourseDao;
import com.example.studentagenda.data.entity.Course;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseViewModel extends ViewModel {

    private final CourseDao courseDao;                // DAO pour accéder aux données
    private final ExecutorService executorService;    // Exécuteur pour opérations en arrière-plan
    private final LiveData<List<Course>> allCourses;  // Liste observable de tous les cours

    public CourseViewModel(CourseDao courseDao) {
        this.courseDao = courseDao;
        this.executorService = Executors.newSingleThreadExecutor(); // Thread unique pour Room
        this.allCourses = courseDao.getAllCourses();                // Récupération des cours
    }

    // Retourne tous les cours observables
    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }

    // Cours filtrés par jour
    public LiveData<List<Course>> getCoursesByDay(int day) {
        return courseDao.getCoursesByDay(day);
    }

    // Recherche de cours via une requête
    public LiveData<List<Course>> searchCourses(String query) {
        return courseDao.searchCourses(query);
    }

    // Insertion en arrière-plan
    public void insert(Course course) {
        executorService.execute(() -> {
            courseDao.insert(course);
        });
    }

    // Mise à jour
    public void update(Course course) {
        executorService.execute(() -> {
            courseDao.update(course);
        });
    }

    // Suppression d’un cours
    public void delete(Course course) {
        executorService.execute(() -> {
            courseDao.delete(course);
        });
    }

    // Récupère un cours par son id via un callback
    public void getCourseById(long courseId, CourseCallback callback) {
        executorService.execute(() -> {
            Course course = courseDao.getCourseById(courseId);
            callback.onCourseLoaded(course);
        });
    }

    // Supprime tous les cours
    public void deleteAllCourses() {
        executorService.execute(courseDao::deleteAllCourses);
    }

    // Interface callback pour retourner un cours hors du thread principal
    public interface CourseCallback {
        void onCourseLoaded(Course course);
    }
}
