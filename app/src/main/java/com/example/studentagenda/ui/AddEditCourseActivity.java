package com.example.studentagenda.ui;

// Import des classes nécessaires pour l'activité, les vues, le TimePicker et le ViewModel

import com.example.studentagenda.notification.NotificationHelper;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentagenda.data.CourseDatabase;
import com.example.studentagenda.data.entity.Course;
import com.example.studentagenda.databinding.ActivityAddEditCourseBinding;
import com.example.studentagenda.ui.viewmodel.CourseViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

// Définition de l'activité pour ajouter ou modifier un cours
public class AddEditCourseActivity extends AppCompatActivity {

    // Binding pour accéder facilement aux vues
    private ActivityAddEditCourseBinding binding;
    // ID du cours à modifier (-1 signifie qu'on ajoute un nouveau cours)
    private long courseId = -1;
    // ViewModel pour gérer les opérations sur les cours
    private CourseViewModel viewModel;
    // Calendrier utilisé pour la sélection de l'heure
    private final Calendar calendar = Calendar.getInstance();
    // Format pour l'affichage et le parsing des heures
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisation du binding
        binding = ActivityAddEditCourseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Récupération de l'ID du cours passé en Intent (si présent)
        courseId = getIntent().getLongExtra("COURSE_ID", -1);
        initializeViewModel(); // Initialisation du ViewModel
        setupUI();             // Configuration des composants UI

        // Définition du titre de la toolbar selon qu'on ajoute ou modifie
        if (courseId != -1) {
            binding.toolbar.setTitle("Modifier le cours");
            loadCourseData(); // Charger les données du cours existant
        } else {
            binding.toolbar.setTitle("Ajouter un cours");
        }

