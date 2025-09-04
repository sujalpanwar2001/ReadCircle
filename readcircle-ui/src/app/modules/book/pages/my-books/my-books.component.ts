import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { BookResponse, PageResponseBookResponse } from 'src/app/services/models';
import { BookService } from 'src/app/services/services';

@Component({
  selector: 'app-my-books',
  templateUrl: './my-books.component.html',
  styleUrls: ['./my-books.component.scss']
})
export class MyBooksComponent implements OnInit {

  bookResponse: PageResponseBookResponse = {};
  page = 0;
  size = 8;
  pages: any = [];

  constructor(
    private bookService: BookService,
    private router: Router,
        private toastService: ToastrService

  ) {
  }

  ngOnInit(): void {
    this.findAllBooks();
  }

  private findAllBooks() {
    this.bookService.findAllBooksByOwner({
      page: this.page,
      size: this.size
    })
      .subscribe({
        next: (books) => {
          this.bookResponse = books;
          this.pages = Array(this.bookResponse.totalPages)
            .fill(0)
            .map((x, i) => i);
        }
      });
  }

  gotToPage(page: number) {
    this.page = page;
    this.findAllBooks();
  }

  goToFirstPage() {
    this.page = 0;
    this.findAllBooks();
  }

  goToPreviousPage() {
    this.page --;
    this.findAllBooks();
  }

  goToLastPage() {
    this.page = this.bookResponse.totalPages as number - 1;
    this.findAllBooks();
  }

  goToNextPage() {
    this.page++;
    this.findAllBooks();
  }

  get isLastPage() {
    return this.page === this.bookResponse.totalPages as number - 1;
  }

  // archiveBook(book: BookResponse) {
  //   this.bookService.updateArchivedStatus({
  //     'book-id': book.id as number
  //   }).subscribe({
  //     next: () => {
  //       book.archived = !book.archived;
  //     }
  //   });
  // }

  // shareBook(book: BookResponse) {
  //   this.bookService.updateShareableStatus({
  //     'book-id': book.id as number
  //   }).subscribe({
  //     next: () => {
  //       book.shareable = !book.shareable;
  //     }
  //   });
  // }
   archiveBook(book: BookResponse) {
    this.bookService.updateArchivedStatus({
      'book-id': book.id as number
    }).subscribe({
      next: () => {
        book.archived = !book.archived;
        if (book.archived) {
          this.toastService.warning(`"${book.title}" has been archived`, 'Archived');
        } else {
          this.toastService.info(`"${book.title}" has been unarchived`, 'Restored');
        }
      },
      error: () => this.toastService.error('Could not update archive status', 'Error')
    });
  }

  shareBook(book: BookResponse) {
    this.bookService.updateShareableStatus({
      'book-id': book.id as number
    }).subscribe({
      next: () => {
        book.shareable = !book.shareable;
        if (book.shareable) {
          this.toastService.success(`"${book.title}" is now shared`, 'Shared');
        } else {
          this.toastService.info(`"${book.title}" is no longer shared`, 'Unshared');
        }
      },
      error: () => this.toastService.error('Could not update share status', 'Error')
    });
  }

  editBook(book: BookResponse) {
    this.router.navigate(['books', 'manage', book.id]);
  }
}