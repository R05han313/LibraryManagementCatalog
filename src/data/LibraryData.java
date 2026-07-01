package data;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import models.Book;
import models.Loan;
import models.Member;

/* Central in-memory data store for the library, with binary serialization
   persistence to disk so data survives between application runs.
 */
public class LibraryData implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "library_catalog.dat";
    private static final String BACKUP_FILE = "library_catalog.bak";

    private List<Book> books = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();
    private List<String> activityLog = new ArrayList<>();

    //Persistence

    public static LibraryData loadOrCreate() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                LibraryData data = (LibraryData) in.readObject();
                data.ensureNonNullCollections();
                return data;
            } catch (Exception e) {
                // Try backup before giving up
                File backup = new File(BACKUP_FILE);
                if (backup.exists()) {
                    try (ObjectInputStream in = new ObjectInputStream(
                            new BufferedInputStream(new FileInputStream(backup)))) {
                        LibraryData data = (LibraryData) in.readObject();
                        data.ensureNonNullCollections();
                        return data;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        LibraryData fresh = new LibraryData();
        fresh.seedSampleData();
        return fresh;
    }

    private void ensureNonNullCollections() {
        if (books == null)
            books = new ArrayList<>();
        if (members == null)
            members = new ArrayList<>();
        if (loans == null)
            loans = new ArrayList<>();
        if (activityLog == null)
            activityLog = new ArrayList<>();
    }

    //Saves to disk atomically: writes a temp file, backs up the old file, then swaps.

    public synchronized void save() {
        try {
            File current = new File(DATA_FILE);
            File backup = new File(BACKUP_FILE);
            File temp = new File(DATA_FILE + ".tmp");

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(temp)))) {
                out.writeObject(this);
            }

            if (current.exists()) {
                Files.copy(current.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temp.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save library data: " + e.getMessage());
        }
    }

    //Sample Data
    private void seedSampleData() {
        Book b1 = new Book("The Pragmatic Programmer", "Andrew Hunt", "9780135957059", "Technology",
                "Addison-Wesley", 2019, 3, 45.99, "T-101", "A guide to becoming a better, more pragmatic developer.");
        b1.setRating(4.7);
        Book b2 = new Book("Clean Code", "Robert C. Martin", "9780132350884", "Technology",
                "Prentice Hall", 2008, 2, 39.50, "T-102", "A handbook of agile software craftsmanship.");
        b2.setRating(4.5);
        Book b3 = new Book("Atomic Habits", "James Clear", "9780735211292", "Self-Help",
                "Avery", 2018, 4, 22.00, "S-201", "Tiny changes, remarkable results.");
        b3.setRating(4.8);
        Book b4 = new Book("Dune", "Frank Herbert", "9780441013593", "Science Fiction",
                "Ace Books", 1965, 2, 18.75, "F-301",
                "Epic tale of politics, religion, and survival on a desert planet.");
        b4.setRating(4.9);
        Book b5 = new Book("Sapiens", "Yuval Noah Harari", "9780062316097", "History",
                "Harper", 2015, 3, 28.00, "H-401", "A brief history of humankind.");
        b5.setRating(4.6);
        books.addAll(List.of(b1, b2, b3, b4, b5));

        Member m1 = new Member("Roshan Kumar", "roshan@example.com", "9876543210", Member.MembershipType.STUDENT);
        Member m2 = new Member("Anita Sharma", "anita@example.com", "9123456780", Member.MembershipType.FACULTY);
        members.addAll(List.of(m1, m2));

        logActivity("System initialized with sample catalog data.");
    }

    //Book Operations

    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
        logActivity("Added book: \"" + book.getTitle() + "\" by " + book.getAuthor());
    }

    public boolean removeBook(Book book) {
        boolean hasActiveLoan = loans.stream().anyMatch(l -> l.getBookId().equals(book.getId()) && !l.isReturned());
        if (hasActiveLoan)
            return false;
        books.remove(book);
        logActivity("Removed book: \"" + book.getTitle() + "\"");
        return true;
    }

    public Optional<Book> findBookById(String id) {
        return books.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public Optional<Book> findBookByIsbn(String isbn) {
        return books.stream().filter(b -> b.getIsbn().equalsIgnoreCase(isbn)).findFirst();
    }

    public boolean isDuplicateIsbn(String isbn, String excludeId) {
        return books.stream().anyMatch(b -> b.getIsbn().equalsIgnoreCase(isbn) && !b.getId().equals(excludeId));
    }

    //Member Operations

    public List<Member> getMembers() {
        return members;
    }

    public void addMember(Member member) {
        members.add(member);
        logActivity("Registered new member: " + member.getName());
    }

    public boolean removeMember(Member member) {
        boolean hasActiveLoan = loans.stream().anyMatch(l -> l.getMemberId().equals(member.getId()) && !l.isReturned());
        if (hasActiveLoan)
            return false;
        members.remove(member);
        logActivity("Removed member: " + member.getName());
        return true;
    }

    public Optional<Member> findMemberById(String id) {
        return members.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    //Loan Operations

    public List<Loan> getLoans() {
        return loans;
    }

    public List<Loan> getActiveLoansForMember(Member member) {
        return loans.stream()
                .filter(l -> l.getMemberId().equals(member.getId()) && !l.isReturned())
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansForBook(Book book) {
        return loans.stream()
                .filter(l -> l.getBookId().equals(book.getId()) && !l.isReturned())
                .collect(Collectors.toList());
    }

    public List<Loan> getAllActiveLoans() {
        return loans.stream().filter(l -> !l.isReturned()).collect(Collectors.toList());
    }

    public List<Loan> getOverdueLoans() {
        return loans.stream().filter(l -> !l.isReturned() && l.isOverdue()).collect(Collectors.toList());
    }

    
    //Attempts to issue a book to a member, returns the result message and outcome.
    
    public IssueResult issueBook(Book book, Member member) {
        if (!member.isActive()) {
            return new IssueResult(false, "This member's account is inactive.");
        }
        if (member.getOutstandingFine() > 0) {
            return new IssueResult(false, String.format(
                    "Member has an outstanding fine of %.2f. Please clear it before issuing new books.",
                    member.getOutstandingFine()));
        }
        if (!book.isAvailable()) {
            return new IssueResult(false, "No copies of this book are currently available.");
        }
        int activeCount = getActiveLoansForMember(member).size();
        if (activeCount >= member.getBorrowLimit()) {
            return new IssueResult(false,
                    "Member has reached their borrowing limit of " + member.getBorrowLimit() + " books.");
        }
        boolean alreadyHasThisBook = getActiveLoansForMember(member).stream()
                .anyMatch(l -> l.getBookId().equals(book.getId()));
        if (alreadyHasThisBook) {
            return new IssueResult(false, "Member already has an active loan for this title.");
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(member.getLoanPeriodDays());
        Loan loan = new Loan(book.getId(), member.getId(), book.getTitle(), member.getName(), issueDate, dueDate);
        loans.add(loan);
        book.borrowOneCopy();
        logActivity("Issued \"" + book.getTitle() + "\" to " + member.getName() + " (due " + dueDate + ")");
        return new IssueResult(true, "Book issued successfully. Due on " + dueDate + ".");
    }

    //Processes the return of a loan, applying any overdue fine to the member.
    public ReturnResult returnBook(Loan loan) {
        if (loan.isReturned()) {
            return new ReturnResult(false, "This loan has already been marked as returned.", 0);
        }
        double fine = loan.markReturned();
        findBookById(loan.getBookId()).ifPresent(Book::returnOneCopy);
        if (fine > 0) {
            findMemberById(loan.getMemberId()).ifPresent(m -> m.addFine(fine));
        }
        logActivity("Returned \"" + loan.getBookTitleSnapshot() + "\" from " + loan.getMemberNameSnapshot()
                + (fine > 0 ? String.format(" (fine: %.2f)", fine) : ""));
        return new ReturnResult(true, fine > 0
                ? String.format("Book returned. Overdue fine of %.2f applied to member account.", fine)
                : "Book returned on time. No fine applied.", fine);
    }

    public record IssueResult(boolean success, String message) {
    }

    public record ReturnResult(boolean success, String message, double fine) {
    }

    //Activity Log

    public List<String> getActivityLog() {
        return activityLog;
    }

    public void logActivity(String entry) {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        activityLog.add("[" + timestamp + "] " + entry);
        // Keep log from growing unbounded
        if (activityLog.size() > 2000) {
            activityLog.remove(0);
        }
    }

    //Statistics

    public int getTotalBookTitles() {
        return books.size();
    }

    public int getTotalCopies() {
        return books.stream().mapToInt(Book::getTotalCopies).sum();
    }

    public int getTotalAvailableCopies() {
        return books.stream().mapToInt(Book::getAvailableCopies).sum();
    }

    public int getTotalBorrowedCopies() {
        return getTotalCopies() - getTotalAvailableCopies();
    }

    public double getTotalCatalogValue() {
        return books.stream().mapToDouble(b -> b.getPrice() * b.getTotalCopies()).sum();
    }

    public Map<String, Long> getGenreDistribution() {
        return books.stream().collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
    }

    public List<Book> getMostPopularBooks(int limit) {
        Map<String, Long> borrowCounts = loans.stream()
                .collect(Collectors.groupingBy(Loan::getBookId, Collectors.counting()));
        return books.stream()
                .sorted((a, b) -> Long.compare(
                        borrowCounts.getOrDefault(b.getId(), 0L),
                        borrowCounts.getOrDefault(a.getId(), 0L)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long getBorrowCountForBook(Book book) {
        return loans.stream().filter(l -> l.getBookId().equals(book.getId())).count();
    }

    public double getTotalFinesCollectable() {
        return members.stream().mapToDouble(Member::getOutstandingFine).sum();
    }
}