        // Activation du bouton retour dans la barre d'action
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    // Méthode pour initialiser le ViewModel
    private void initializeViewModel() {
        CourseDatabase database = CourseDatabase.getInstance(this); // Obtenir l'instance de la DB
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CourseViewModel(database.courseDao()); // Création du ViewModel avec le DAO
            }
        }).get(CourseViewModel.class);
    }

    // Méthode pour configurer les composants UI
    private void setupUI() {
        setupSpinners();      // Configuration des spinners pour type et jour
        setupTimePickers();   // Configuration des TimePickers
        setupClickListeners();// Configuration des boutons
    }

    // Configuration des spinners (type de cours et jour de la semaine)
    private void setupSpinners() {
        // Adapter pour le type de cours
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getCourseTypeNames() // Récupère les noms des types de cours
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerType.setAdapter(typeAdapter);

        // Adapter pour les jours de la semaine
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                days
        );
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDay.setAdapter(dayAdapter);
    }

    // Récupère les noms des types de cours à partir de l'enum
    private String[] getCourseTypeNames() {
        Course.CourseType[] types = Course.CourseType.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            typeNames[i] = types[i].name(); // Conversion de l'enum en String
        }
        return typeNames;
    }

    // Configuration des TimePickers pour les heures de début et de fin
    private void setupTimePickers() {
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));

        // Définir l'heure actuelle comme valeur par défaut pour le début
        String currentTime = timeFormat.format(Calendar.getInstance().getTime());
        binding.etStartTime.setText(currentTime);

        // Définir l'heure de fin par défaut à 1h30 plus tard
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR, 1);
        endCalendar.add(Calendar.MINUTE, 30);
        binding.etEndTime.setText(timeFormat.format(endCalendar.getTime()));
    }

    // Configuration des boutons Save et Cancel
    private void setupClickListeners() {
        // Enregistrer le cours si le formulaire est valide
        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                saveCourse();
            }
        });

        // Annuler et fermer l'activité
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    // Affiche un TimePickerDialog
    private void showTimePicker(final boolean isStartTime) {
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    String selectedTime = timeFormat.format(calendar.getTime());

                    if (isStartTime) {
                        binding.etStartTime.setText(selectedTime);
                    } else {
                        binding.etEndTime.setText(selectedTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.setTitle(isStartTime ? "Heure de début" : "Heure de fin");
        timePicker.show();
    }

    // Validation des champs du formulaire
    private boolean validateForm() {
        String name = Objects.requireNonNull(binding.etCourseName.getText()).toString().trim();
        String professor = Objects.requireNonNull(binding.etProfessor.getText()).toString().trim();
        String room = Objects.requireNonNull(binding.etRoom.getText()).toString().trim();
        String startTime = binding.etStartTime.getText().toString().trim();
        String endTime = binding.etEndTime.getText().toString().trim();

        boolean isValid = true;

        // Vérification des champs obligatoires
        if (name.isEmpty()) {
            binding.etCourseName.setError("Le nom du cours est obligatoire");
            isValid = false;
        }

        if (professor.isEmpty()) {
            binding.etProfessor.setError("Le nom du professeur est obligatoire");
            isValid = false;
        }

        if (room.isEmpty()) {
            binding.etRoom.setError("La salle est obligatoire");
            isValid = false;
        }

        if (startTime.isEmpty()) {
            binding.etStartTime.setError("L'heure de début est obligatoire");
            isValid = false;
        }

        if (endTime.isEmpty()) {
            binding.etEndTime.setError("L'heure de fin est obligatoire");
            isValid = false;
        }

        // Vérification que l'heure de fin est après l'heure de début
        if (isValid && !isTimeValid(startTime, endTime)) {
            Toast.makeText(this, "L'heure de fin doit être après l'heure de début", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    // Vérifie que l'heure de fin est après l'heure de début
    private boolean isTimeValid(String startTime, String endTime) {
        try {
            java.util.Date start = timeFormat.parse(startTime);
            java.util.Date end = timeFormat.parse(endTime);
            return start != null && end != null && end.after(start);
        } catch (ParseException e) {
            return false; // Retourne false si parsing échoue
        }
    }

    // Sauvegarde ou met à jour un cours
    private void saveCourse() {
        Course course = new Course();
        if (courseId != -1) {
            course.setId(courseId); // Si on modifie un cours, on définit son ID
        }

        // Récupération des données du formulaire
        course.setName(Objects.requireNonNull(binding.etCourseName.getText()).toString().trim());
        course.setProfessor(Objects.requireNonNull(binding.etProfessor.getText()).toString().trim());
        course.setRoom(Objects.requireNonNull(binding.etRoom.getText()).toString().trim());
        course.setType(Course.CourseType.valueOf(binding.spinnerType.getSelectedItem().toString()));
        course.setDayOfWeek(binding.spinnerDay.getSelectedItemPosition() + 1);
        course.setStartTime(binding.etStartTime.getText().toString().trim());
        course.setEndTime(binding.etEndTime.getText().toString().trim());
        course.setNotificationEnabled(binding.switchNotification.isChecked());

        // Insertion ou mise à jour dans la base de données
        if (courseId != -1) {

            // Annuler l'ancien rappel avant de mettre à jour
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.cancelScheduledReminder(courseId);

            viewModel.update(course);
            Toast.makeText(this, "Cours modifié avec succès", Toast.LENGTH_SHORT).show();

        } else {
            viewModel.insert(course);
            Toast.makeText(this, "Cours ajouté avec succès", Toast.LENGTH_SHORT).show();
        }


        // Si les notifications sont activées
        if (course.isNotificationEnabled()) {
            NotificationHelper notificationHelper = new NotificationHelper(this);

            // 1. Notification immédiate
            notificationHelper.showImmediateNotification(course);

            // 2. Planifier rappel 15 minutes avant
            notificationHelper.scheduleReminder(course, 15);

            // Message supplémentaire
            String message = (courseId != -1) ?
                    "Cours modifié et rappel planifié 15 minutes avant" :
                    "Cours ajouté et rappel planifié 15 minutes avant";

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            // Si les notifications sont désactivées, annuler les rappels existants
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.cancelScheduledReminder(course.getId());
        }

        finish(); // Ferme l'activité
    }

    // Charge les données d'un cours existant pour modification
    private void loadCourseData() {
        viewModel.getCourseById(courseId, new CourseViewModel.CourseCallback() {
            @Override
            public void onCourseLoaded(Course course) {
                if (course != null) {
                    runOnUiThread(() -> {
                        binding.etCourseName.setText(course.getName());
                        binding.etProfessor.setText(course.getProfessor());
                        binding.etRoom.setText(course.getRoom());
                        binding.etStartTime.setText(course.getStartTime());
                        binding.etEndTime.setText(course.getEndTime());

                        // Sélection du type de cours
                        ArrayAdapter adapter = (ArrayAdapter) binding.spinnerType.getAdapter();
                        int position = adapter.getPosition(course.getType().name());
                        if (position >= 0) {
                            binding.spinnerType.setSelection(position);
                        }

                        // Sélection du jour
                        binding.spinnerDay.setSelection(course.getDayOfWeek() - 1);

                        // Activation ou non de la notification
                        binding.switchNotification.setChecked(course.isNotificationEnabled());
                    });
                }
            }
        });
    }

    // Gestion du bouton "retour" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Ferme l'activité
        return true;
    }

     //Annule les rappels planifiés quand l'activité est détruite

    @Override
    protected void onDestroy() {
        // Si l'activité est détruite sans sauvegarde, annuler les rappels temporaires
        // (Utile en cas d'annulation)
        super.onDestroy();
    }

}