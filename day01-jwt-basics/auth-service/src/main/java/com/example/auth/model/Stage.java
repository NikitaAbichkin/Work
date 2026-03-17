package com.example.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
public class Stage {
    public enum PriorityStage {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum StatusPriority {
        IN_PROGRESS,
        COMPLETED,
        FROZEN,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "starts_at")
    private LocalDate startsAt;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private PriorityStage priority = PriorityStage.MEDIUM;

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPriority status = StatusPriority.IN_PROGRESS;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "result_text")
    private String resultText;

    @ElementCollection
    @CollectionTable(name = "stage_result_images", joinColumns = @JoinColumn(name = "stage_id"))
    @Column(name = "images")
    private List<String> resultImages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public List<String> getResultImages() {
        return resultImages;
    }

    public void setResultImages(List<String> resultImages) {
        this.resultImages = resultImages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
    }

    public LocalDate getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDate startsAt) {
        this.startsAt = startsAt;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public PriorityStage getPriority() {
        return priority;
    }

    public void setPriority(PriorityStage priority) {
        this.priority = priority;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress == null ? 0 : progress;
        this.isCompleted = (this.progress == 100);
        if (this.isCompleted && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
            this.status = StatusPriority.COMPLETED;
        } else if (!isCompleted) {
            this.completedAt = null;
            if (this.status == StatusPriority.COMPLETED) {
                this.status = StatusPriority.IN_PROGRESS;
            }
        }
    }

    public StatusPriority getStatus() {
        return status;
    }

    /** Устанавливает статус. Допустимые значения: IN_PROGRESS, FROZEN, COMPLETED. */
    public void setStatus(StatusPriority status) {
        if (status == null) {
            return;
        }
        this.status = status;
        if (status == StatusPriority.COMPLETED) {
            this.isCompleted = true;
            this.progress = 100;
            if (this.completedAt == null) {
                this.completedAt = LocalDateTime.now();
            }
        } else {
            this.isCompleted = false;
            this.completedAt = null;
        }
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt(){
        return  updatedAt;
    }
    public  void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

}

