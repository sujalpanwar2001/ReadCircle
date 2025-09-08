package com.sujal.readcircle.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;



public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {


//    @Query("""
//    SELECT book
//    FROM Book book
//    WHERE book.archived = false
//      AND book.shareable = true
//      AND book.createdBy != :userId
//      AND NOT EXISTS (
//          SELECT 1
//          FROM BookTransactionHistory history
//          WHERE history.book = book
//          AND history.returnApproved = false
//      )
//    """)
//    Page<Book> findAllDisplayableBooks(Pageable pageable, String userId);

    @Query("""
    SELECT book
    FROM Book book
    WHERE book.archived = false 
      AND book.shareable = true
      AND book.createdBy != :userEmail
      AND NOT EXISTS (
          SELECT 1
          FROM BookTransactionHistory history
          WHERE history.book = book
          AND history.returnApproved = false
      )
    """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, String userEmail);

    @Query("""
    SELECT b
    FROM Book b
    WHERE b.createdBy = :email
    ORDER BY b.createdDate DESC
    """)
    Page<Book> findAllByOwnerEmail(String email, Pageable pageable);



}
