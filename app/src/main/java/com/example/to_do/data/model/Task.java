package com.example.to_do.data.model;

import java.util.Date;
import java.util.Objects;

public class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private Date dueDate;
    private Category category;
    private boolean isCompleted;

    public Task(
            int id,
            String title,
            String description,
            Priority priority,
            Date dueDate,
            Category category,
            boolean isCompleted
        ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.category = category;
        this.isCompleted = isCompleted;
    }
    public Task(
            String title,
            String description,
            Priority priority,
            Date dueDate,
            Category category
    ) {
        this(0, title, description, priority, dueDate, category, false);
    }

    // Getters y setters
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }

    public String getPriorityText() {
        switch (priority) {
            case HIGH:
                return "Alta";
            case MEDIUM:
                return "Media";
            case LOW:
                return "Baja";
            case OTHER:
            default:
                return "Otra";
        }
    }

    public void setPriority(Priority priority) { this.priority = priority; }

    public Date getDueDate() { return dueDate; }

    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Category getCategory() { return category; }

    public void setCategory(Category category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }

    public void setCompleted(boolean completed) { isCompleted = completed; }

    // equals, hashCode, toString opcionales
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id &&
                isCompleted == task.isCompleted &&
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                priority == task.priority &&
                Objects.equals(dueDate, task.dueDate) &&
                category == task.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, priority, dueDate, category, isCompleted);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", category=" + category +
                ", isCompleted=" + isCompleted +
                '}';
    }

}

