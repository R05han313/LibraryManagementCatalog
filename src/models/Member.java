package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a library member who can borrow books.
 */
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MembershipType {
        STUDENT, FACULTY, GENERAL, PREMIUM
    }

    private final String id;
    private String name;
    private String email;
    private String phone;
    private MembershipType type;
    private LocalDate joinDate;
    private double outstandingFine;
    private boolean active;

    public Member(String name, String email, String phone, MembershipType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.joinDate = LocalDate.now();
        this.outstandingFine = 0.0;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MembershipType getType() {
        return type;
    }

    public void setType(MembershipType type) {
        this.type = type;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public double getOutstandingFine() {
        return outstandingFine;
    }

    public void addFine(double amount) {
        this.outstandingFine += amount;
    }

    public void clearFine() {
        this.outstandingFine = 0.0;
    }

    public void payPartial(double amount) {
        this.outstandingFine = Math.max(0, this.outstandingFine - amount);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Max books a member of this type may borrow at once. */
    public int getBorrowLimit() {
        switch (type) {
            case FACULTY:
                return 10;
            case PREMIUM:
                return 8;
            case GENERAL:
                return 5;
            case STUDENT:
            default:
                return 4;
        }
    }

    /** Loan period in days for this member type. */
    public int getLoanPeriodDays() {
        switch (type) {
            case FACULTY:
                return 30;
            case PREMIUM:
                return 21;
            case GENERAL:
                return 14;
            case STUDENT:
            default:
                return 14;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Member))
            return false;
        return id.equals(((Member) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}