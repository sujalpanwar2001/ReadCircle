package com.sujal.readcircle.book;

import com.sujal.readcircle.exception.OperationNotPermittedException;
import com.sujal.readcircle.common.PageResponse;
import com.sujal.readcircle.file.FileStorageService;
import com.sujal.readcircle.history.BookTransactionHistory;
import com.sujal.readcircle.history.BookTransationHistoryRepository;
import com.sujal.readcircle.notification.Notification;
import com.sujal.readcircle.notification.NotificationService;
import com.sujal.readcircle.notification.NotificationStatus;
import com.sujal.readcircle.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.sujal.readcircle.book.BookSpecification.withOwnerId;
import static com.sujal.readcircle.notification.NotificationStatus.*;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransationHistoryRepository bookTransationHistoryRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public Integer save(BookRequest request, Authentication connectedUser) {

//        User user = ((User)connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
//        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID: " + bookId));
    }


    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
//         User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, connectedUser.getName());
        System.out.println("displayable books" + books.getContent());
        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        System.out.println(booksResponse);
        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
//        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(withOwnerId(connectedUser.getName()), pageable);

        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }


    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
//        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransationHistoryRepository.findAllBorrowedBooks(pageable,connectedUser.getName());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );

    }


    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
//        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransationHistoryRepository.findAllReturnedBooks(pageable,connectedUser.getName());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
//        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;

    }



    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
//        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot update others books archived status");
        }
        book.setArchived(!book.isArchived());
        System.out.println(book);
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book can't be borrowed because it's either archived or not shareable");
        }

        if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }

        // check if THIS user already borrowed it
        if (bookTransationHistoryRepository.isAlreadyBorrowedByUser(bookId, connectedUser.getName())) {
            throw new OperationNotPermittedException("You already borrowed this book and it is still not returned or the return is not approved by the owner");
        }

        // check if ANYONE has borrowed it
        if (bookTransationHistoryRepository.isAlreadyBorrowed(bookId)) {
            throw new OperationNotPermittedException("The requested book is already borrowed by another user");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .userId(connectedUser.getName())
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();

        notificationService.sendNotification(
                book.getCreatedBy(),
                Notification.builder()
                        .status(BORROWED)
                        .message("Your book has been borrowed")
                        .bookTitle(book.getTitle())
                        .build()
        );

        return bookTransationHistoryRepository.save(bookTransactionHistory).getId();
    }


//    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
////        User user = ((User) connectedUser.getPrincipal());
//        if(book.isArchived() || !book.isShareable()){
//            throw new OperationNotPermittedException("The requested book cant be borrowed bcz its either archived or no shareable");
//
//        }
//        if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
//            throw new OperationNotPermittedException("You cannot borrow your own book");
//        }
//        final boolean isAlreadyBorrowed = bookTransationHistoryRepository.isAlreadyBorrowedByUser(bookId, connectedUser.getName());
//        if(isAlreadyBorrowed){
//            throw new OperationNotPermittedException("The requested book is already borrowed by you");
//        }
//
//        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
//                .userId(connectedUser.getName())
//                .book(book)
//                .returned(false)
//                .returnApproved(false)
//                .build();
//        return bookTransationHistoryRepository.save(bookTransactionHistory).getId();
//
//    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cant be borrowed bcz its either archived or no shareable");

        }

//        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory = bookTransationHistoryRepository.findByBookIdAndUserId(bookId, connectedUser.getName())
                .orElseThrow(()-> new OperationNotPermittedException("You didnt borrow this book"));
        bookTransactionHistory.setReturned(true);
        var saved =  bookTransationHistoryRepository.save(bookTransactionHistory);
        notificationService.sendNotification(
                book.getCreatedBy(),
                Notification.builder()
                        .status(RETURNED)
                        .message("Your book has been returned")
                        .bookTitle(book.getTitle())
                        .build()
        );
        return saved.getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cant be approved for return  bcz its either archived or no shareable");

        }

        // User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot approve the return of a book you do not own");
        }


        BookTransactionHistory bookTransactionHistory = bookTransationHistoryRepository.findByBookIdAndOwnerId(bookId, connectedUser.getName())
                .orElseThrow(()-> new OperationNotPermittedException("The book is not returned yet, so you cant approve its return "));
        bookTransactionHistory.setReturnApproved(true);
        var saved =  bookTransationHistoryRepository.save(bookTransactionHistory);

        notificationService.sendNotification(
                bookTransactionHistory.getCreatedBy(),
                Notification.builder()
                        .status(RETURN_APPROVED)
                        .message("Your book return has been approved by the owner")
                        .bookTitle(book.getTitle())
                        .build()
        );
        return saved.getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
//        User user = ((User) connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file,connectedUser.getName());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
