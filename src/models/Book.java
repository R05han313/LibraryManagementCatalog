package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single book record in the library catalog.
 * Implements Serializable for persistence to disk.
 */
public class Book implements Serializable, Comparable<Book> {
    private static final long serialVersionUID = 1L;

    public enum Status {
        AVAILABLE, BORROWED, RESERVED, LOST, DAMAGED
    }

    private final String id; // immutable unique identifier
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String publisher;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;
    private double price;
    private String shelfLocation;
    private String description;
    private double rating; // 0.0 - 5.0
    private Status status;

    // Borrowing-related fields (for the currently active loan, simple model:
    // per-copy tracking handled by Loan objects)
    private LocalDate dateAdded;

    public Book(String title, String author, String isbn, String genre, String publisher,
            int publicationYear, int totalCopies, double price, String shelfLocation, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.price = price;
        this.shelfLocation = shelfLocation;
        this.description = description == null ? "" : description;
        this.rating = 0.0;
        this.status = totalCopies > 0 ? Status.AVAILABLE : Status.BORROWED;
        this.dateAdded = LocalDate.now();
    }

    // ---- Getters & Setters ----
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0, Math.min(5, rating));
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void borrowOneCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            if (availableCopies == 0)
                status = Status.BORROWED;
        }
    }

    public void returnOneCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
        if (availableCopies > 0 && status == Status.BORROWED) {
            status = Status.AVAILABLE;
        }
    }

    @Override
    public int compareTo(Book other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Book))
            return false;
        return id.equals(((Book) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}