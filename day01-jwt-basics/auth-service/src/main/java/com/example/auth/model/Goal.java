package com.example.auth.model;

import jakarta.persistence.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "goals")
public class Goal {

    public enum GoalStatus {
        IN_PROGRESS,
        COMPLETED,
        FROZEN,
        ARCHIVED
    }

    public enum PriorityStatus {
        LOW,
        MEDIUM,
        HIGH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // не подтягивай юзера пока не попросят
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityStatus priority = PriorityStatus.MEDIUM;

    @Column(name = "start_date", nullable = false)
    private LocalDate startdate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "daily_time_minutes")
    private Integer daily_time_minutes;


    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)// Указываем, что хранить нужно как строку
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "goal", fetch = FetchType.LAZY,  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stage> stages = new ArrayList<>();

    @Column( name  ="progress")
    private Integer progress = 0 ;



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public Integer getProgress(){
        return progress;
    }
    public void setProgress(Integer progress){
        if (progress == null) {
            this.progress = 0;
            return;
        }
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Прогресс цели должен быть в диапазоне 0-100");
        }
        this.progress  = progress;
    }

    public void recalculateProgress() {
        if (stages.isEmpty()) {
            this.progress = 0;
            return;
        }
        Integer total = 0;
        for (Stage stage :stages){
            total += stage.getProgress();
        }
        this.progress = total/stages.size();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
        
    }

    public void setUser(User user) {
        this.user = user;
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

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Stage> getStages() {
        return stages;
    }
    public PriorityStatus getPriority() {
        return priority;
    }
    public void setPriority(PriorityStatus priority) {
        this.priority = priority;
    }
    public LocalDate getStartdate() {
        return startdate;
    }
    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }
        public LocalDate getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    public Integer getDaily_time_minutes() {
        return daily_time_minutes;
    }
    public void setDaily_time_minutes(Integer daily_time_minutes) {
        this.daily_time_minutes = daily_time_minutes;
    }
    public void setStages(List<Stage> stages) {
        this.stages.clear();
        if (stages != null){
            for (Stage stage : stages){
                stage.setGoal(this);
                this.stages.add(stage);
            }
        }
        // когда придет json, чтобы сразу создались обьекты у каждой задачи было сказано чья
        //она


    }

}
