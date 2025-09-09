package com.sujal.readcircle.book;

import com.sujal.readcircle.exception.OperationNotPermittedException;
import com.sujal.readcircle.common.PageResponse;
import com.sujal.readcircle.file.FileStorageService;
import com.sujal.readcircle.history.BookTransactionHistory;
import com.sujal.readcircle.history.BookTransationHistoryRepository;
import com.sujal.readcircle.notification.Notification;
import com.sujal.readcircle.notification.NotificationService;
import com.sujal.readcircle.notification.NotificationStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.sujal.readcircle.notification.NotificationStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookTransationHistoryRepository bookTransationHistoryRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getPrincipal()).thenReturn(mock(Jwt.class));
        when(((Jwt) authentication.getPrincipal()).getClaim("email")).thenReturn("user@test.com");
    }

    @Test
    void save_ShouldSaveBookAndReturnId() {
        BookRequest request = new BookRequest(
                1,
                "Some Title",
                "Some Author",
                "1234567890",
                "A test synopsis",
                true
        );

        Book book = new Book();
        book.setId(1);

        when(bookMapper.toBook(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Integer result = bookService.save(request, authentication);

        assertEquals(1, result);
        verify(bookRepository).save(book);
    }


    @Test
    void findById_ShouldReturnBookResponse_WhenBookExists() {
        Book book = new Book();
        BookResponse response = new BookResponse();

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookMapper.toBookResponse(book)).thenReturn(response);

        BookResponse result = bookService.findById(1);

        assertEquals(response, result);
    }

    @Test
    void findById_ShouldThrow_WhenBookNotFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.findById(1));
    }

    @Test
    void findAllBooks_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdDate").descending());
        Book book = new Book();
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAllDisplayableBooks(pageable, "user@test.com")).thenReturn(page);
        when(bookMapper.toBookResponse(book)).thenReturn(new BookResponse());

        PageResponse<BookResponse> result = bookService.findAllBooks(0, 5, authentication);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllBooksByOwner_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdDate").descending());
        Book book = new Book();
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAllByOwnerEmail("user@test.com", pageable)).thenReturn(page);
        when(bookMapper.toBookResponse(book)).thenReturn(new BookResponse());

        PageResponse<BookResponse> result = bookService.findAllBooksByOwner(0, 5, authentication);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void updateShareableStatus_ShouldToggleShareable_WhenOwner() {
        Book book = new Book();
        book.setId(1);
        book.setCreatedBy("user@test.com");
        book.setShareable(false);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Integer result = bookService.updateShareableStatus(1, authentication);

        assertEquals(1, result);
        assertTrue(book.isShareable());
    }

    @Test
    void updateShareableStatus_ShouldThrow_WhenNotOwner() {
        Book book = new Book();
        book.setCreatedBy("other@test.com");

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        assertThrows(OperationNotPermittedException.class,
                () -> bookService.updateShareableStatus(1, authentication));
    }

    @Test
    void borrowBook_ShouldSaveTransaction_WhenAvailable() {
        Book book = new Book();
        book.setId(1);
        book.setCreatedBy("owner@test.com");
        book.setShareable(true);
        book.setArchived(false);

        BookTransactionHistory history = new BookTransactionHistory();
        history.setId(5);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookTransationHistoryRepository.isAlreadyBorrowedByUser(1, "user@test.com")).thenReturn(false);
        when(bookTransationHistoryRepository.isAlreadyBorrowed(1)).thenReturn(false);
        when(bookTransationHistoryRepository.save(any())).thenReturn(history);

        Integer result = bookService.borrowBook(1, authentication);

        assertEquals(5, result);
        verify(notificationService).sendNotification(eq("owner@test.com"), any(Notification.class));
    }

    @Test
    void borrowBook_ShouldThrow_WhenBorrowOwnBook() {
        Book book = new Book();
        book.setId(1);
        book.setCreatedBy("user@test.com");
        book.setShareable(true);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        assertThrows(OperationNotPermittedException.class,
                () -> bookService.borrowBook(1, authentication));
    }

    @Test
    void returnBorrowedBook_ShouldMarkReturned() {
        Book book = new Book();
        book.setId(1);
        book.setCreatedBy("owner@test.com");
        book.setShareable(true);

        BookTransactionHistory history = new BookTransactionHistory();
        history.setId(10);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookTransationHistoryRepository.findByBookIdAndUserId(1, "user@test.com"))
                .thenReturn(Optional.of(history));
        when(bookTransationHistoryRepository.save(history)).thenReturn(history);

        Integer result = bookService.returnBorrowedBook(1, authentication);

        assertEquals(10, result);
        assertTrue(history.isReturned());
        verify(notificationService).sendNotification(eq("owner@test.com"), any(Notification.class));
    }

    @Test
    void approveReturnBorrowedBook_ShouldApproveReturnAndSendNotification() {
        // Arrange
        int bookId = 1;
        String ownerEmail = "owner@example.com";
        String borrowerEmail = "borrower@example.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("email")).thenReturn(ownerEmail);

        Book book = new Book();
        book.setId(bookId);
        book.setCreatedBy(ownerEmail);
        book.setShareable(true);
        book.setArchived(false);

        BookTransactionHistory history = new BookTransactionHistory();
        history.setId(10);
        history.setBook(book);
        history.setReturnApproved(false);
        history.setCreatedBy(borrowerEmail);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookTransationHistoryRepository.findByBookIdAndOwnerId(bookId, ownerEmail))
                .thenReturn(Optional.of(history));
        when(bookTransationHistoryRepository.save(any(BookTransactionHistory.class)))
                .thenAnswer(inv -> {
                    BookTransactionHistory h = inv.getArgument(0);
                    h.setId(10);
                    return h;
                });

        // Act
        Integer result = bookService.approveReturnBorrowedBook(bookId, authentication);

        // Assert
        assertEquals(10, result);
        assertTrue(history.isReturnApproved());

        verify(notificationService).sendNotification(
                eq(borrowerEmail),   // ✅ borrower gets notified
                argThat(notification ->
                        notification.getStatus() == NotificationStatus.RETURN_APPROVED &&
                                notification.getMessage().contains("approved") &&
                                notification.getBookTitle() == null // because in your service code it’s not set
                )
        );
    }


    @Test
    void uploadBookCoverPicture_ShouldSaveFile() {
        Book book = new Book();
        book.setId(1);
        book.setCreatedBy("user@test.com");

        MultipartFile file = mock(MultipartFile.class);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(fileStorageService.saveFile(file, "user@test.com")).thenReturn("cover.png");

        bookService.uploadBookCoverPicture(file, authentication, 1);

        assertEquals("cover.png", book.getBookCover());
        verify(bookRepository).save(book);
    }
}
