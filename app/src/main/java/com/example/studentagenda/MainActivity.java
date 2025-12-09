package com.example.studentagenda;

// Import des classes nécessaires pour l'activité, vues, recyclerView, ViewModel, AlertDialog, Intent, etc.
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studentagenda.adapter.CourseAdapter;
import com.example.studentagenda.data.CourseDatabase;
import com.example.studentagenda.data.entity.Course;
import com.example.studentagenda.databinding.ActivityMainBinding;
import com.example.studentagenda.ui.AddEditCourseActivity;
import com.example.studentagenda.ui.CourseDetailActivity;
import com.example.studentagenda.ui.viewmodel.CourseViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

// AJOUTEZ CES IMPORTS
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import com.example.studentagenda.notification.NotificationHelper;

// Activité principale affichant la liste des cours
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;   // Binding pour accéder aux vues
    private CourseAdapter courseAdapter;   // Adapter pour recyclerView
    private CourseViewModel viewModel;     // ViewModel pour manipuler les données des cours
    private Integer currentDayFilter = null; // Filtre du jour sélectionné (1=Lundi, 2=Mardi...)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // Initialisation du binding
        setContentView(binding.getRoot());

        // Initialisation du ViewModel et configuration de l'UI
        initializeViewModel();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupSearchBar();

        // AJOUTEZ CETTE LIGNE
        setupNotificationPermissions(); // Demander la permission pour les notifications

        loadAllCourses(); // Chargement initial de tous les cours


        // Replanifier tous les rappels au démarrage
        scheduleAllCourseReminders();


    }

    // AJOUTEZ CETTE MÉTHODE
    private void setupNotificationPermissions() {
        // Demande la permission pour les notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                // Demander la permission
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    // AJOUTEZ CETTE MÉTHODE POUR GÉRER LA RÉPONSE DE PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée - replanifier les rappels
                scheduleAllCourseReminders();
            } else {
                // Permission refusée
                // Vous pourriez montrer un message à l'utilisateur
            }
        }
    }

    // AJOUTEZ CETTE MÉTHODE POUR TESTER LES NOTIFICATIONS
    private void testNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.showTestNotification();
    }

  // Replanifie tous les rappels des cours existants

    private void scheduleAllCourseReminders() {
        viewModel.getAllCourses().observe(this, courses -> {
            if (courses != null && !courses.isEmpty()) {
                NotificationHelper helper = new NotificationHelper(this);
                helper.rescheduleAllReminders(courses);
            }
        });
    }
    // Test d'une notification planifiée (pour debug)

    private void testScheduledNotification() {
        NotificationHelper helper = new NotificationHelper(this);
        helper.scheduleTestReminder();

        // Afficher un message à l'utilisateur
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Test de rappel planifié")
                .setMessage("Un rappel de test a été planifié pour dans 1 minute.\n" +
                        "Vous recevrez une notification automatiquement.")
                .setPositiveButton("OK", null)
                .show();
    }


    // Initialisation du ViewModel avec le DAO de la base de données
    private void initializeViewModel() {
        CourseDatabase database = CourseDatabase.getInstance(this);
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(CourseViewModel.class)) {
                    return (T) new CourseViewModel(database.courseDao());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
        viewModel = new ViewModelProvider(this, factory).get(CourseViewModel.class);
    }

    // Configuration du RecyclerView avec son adapter et layout manager
    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(
                this::onCourseClick,       // Clic simple sur un cours
                this::onCourseLongClick    // Clic long sur un cours
        );

        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCourses.setAdapter(courseAdapter);
    }

    // Observateur LiveData pour mettre à jour la liste des cours en temps réel
    private void setupObservers() {
        viewModel.getAllCourses().observe(this, courses -> {
            if (courses != null) {
                courseAdapter.setCourses(courses);
                updateEmptyState(courses.isEmpty()); // Affiche un message si liste vide
            }
        });
    }

    // Configuration des boutons et filtres
    private void setupClickListeners() {
        // Bouton d'ajout d'un cours
        binding.fabAddCourse.setOnClickListener(v -> {
            if (isAddEditCourseActivityAvailable()) { // Vérifie si l'activité est disponible
                Intent intent = new Intent(MainActivity.this, AddEditCourseActivity.class);
                startActivity(intent);
            } else { // Sinon, montre un message temporaire
                showAddCourseDialog();
            }
        });

        // Configuration des filtres par jour
        setupDayFilters();
    }

    // Vérifie si AddEditCourseActivity est présente dans le projet
    private boolean isAddEditCourseActivityAvailable() {
        try {
            Class.forName("com.example.studentagenda.ui.AddEditCourseActivity");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // Affiche un message temporaire pour l'ajout de cours
    private void showAddCourseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ajouter un cours")
                .setMessage("La fonction d'ajout de cours sera bientôt disponible")
                .setPositiveButton("OK", null)
                .show();
    }

    // Configuration des filtres par jour de la semaine
    private void setupDayFilters() {
        binding.filterAll.setOnClickListener(v -> filterByDay(null));
        binding.filterMonday.setOnClickListener(v -> filterByDay(1));
        binding.filterTuesday.setOnClickListener(v -> filterByDay(2));
        binding.filterWednesday.setOnClickListener(v -> filterByDay(3));
        binding.filterThursday.setOnClickListener(v -> filterByDay(4));
        binding.filterFriday.setOnClickListener(v -> filterByDay(5));
        binding.filterSaturday.setOnClickListener(v -> filterByDay(6));
        binding.filterSunday.setOnClickListener(v -> filterByDay(7));
    }

    // Configuration de la barre de recherche
    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (!query.isEmpty()) {
                    performSearch(query);
                } else {
                    if (currentDayFilter != null) {
                        filterByDay(currentDayFilter);
                    } else {
                        loadAllCourses();
                    }
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Bouton pour effacer la recherche
        binding.btnClearSearch.setOnClickListener(v -> binding.etSearch.setText(""));
    }

    // Clic simple sur un cours : ouvre CourseDetailActivity
    private void onCourseClick(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }

    // Clic long sur un cours : affiche un dialogue d'actions
    private boolean onCourseLongClick(Course course) {
        showCourseActionsDialog(course);
        return true;
    }

    // Chargement de tous les cours
    private void loadAllCourses() {
        viewModel.getAllCourses().observe(this, courses -> {
            courseAdapter.setCourses(courses);
            updateEmptyState(courses.isEmpty());
        });
    }

    // Filtrage des cours par jour
    private void filterByDay(Integer day) {
        currentDayFilter = day;
        if (day == null) {
            loadAllCourses();
        } else {
            viewModel.getCoursesByDay(day).observe(this, courses -> {
                courseAdapter.setCourses(courses);
                updateEmptyState(courses.isEmpty());
            });
        }
        updateDayFilterUI(day);
    }

    // Recherche des cours par nom ou professeur
    private void performSearch(String query) {
        viewModel.searchCourses(query).observe(this, courses -> {
            courseAdapter.setCourses(courses);
            updateEmptyState(courses.isEmpty());
        });
    }

    // Met à jour l'état des boutons de filtre
    private void updateDayFilterUI(Integer selectedDay) {
        List<MaterialButton> filterButtons = new ArrayList<>();
        filterButtons.add(binding.filterAll);
        filterButtons.add(binding.filterMonday);
        filterButtons.add(binding.filterTuesday);
        filterButtons.add(binding.filterWednesday);
        filterButtons.add(binding.filterThursday);
        filterButtons.add(binding.filterFriday);
        filterButtons.add(binding.filterSaturday);
        filterButtons.add(binding.filterSunday);

        for (MaterialButton button : filterButtons) {
            button.setSelected(false); // Réinitialise tous les boutons
        }

        // Active le bouton correspondant au jour sélectionné
        if (selectedDay == null) {
            binding.filterAll.setSelected(true);
        } else {
            switch (selectedDay) {
                case 1: binding.filterMonday.setSelected(true); break;
                case 2: binding.filterTuesday.setSelected(true); break;
                case 3: binding.filterWednesday.setSelected(true); break;
                case 4: binding.filterThursday.setSelected(true); break;
                case 5: binding.filterFriday.setSelected(true); break;
                case 6: binding.filterSaturday.setSelected(true); break;
                case 7: binding.filterSunday.setSelected(true); break;
            }
        }
    }

    // Affiche ou cache le message "aucun cours"
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewCourses.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerViewCourses.setVisibility(View.VISIBLE);
        }
    }

    // Dialogue pour modifier ou supprimer un cours
    private void showCourseActionsDialog(Course course) {
        String[] actions = {"Modifier", "Supprimer"};

        new AlertDialog.Builder(this)
                .setTitle(course.getTitle()) // Titre du dialogue = nom du cours
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Modifier
                            if (isAddEditCourseActivityAvailable()) {
                                Intent intent = new Intent(this, AddEditCourseActivity.class);
                                intent.putExtra("COURSE_ID", course.getId());
                                startActivity(intent);
                            } else {
                                showEditNotAvailableDialog();
                            }
                            break;
                        case 1: // Supprimer
                            showDeleteConfirmationDialog(course);
                            break;
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    private void showDeleteConfirmationDialog(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le cours")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + course.getTitle() + "\" ?\n\n" +
                        "⚠️ Tous les rappels planifiés seront également annulés.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Annuler le rappel planifié avant de supprimer le cours
                    NotificationHelper helper = new NotificationHelper(this);
                    helper.cancelScheduledReminder(course.getId());

                    // Supprimer le cours
                    viewModel.delete(course);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    private void showEditNotAvailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Modification")
                .setMessage("La fonction de modification sera bientôt disponible")
                .setPositiveButton("OK", null)
                .show();
    }

    // Création du menu dans la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Gestion des actions du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        } else if (id == R.id.action_clear_all) {
            showClearAllConfirmationDialog();
            return true;
        }
        // AJOUTEZ CE CAS
        else if (id == R.id.action_test_notification) {
            testNotification();
            return true;
        }

        else if (id == R.id.action_test_scheduled) {
            testScheduledNotification();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Paramètres")
                .setMessage("Les paramètres seront bientôt disponibles")
                .setPositiveButton("OK", null)
                .show();
    }


    private void showClearAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer tous les cours")
                .setMessage("Êtes-vous sûr de vouloir supprimer tous les cours ?\n\n" +
                        "⚠️ Cette action est irréversible.\n" +
                        "⚠️ Tous les rappels planifiés seront annulés.")
                .setPositiveButton("Supprimer tout", (dialog, which) -> {
                    // Annuler tous les rappels planifiés
                    viewModel.getAllCourses().observe(this, courses -> {
                        if (courses != null && !courses.isEmpty()) {
                            NotificationHelper helper = new NotificationHelper(this);
                            for (Course course : courses) {
                                helper.cancelScheduledReminder(course.getId());
                            }
                        }
                    });

                    // Supprimer tous les cours
                    viewModel.deleteAllCourses();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Replanifier les rappels quand l'app revient au premier plan
        // (Utile après un redémarrage du système)
        scheduleAllCourseReminders();
    }

}