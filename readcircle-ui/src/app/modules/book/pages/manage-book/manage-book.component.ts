import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { BookRequest } from 'src/app/services/models';
import { BookService } from 'src/app/services/services';

@Component({
  selector: 'app-manage-book',
  templateUrl: './manage-book.component.html',
  styleUrls: ['./manage-book.component.scss']
})


export class ManageBookComponent implements OnInit {
  errorMsg: Array<string> = [];
  bookRequest: BookRequest = {
    authorName: '',
    isbn: '',
    synopsis: '',
    title: ''
  };
  selectedBookCover: File | null = null;   // if user selects new
  selectedPicture: string | undefined;     // for preview
  originalCover: string | null = null;     // keep original if editing

  constructor(
    private bookService: BookService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private toastService: ToastrService
  ) { }

  ngOnInit(): void {
    const bookId = this.activatedRoute.snapshot.params['bookId'];
    if (bookId) {
      this.bookService.findBookById({ 'book-id': bookId }).subscribe({
        next: (book) => {
          this.bookRequest = {
            id: book.id,
            title: book.title as string,
            authorName: book.authorName as string,
            isbn: book.isbn as string,
            synopsis: book.synopsis as string,
            shareable: book.shareable
          };
          if (book.cover) {
            this.selectedPicture = 'data:image/jpg;base64,' + book.cover;
            this.originalCover = book.cover; // store base64 string
          }
        }
      });
    }
  }

  saveBook() {
    this.errorMsg = [];

    // Validation: All fields must be filled
    if (!this.bookRequest.title?.trim()) this.errorMsg.push('Title is required');
    if (!this.bookRequest.authorName?.trim()) this.errorMsg.push('Author name is required');
    if (!this.bookRequest.isbn?.trim()) this.errorMsg.push('ISBN is required');
    if (!this.bookRequest.synopsis?.trim()) this.errorMsg.push('Synopsis is required');

    if (this.errorMsg.length > 0) {
      this.toastService.error(this.errorMsg.join(', '), 'Validation Error');
      return;
    }

    this.bookService.saveBook({ body: this.bookRequest }).subscribe({
      next: (bookId) => {
        if (this.selectedBookCover) {
          // User picked a new file
          this.uploadCover(bookId, this.selectedBookCover);
        } else if (this.originalCover) {
          // Reuse the existing cover (convert base64 to File)
          const file = this.base64ToFile(this.originalCover, "cover.jpg");
          this.uploadCover(bookId, file);
        } else {
          this.handleSuccess();
        }
      },
      error: (err) => {
        console.log(err.error);
        this.toastService.warning('Synopsis cannot be too long', 'Oops!');
        this.errorMsg = err.error.validationErrors;
      }
    });
  }

  private uploadCover(bookId: number, file: File) {
    this.bookService.uploadBookCoverPicture({
      'book-id': bookId,
      body: { file }
    }).subscribe({
      next: () => this.handleSuccess(),
      error: () => this.toastService.warning('Cover upload failed', 'Oops!')
    });
  }

  private handleSuccess() {
    this.toastService.success('Book saved successfully', 'Done');
    this.router.navigate(['/books/my-books']);
  }

  onFileSelected(event: any) {
    this.selectedBookCover = event.target.files[0];
    if (this.selectedBookCover) {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPicture = reader.result as string;
      };
      reader.readAsDataURL(this.selectedBookCover);
    }
  }

  // helper: convert base64 string back to File
  private base64ToFile(base64: string, filename: string): File {
    const byteString = atob(base64);
    const arrayBuffer = new ArrayBuffer(byteString.length);
    const int8Array = new Uint8Array(arrayBuffer);
    for (let i = 0; i < byteString.length; i++) {
      int8Array[i] = byteString.charCodeAt(i);
    }
    const blob = new Blob([int8Array], { type: "image/jpeg" });
    return new File([blob], filename, { type: "image/jpeg" });
  }
}
