package com.example.studentagenda.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.studentagenda.data.CourseTypeConverter;


import java.util.Objects;

@Entity(tableName = "courses") // Table Room représentant un cours
@TypeConverters(CourseTypeConverter.class) // Convertisseur pour CourseType
public class Course {

    @PrimaryKey(autoGenerate = true) // ID généré automatiquement
    private long id;

    private String name;               // Nom du cours
    private String professor;          // Nom du professeur
    private String room;               // Salle
    private CourseType type;           // Type (CM, TD, TP...)
    private int dayOfWeek;             // Jour de la semaine (1 = Lundi)
    private String startTime;          // Heure de début
    private String endTime;            // Heure de fin
    private boolean notificationEnabled; // Notification activée ou non

    // Constructeur vide requis par Room
    public Course() { }

    // Retourne le nom comme titre (utilisé dans certaines vues)
    public String getTitle() {
        return name;
    }

    // Types possibles de cours
    public enum CourseType {
        CM, TD, TP, AUTRE
    }

    // Constructeur complet
    public Course(long id, String name, String professor, String room,
                  CourseType type, int dayOfWeek, String startTime,
                  String endTime, boolean notificationEnabled) {
        this.id = id;
        this.name = name;
        this.professor = professor;
        this.room = room;
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notificationEnabled = notificationEnabled;
    }

    // Getters / Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public CourseType getType() { return type; }
    public void setType(CourseType type) { this.type = type; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    // Renvoie le nom du jour en texte
    public String getDayName() {
        switch(dayOfWeek) {
            case 1: return "Lundi";
            case 2: return "Mardi";
            case 3: return "Mercredi";
            case 4: return "Jeudi";
            case 5: return "Vendredi";
            case 6: return "Samedi";
            case 7: return "Dimanche";
            default: return "Inconnu";
        }
    }

    // Comparaison logique entre deux objets Course
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id == course.id &&
                dayOfWeek == course.dayOfWeek &&
                notificationEnabled == course.notificationEnabled &&
                Objects.equals(name, course.name) &&
                Objects.equals(professor, course.professor) &&
                Objects.equals(room, course.room) &&
                type == course.type &&
                Objects.equals(startTime, course.startTime) &&
                Objects.equals(endTime, course.endTime);
    }

    // Hash pour le comparer dans les structures de données
    @Override
    public int hashCode() {
        return Objects.hash(id, name, professor, room, type,
                dayOfWeek, startTime, endTime, notificationEnabled);
    }
}
