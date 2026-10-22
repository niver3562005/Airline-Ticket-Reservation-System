package com.aeronest.service;

import com.aeronest.domain.CustomerAccount;
import com.aeronest.domain.Feedback;
import com.aeronest.domain.DomainEnums.FeedbackStatus;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.FeedbackRepository;
import com.aeronest.web.FeedbackForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    private final FeedbackRepository feedback;
    private final BookingRepository bookings;

    public FeedbackService(FeedbackRepository feedback, BookingRepository bookings) {
        this.feedback = feedback; this.bookings = bookings;
    }

    @Transactional
    public Feedback submit(CustomerAccount account, FeedbackForm form) {
        Feedback item = new Feedback(); item.setAccount(account); item.setCategory(form.getCategory()); item.setRating(form.getRating());
        item.setSubject(form.getSubject().trim()); item.setComments(form.getComments().trim());
        if (form.getBookingReference() != null && !form.getBookingReference().isBlank()) {
            var booking = bookings.findByPnrIgnoreCase(form.getBookingReference().trim())
                    .orElseThrow(() -> new BusinessRuleException("Booking reference was not found"));
            if (!booking.getContactEmail().equalsIgnoreCase(account.getEmail()))
                throw new BusinessRuleException("That booking does not belong to this account");
            item.setBooking(booking);
        }
        return feedback.save(item);
    }

    public List<Feedback> forAccount(CustomerAccount account) { return feedback.findByAccountIdOrderByCreatedAtDesc(account.getId()); }
    public List<Feedback> all() { return feedback.findAllByOrderByCreatedAtDesc(); }
    public long newCount() { return feedback.countByStatus(FeedbackStatus.NEW); }

    @Transactional
    public void resolve(Long id, String response) {
        Feedback item = feedback.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        if (response == null || response.trim().length() < 5) throw new BusinessRuleException("Add a useful response before resolving feedback");
        item.setResponse(response.trim()); item.setStatus(FeedbackStatus.RESOLVED);
    }
}
