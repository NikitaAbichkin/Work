package com.example.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) //не подтягивай Goal пока тебя не попопросят
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "estimated_time")
    private String estimatedTime;

    @Column(name = "priority")
    private String priority;

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    /** Статус этапа: IN_PROGRESS, FROZEN, COMPLETED */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @Column(name = "result_text", nullable = true)
    private   String resultText;

    @Column (name = "images", nullable = true)
    @ElementCollection
    @CollectionTable(name = "stage_result_images", joinColumns =  @JoinColumn(name =  "stage_id"))
    // у кождой задачи будут свои фотки ( отдельная таблица всех изображений которые буду связаны через поле

    private  List<String> resultImages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public  String getResultText(){
        return  resultText;
    }
    public void setResultText(String resultText){
        this.resultText = resultText;
    }

    public  List<String> getResultImages(){
        return  resultImages;
    }
    public  void setResultImages(List <String> resultImages){
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

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getProgress() {
        return progress;
    }
    public  void setProgress( Integer progress){
        this.progress = progress == null ? 0 : progress;
        this.isCompleted = (this.progress == 100);
        if (this.isCompleted && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
            this.status = "COMPLETED";
        } else if (!isCompleted) {
            this.completedAt = null;
            if ("COMPLETED".equals(this.status)) {
                this.status = "IN_PROGRESS";
            }
        }
    }

    public String getStatus() {
        return status;
    }

    /** Устанавливает статус. Допустимые значения: IN_PROGRESS, FROZEN, COMPLETED. */
    public void setStatus(String status) {
        if (status == null || (!status.equals("IN_PROGRESS") && !status.equals("FROZEN") && !status.equals("COMPLETED"))) {
            return;
        }
        this.status = status;
        if ("COMPLETED".equals(status)) {
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
}
