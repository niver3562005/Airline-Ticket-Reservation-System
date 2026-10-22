package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.FeedbackCategory;
import com.aeronest.domain.DomainEnums.FeedbackStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class Feedback extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) private CustomerAccount account;
    @ManyToOne(fetch = FetchType.LAZY) private Booking booking;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private FeedbackCategory category;
    @Column(nullable = false) private int rating;
    @Column(nullable = false, length = 120) private String subject;
    @Column(nullable = false, length = 1200) private String comments;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private FeedbackStatus status = FeedbackStatus.NEW;
    @Column(length = 700) private String response;

    public CustomerAccount getAccount() { return account; }
    public void setAccount(CustomerAccount account) { this.account = account; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public FeedbackCategory getCategory() { return category; }
    public void setCategory(FeedbackCategory category) { this.category = category; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}
