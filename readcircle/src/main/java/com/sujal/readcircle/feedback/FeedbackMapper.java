package com.sujal.readcircle.feedback;

import com.sujal.readcircle.book.Book;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeedbackMapper {
    public Feedback toFeedback(FeedbackRequest request) {

        return Feedback.builder()
                .note(request.note())
                .comment((request.comment())
                )
                .book(Book.builder()
                                .id(request.bookId())
                                .archived(false)
                                .shareable(false)
                                .build()
                        )
                        .build();

    }

    public FeedbackResponse toFeedbackResponse(Feedback f, Integer id) {
        return FeedbackResponse.builder()
                .note(f.getNote())
                .comment(f.getComment())
                .ownFeedback(Objects.equals(f.getCreatedBy(), id))
                .build();
    }
}
