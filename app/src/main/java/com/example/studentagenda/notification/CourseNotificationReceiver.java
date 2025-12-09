package com.example.studentagenda.notification;

import com.example.studentagenda.data.entity.Course;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CourseNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "CourseNotificationReceiver";

    // Action pour les rappels planifiés
    private static final String ACTION_REMINDER = "com.example.studentagenda.ACTION_REMINDER";

    // Actions système
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Intent ou action null");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Receiver déclenché - Action: " + action);

        // Gestion des rappels planifiés
        if (ACTION_REMINDER.equals(action)) {
            handleCourseReminder(context, intent);
        }
        // Gestion du redémarrage du système
        else if (ACTION_BOOT_COMPLETED.equals(action) || ACTION_QUICKBOOT_POWERON.equals(action)) {
            handleBootCompleted(context);
        }
        // Pour d'autres actions futures
        else {
            Log.w(TAG, "Action non gérée: " + action);
        }
    }

    //Gère un rappel de cours planifié

    private void handleCourseReminder(Context context, Intent intent) {
        try {
            // Récupérer les données du cours depuis l'intent
            long courseId = intent.getLongExtra("course_id", -1);
            String courseName = intent.getStringExtra("course_name");
            String professor = intent.getStringExtra("course_professor");
            String room = intent.getStringExtra("course_room");
            String time = intent.getStringExtra("course_time");

            Log.d(TAG, "📚 Rappel reçu pour le cours: " + courseName +
                    " (ID: " + courseId + ")");

            if (courseId == -1 || courseName == null) {
                Log.e(TAG, "Données du cours incomplètes");
                return;
            }

            // Créer un objet Course temporaire
            Course course = new Course();
            course.setId(courseId);
            course.setName(courseName);
            course.setProfessor(professor != null ? professor : "Professeur");
            course.setRoom(room != null ? room : "Salle");
            course.setStartTime(time != null ? time : "");
            course.setNotificationEnabled(true);

            // Afficher la notification de rappel
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showReminderNotification(course);

            Log.i(TAG, "✅ Notification de rappel affichée pour: " + courseName);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors du traitement du rappel", e);
        }
    }

    //Gère le redémarrage du système

    private void handleBootCompleted(Context context) {
        Log.i(TAG, "🔄 Système redémarré - Prêt à replanifier les rappels");


    }

    // Méthode utilitaire pour créer un intent de rappel

    public static Intent createReminderIntent(Context context, Course course) {
        Intent intent = new Intent(context, CourseNotificationReceiver.class);
        intent.setAction(ACTION_REMINDER);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("course_name", course.getName());
        intent.putExtra("course_professor", course.getProfessor());
        intent.putExtra("course_room", course.getRoom());
        intent.putExtra("course_time", course.getStartTime());
        return intent;
    }
}