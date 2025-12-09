 package com.example.studentagenda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentagenda.R;
import com.example.studentagenda.data.entity.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>(); // Liste des cours affichés
    private final OnItemClickListener onItemClickListener; // Listener clic simple
    private final OnItemLongClickListener onItemLongClickListener; // Listener clic long

    // Interface pour gérer le clic sur un item
    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    // Interface pour gérer le clic long
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Course course);
    }

    // Constructeur avec les deux listeners
    public CourseAdapter(OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.onItemClickListener = clickListener;
        this.onItemLongClickListener = longClickListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate du layout de chaque item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position); // Récupération du cours actuel
        holder.bind(course); // Remplissage des vues

        // Gestion du clic simple
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(course);
            }
        });

        // Gestion du clic long
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(course);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return courses.size(); // Nombre total d’items
    }

    // Mettre à jour la liste des cours
    public void setCourses(List<Course> courses) {
        this.courses = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged(); // Rafraîchir la liste
    }

    // ViewHolder représentant un item du RecyclerView
    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvProfessor, tvTime, tvRoom, tvType, tvDay;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourseName);
            tvProfessor = itemView.findViewById(R.id.tvProfessor);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvType = itemView.findViewById(R.id.tvType);
            tvDay = itemView.findViewById(R.id.tvDay);
        }

        // Lier les données du cours aux TextView
        public void bind(Course course) {
            tvName.setText(course.getName());
            tvProfessor.setText(course.getProfessor());
            tvTime.setText(course.getStartTime() + " - " + course.getEndTime());
            tvRoom.setText(course.getRoom());
            tvType.setText(course.getType().name());
            tvDay.setText(course.getDayName());

            // Couleur selon le type de cours
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
            tvType.setBackgroundResource(typeColor);
        }
    }
}