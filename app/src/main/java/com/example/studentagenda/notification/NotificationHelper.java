package com.example.studentagenda.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.studentagenda.R;
import com.example.studentagenda.data.entity.Course;
import com.example.studentagenda.MainActivity;
import android.app.PendingIntent;
import android.content.Intent;



public class NotificationHelper {

    private final Context context;
    private static final String CHANNEL_ID = "course_reminders_channel";
    private static final String CHANNEL_NAME = "Rappels de cours";

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel(); // Création du canal (obligatoire Android 8+)
    }

    // Création du canal de notification pour Android O+
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Importance élevée
            );

            channel.setDescription("Notifications de rappel pour les cours");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Affiche immédiatement une notification pour un cours
    public void showImmediateNotification(Course course) {
        NotificationCompat.Builder builder = buildNotification(course);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify((int) course.getId(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace(); // Cas où la permission POST_NOTIFICATIONS manque
        }
    }

    // Construction du contenu de la notification
    private NotificationCompat.Builder buildNotification(Course course) {

        // Intent pour ouvrir l'activité principale quand on clique sur la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Style détaillé pour afficher plusieurs lignes
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText("Cours: " + course.getName() +
                        "\nProfesseur: " + course.getProfessor() +
                        "\nSalle: " + course.getRoom() +
                        "\nHeure: " + course.getStartTime())
                .setBigContentTitle("📚 " + course.getName())
                .setSummaryText("Rappel de cours");

        // Construction finale de la notification
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_school)
                .setContentTitle("⏰ Cours bientôt")
                .setContentText(course.getName() + " avec " + course.getProfessor())
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Action d'ouverture
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    // Notification de test
    public void showTestNotification() {
        Course testCourse = new Course();
        testCourse.setId(999);
        testCourse.setName("Test de notification");
        testCourse.setProfessor("Professeur Test");
        testCourse.setRoom("Salle A1");
        testCourse.setStartTime("14:00");

        showImmediateNotification(testCourse); // Affiche la notification de test
    }

    private static final String TAG = "NotificationHelper";
    private static final String ACTION_REMINDER = "com.example.studentagenda.ACTION_REMINDER";

     //Planifie une notification 15 minutes avant un cours

    public void scheduleReminder(Course course, int minutesBefore) {
        if (course == null || !course.isNotificationEnabled()) {
            return;
        }

        try {
            // Convertir l'heure du cours en timestamp
            long courseTime = getCourseTimestamp(course);
            if (courseTime <= 0) {
                android.util.Log.e(TAG, "Impossible de calculer l'heure du cours");
                return;
            }

            // Soustraire X minutes
            long reminderTime = courseTime - (minutesBefore * 60 * 1000L);

            // Vérifier si le rappel est dans le futur
            long now = System.currentTimeMillis();
            if (reminderTime <= now) {
                android.util.Log.w(TAG, "Le cours est déjà passé ou dans moins de " + minutesBefore + " minutes");
                return;
            }

            // Créer l'intent pour le rappel
            Intent reminderIntent = new Intent(context, CourseNotificationReceiver.class);
            reminderIntent.setAction(ACTION_REMINDER);
            reminderIntent.putExtra("course_id", course.getId());
            reminderIntent.putExtra("course_name", course.getName());
            reminderIntent.putExtra("course_professor", course.getProfessor());
            reminderIntent.putExtra("course_room", course.getRoom());
            reminderIntent.putExtra("course_time", course.getStartTime());

            // Utiliser l'ID du cours pour avoir un PendingIntent unique
            int requestCode = (int) course.getId();

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    reminderIntent,
                    flags
            );

            // Planifier avec AlarmManager
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                } else {
                    alarmManager.set(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                }

                android.util.Log.d(TAG, "Rappel planifié pour: " + new java.util.Date(reminderTime));
            }

        } catch (Exception e) {
            android.util.Log.e(TAG, "Erreur lors de la planification du rappel", e);
        }
    }


        //Annule un rappel planifié

    public void cancelScheduledReminder(long courseId) {
        try {
            Intent reminderIntent = new Intent(context, CourseNotificationReceiver.class);
            reminderIntent.setAction(ACTION_REMINDER);

            int requestCode = (int) courseId;

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    reminderIntent,
                    flags
            );

            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Erreur lors de l'annulation du rappel", e);
        }
    }


        //Convertit l'heure du cours en timestamp

    private long getCourseTimestamp(Course course) {
        try {
            // Obtenir la date actuelle
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            // Convertir le jour de la semaine (1=Lundi) en constante Calendar
            int targetDayOfWeek = convertToCalendarDay(course.getDayOfWeek());
            int currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);

            // Calculer le décalage en jours
            int daysToAdd = targetDayOfWeek - currentDayOfWeek;
            if (daysToAdd < 0) {
                daysToAdd += 7; // Semaine suivante
            }

            // Ajouter les jours
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd);

            // Parser l'heure du cours
            String[] timeParts = course.getStartTime().split(":");
            if (timeParts.length >= 2) {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
                calendar.set(java.util.Calendar.MINUTE, minute);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);

                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Erreur de parsing de l'heure", e);
        }
        return 0;
    }


      //Convertit votre jour (1=Lundi) en constante Calendar

    private int convertToCalendarDay(int yourDayOfWeek) {
        switch (yourDayOfWeek) {
            case 1: return java.util.Calendar.MONDAY;
            case 2: return java.util.Calendar.TUESDAY;
            case 3: return java.util.Calendar.WEDNESDAY;
            case 4: return java.util.Calendar.THURSDAY;
            case 5: return java.util.Calendar.FRIDAY;
            case 6: return java.util.Calendar.SATURDAY;
            case 7: return java.util.Calendar.SUNDAY;
            default: return java.util.Calendar.MONDAY;
        }
    }


         //Replanifie tous les rappels

    public void rescheduleAllReminders(java.util.List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return;
        }

        // Replanifier seulement les cours avec notifications activées
        for (Course course : courses) {
            cancelScheduledReminder(course.getId());
            if (course.isNotificationEnabled()) {
                scheduleReminder(course, 15); // 15 minutes avant
            }
        }
    }


              //   Affiche une notification de rappel

    public void showReminderNotification(Course course) {
        if (course == null) {
            return;
        }

        NotificationCompat.Builder builder = buildReminderNotification(course);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            int notificationId = (int) (course.getId() % Integer.MAX_VALUE);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            android.util.Log.e(TAG, "Permission manquante pour les notifications", e);
        }
    }

    //Construit une notification de rappel spéciale

    private NotificationCompat.Builder buildReminderNotification(Course course) {
        // Intent pour ouvrir l'application
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("course_id", course.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) course.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Style détaillé pour les rappels
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText("Cours: " + course.getName() +
                        "\nProfesseur: " + course.getProfessor() +
                        "\nSalle: " + course.getRoom() +
                        "\nHeure: " + course.getStartTime() +
                        "\n\n⚠️ Cours dans 15 minutes !")
                .setBigContentTitle("⏰ URGENT: " + course.getName())
                .setSummaryText("Rappel - Cours imminent");

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_school)
                .setContentTitle("⏰ COURS DANS 15 MINUTES!")
                .setContentText(course.getName() + " avec " + course.getProfessor())
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    //Test de notification planifiée

    public void scheduleTestReminder() {
        Course testCourse = new Course();
        testCourse.setId(System.currentTimeMillis());
        testCourse.setName("TEST Planifié");
        testCourse.setProfessor("Professeur Test");
        testCourse.setRoom("Salle Test");
        testCourse.setStartTime(getTimeIn1Minute());
        testCourse.setDayOfWeek(getCurrentDayOfWeek());
        testCourse.setNotificationEnabled(true);

        // Planifier notification dans 1 minute
        scheduleReminder(testCourse, 1);
    }

    /**
     * Méthodes auxiliaires pour les tests
     */
    private String getTimeIn1Minute() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MINUTE, 1);
        return String.format("%02d:%02d",
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE));
    }

    private int getCurrentDayOfWeek() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int calendarDay = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        // Convertir Calendar day (1=Sunday) vers votre format (1=Monday)
        switch (calendarDay) {
            case java.util.Calendar.MONDAY: return 1;
            case java.util.Calendar.TUESDAY: return 2;
            case java.util.Calendar.WEDNESDAY: return 3;
            case java.util.Calendar.THURSDAY: return 4;
            case java.util.Calendar.FRIDAY: return 5;
            case java.util.Calendar.SATURDAY: return 6;
            case java.util.Calendar.SUNDAY: return 7;
            default: return 1;
        }
    }
}
