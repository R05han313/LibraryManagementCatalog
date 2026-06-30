package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents a single borrowing transaction linking a Book copy to a Member.
 */
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double FINE_PER_DAY = 5.0; // currency units per day overdue

    private final String id;
    private final String bookId;
    private final String memberId;
    private final String bookTitleSnapshot; // snapshot for display even if book is later removed
    private final String memberNameSnapshot;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; // null while still borrowed
    private double fineCharged;
    private LocalDate currentDueDate; // mutable due date, supports renewals
    private int renewalCount;

    public Loan(String bookId, String memberId, String bookTitleSnapshot, String memberNameSnapshot,
            LocalDate issueDate, LocalDate dueDate) {
        this.id = UUID.randomUUID().toString();
        this.bookId = bookId;
        this.memberId = memberId;
        this.bookTitleSnapshot = bookTitleSnapshot;
        this.memberNameSnapshot = memberNameSnapshot;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.fineCharged = 0.0;
        this.currentDueDate = dueDate;
        this.renewalCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getBookTitleSnapshot() {
        return bookTitleSnapshot;
    }

    public String getMemberNameSnapshot() {
        return memberNameSnapshot;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return currentDueDate;
    }

    public LocalDate getOriginalDueDate() {
        return dueDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public double getFineCharged() {
        return fineCharged;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        if (isReturned())
            return returnDate.isAfter(currentDueDate);
        return LocalDate.now().isAfter(currentDueDate);
    }

    public long daysOverdue() {
        LocalDate referenceDate = isReturned() ? returnDate : LocalDate.now();
        long days = ChronoUnit.DAYS.between(currentDueDate, referenceDate);
        return Math.max(0, days);
    }

    /**
     * Extends the due date by the given number of days. Only allowed for active,
     * non-overdue loans, max 2 renewals.
     */
    public RenewResult renew(int days, int maxRenewals) {
        if (isReturned())
            return new RenewResult(false, "This loan has already been returned.");
        if (isOverdue())
            return new RenewResult(false,
                    "Overdue loans cannot be renewed. Please return the book and pay any fine first.");
        if (renewalCount >= maxRenewals)
            return new RenewResult(false, "Maximum number of renewals (" + maxRenewals + ") reached for this loan.");
        currentDueDate = currentDueDate.plusDays(days);
        renewalCount++;
        return new RenewResult(true, "Loan renewed. New due date: " + currentDueDate);
    }

    public record RenewResult(boolean success, String message) {
    }

    public double calculateFine() {
        return daysOverdue() * FINE_PER_DAY;
    }

    /** Marks the loan as returned today and computes/locks in the fine. */
    public double markReturned() {
        this.returnDate = LocalDate.now();
        this.fineCharged = calculateFine();
        return fineCharged;
    }

    public String getStatusLabel() {
        if (isReturned())
            return "Returned";
        if (isOverdue())
            return "Overdue";
        return "Active";
    }
}