package com.example.studentagenda.data;

import androidx.room.TypeConverter;
import com.example.studentagenda.data.entity.Course;

public class CourseTypeConverter {

    // Convertit une chaîne stockée en base vers l'énumération CourseType
    @TypeConverter
    public static Course.CourseType toCourseType(String value) {
        if (value == null) {
            return null; // Aucun type enregistré
        }
        try {
            return Course.CourseType.valueOf(value); // Conversion normale
        } catch (IllegalArgumentException e) {
            return Course.CourseType.AUTRE; // Si la valeur ne correspond à rien
        }
    }

    // Convertit l'énumération CourseType vers une chaîne pour Room
    @TypeConverter
    public static String fromCourseType(Course.CourseType type) {
        return type == null ? null : type.name(); // Retourne le nom exact de l'Enum
    }
}
