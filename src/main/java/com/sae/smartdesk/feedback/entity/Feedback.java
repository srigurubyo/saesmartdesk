package com.sae.smartdesk.feedback.entity;

import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.request.entity.Request;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "given_by_user_id", nullable = false)
    private User givenBy;

    @Column(name = "given_by_role", nullable = false, length = 40)
    private String givenByRole;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comments", length = 4000)
    private String comments;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public User getGivenBy() {
        return givenBy;
    }

    public void setGivenBy(User givenBy) {
        this.givenBy = givenBy;
    }

    public String getGivenByRole() {
        return givenByRole;
    }

    public void setGivenByRole(String givenByRole) {
        this.givenByRole = givenByRole;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
