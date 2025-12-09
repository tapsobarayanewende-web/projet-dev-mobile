package com.example.studentagenda.ui;

// Import des classes nécessaires pour l'activité, les vues, le ViewModel et les dialogues
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentagenda.R;
import com.example.studentagenda.data.CourseDatabase;
import com.example.studentagenda.data.entity.Course;
import com.example.studentagenda.databinding.ActivityCourseDetailBinding;
import com.example.studentagenda.ui.viewmodel.CourseViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

// Activité affichant les détails d'un cours
public class CourseDetailActivity extends AppCompatActivity {

    // Binding pour accéder facilement aux vues de l'UI
    private ActivityCourseDetailBinding binding;
    // ID du cours à afficher
    private long courseId = -1;
    // ViewModel pour manipuler les cours
    private CourseViewModel viewModel;
    // Référence au cours actuellement chargé
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisation du binding
        binding = ActivityCourseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Récupération de l'ID du cours passé en Intent
        courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId == -1) { // Si pas d'ID, fermer l'activité
            finish();
            return;
        }

        initializeViewModel(); // Initialisation du ViewModel
        setupUI();             // Configuration de l'UI
        loadCourseData();      // Chargement des détails du cours

        // Configuration de la toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Détails du cours");
    }

    // Initialisation du ViewModel avec le DAO
    private void initializeViewModel() {
        CourseDatabase database = CourseDatabase.getInstance(this);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CourseViewModel(database.courseDao());
            }
        }).get(CourseViewModel.class);
    }

    // Configuration des clics sur les boutons
    private void setupUI() {
        setupClickListeners();
    }

    // Définition des actions pour les boutons Edit et Delete
    private void setupClickListeners() {
        // Bouton modifier : ouvre AddEditCourseActivity avec l'ID du cours
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailActivity.this, AddEditCourseActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });

        // Bouton supprimer : affiche un dialogue de confirmation
        binding.btnDelete.setOnClickListener(v -> {
            if (currentCourse != null) {
                showDeleteConfirmationDialog(currentCourse);
            }
        });
    }

    // Chargement du cours depuis la base de données via le ViewModel
    private void loadCourseData() {
        viewModel.getCourseById(courseId, new CourseViewModel.CourseCallback() {
            @Override
            public void onCourseLoaded(Course course) {
                currentCourse = course;
                if (course != null) {
                    runOnUiThread(() -> {
                        displayCourseDetails(course); // Affichage des détails
                    });
                }
            }
        });
    }

    // Affichage des détails du cours dans l'UI
    private void displayCourseDetails(Course course) {
        binding.tvCourseName.setText(course.getName());
        binding.tvProfessor.setText(course.getProfessor());
        binding.tvRoom.setText(course.getRoom());
        binding.tvType.setText(course.getType().name());
        binding.tvDay.setText(course.getDayName());
        binding.tvTime.setText(course.getStartTime() + " - " + course.getEndTime());
        binding.tvDuration.setText(calculateDuration(course.getStartTime(), course.getEndTime()));

        // Changement de la couleur de fond selon le type de cours
        int typeColor;
        switch (course.getType()) {
            case CM:
                typeColor = R.color.cm_color;
                break;
            case TD:
                typeColor = R.color.td_color;
                break;
            case TP:
                typeColor = R.color.tp_color;
                break;
            default:
                typeColor = R.color.autre_color;
        }
        binding.tvType.setBackgroundResource(typeColor);

        // Affichage d'informations supplémentaires
        displayAdditionalInfo(course);
    }

    // Calcul de la durée du cours à partir des heures de début et fin
    private String calculateDuration(String startTime, String endTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            java.util.Date start = format.parse(startTime);
            java.util.Date end = format.parse(endTime);

            if (start != null && end != null) {
                long diff = end.getTime() - start.getTime();
                long hours = diff / (60 * 60 * 1000);
                long minutes = (diff % (60 * 60 * 1000)) / (60 * 1000);

                if (hours > 0) {
                    return hours + "h" + (minutes > 0 ? minutes + "min" : "");
                } else {
                    return minutes + "min";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "N/A"; // Retourne N/A si le parsing échoue
    }

    // Affichage des informations détaillées sous forme de texte
    private void displayAdditionalInfo(Course course) {
        String info = "• Cours: " + course.getName() + "\n" +
                "• Professeur: " + course.getProfessor() + "\n" +
                "• Salle: " + course.getRoom() + "\n" +
                "• Type: " + course.getType().name() + "\n" +
                "• Jour: " + course.getDayName() + "\n" +
                "• Horaire: " + course.getStartTime() + " - " + course.getEndTime() + "\n" +
                "• Durée: " + calculateDuration(course.getStartTime(), course.getEndTime());

        binding.tvAdditionalInfo.setText(info);
    }

    // Affiche un dialogue pour confirmer la suppression d'un cours
    private void showDeleteConfirmationDialog(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le cours")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + course.getName() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.delete(course); // Suppression du cours
                    Toast.makeText(this, "Cours supprimé", Toast.LENGTH_SHORT).show();
                    finish(); // Ferme l'activité après suppression
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Gestion du bouton retour de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Ferme l'activité
        return true;
    }
}
