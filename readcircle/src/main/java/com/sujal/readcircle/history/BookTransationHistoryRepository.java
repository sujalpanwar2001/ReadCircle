package com.sujal.readcircle.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface BookTransationHistoryRepository extends JpaRepository<BookTransactionHistory,Integer> {

    @Query("""
        SELECT history
        FROM BookTransactionHistory  history
        WHERE history.userId = :email
        """)
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, String  email);

    @Query("""
        SELECT history
        FROM BookTransactionHistory  history
        WHERE history.book.createdBy = :email
        AND history.returned = true
        """)
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, String email);

    @Query("""
    SELECT
    ( COUNT(*) > 0) AS isBorrowed
    FROM BookTransactionHistory bookTransactionHistory
    WHERE bookTransactionHistory.userId = :email
    AND bookTransactionHistory.book.id = :bookId
    AND bookTransactionHistory.returnApproved = false 
""")
    boolean isAlreadyBorrowedByUser(Integer bookId, String email);

    @Query("""
    SELECT transaction
    FROM BookTransactionHistory transaction
    WHERE transaction.userId = :email
    AND transaction.book.id = :bookId
    AND transaction.returned = false
    AND transaction.returnApproved = false 
""")
    Optional<BookTransactionHistory> findByBookIdAndUserId(@Param("bookId") Integer bookId, @Param("email") String email);

    @Query("""
    SELECT transaction
    FROM BookTransactionHistory transaction
    WHERE transaction.book.createdBy = :email
    AND transaction.book.id = :bookId
    AND transaction.returned = true 
    AND transaction.returnApproved = false 
""")
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(@Param("bookId") Integer bookId, @Param("email") String email);



    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory
            WHERE bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowed(@Param("bookId") Integer bookId);
}
