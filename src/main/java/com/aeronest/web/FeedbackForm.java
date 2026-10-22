package com.aeronest.web;

import com.aeronest.domain.DomainEnums.FeedbackCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FeedbackForm {
    @NotNull private FeedbackCategory category;
    @Min(1) @Max(5) private int rating = 5;
    @NotBlank @Size(min = 4, max = 120) private String subject;
    @NotBlank @Size(min = 10, max = 1200) private String comments;
    @Size(max = 8) private String bookingReference;

    public FeedbackCategory getCategory() { return category; }
    public void setCategory(FeedbackCategory category) { this.category = category; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
}
