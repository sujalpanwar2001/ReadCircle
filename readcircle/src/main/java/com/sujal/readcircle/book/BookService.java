package com.sujal.readcircle.book;

import com.sujal.readcircle.exception.OperationNotPermittedException;
import com.sujal.readcircle.common.PageResponse;
import com.sujal.readcircle.file.FileStorageService;
import com.sujal.readcircle.history.BookTransactionHistory;
import com.sujal.readcircle.history.BookTransationHistoryRepository;
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

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransationHistoryRepository bookTransationHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {

        User user = ((User)connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID: " + bookId));
    }


    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
         User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
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
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()),pageable);

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
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransationHistoryRepository.findAllBorrowedBooks(pageable,user.getId());
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
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransationHistoryRepository.findAllReturnedBooks(pageable,user.getId());
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
        User user = ((User) connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot update others book's shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;

    }



    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            System.out.println(book.getOwner().getId());
            System.out.println(user.getId());
            throw new OperationNotPermittedException("You cannot update others book's archived status");
        }
        book.setArchived(!book.isArchived());
        System.out.println(book);
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cant be borrowed bcz its either archived or no shareable");

        }
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowed = bookTransationHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if(isAlreadyBorrowed){
            throw new OperationNotPermittedException("The requested book is already borrowed by you");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransationHistoryRepository.save(bookTransactionHistory).getId();

    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cant be borrowed bcz its either archived or no shareable");

        }

        User user = ((User) connectedUser.getPrincipal());
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot return  your own book");
        }

        BookTransactionHistory bookTransactionHistory = bookTransationHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("You didnt borrow this book"));
        bookTransactionHistory.setReturned(true);
        return bookTransationHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cant be approved for return  bcz its either archived or no shareable");

        }

        User user = ((User) connectedUser.getPrincipal());
//        if(Objects.equals(book.getOwner().getId(), user.getId())){
//            throw new OperationNotPermittedException("You cannot return  your own book");
//        }


        BookTransactionHistory bookTransactionHistory = bookTransationHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("The book is not returned yet, so you cant approve its return "));
        bookTransactionHistory.setReturnApproved(true);
        return bookTransationHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
