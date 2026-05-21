package it.skillswap.web.api.dto;

import it.skillswap.domain.Review;

public record ReviewDto(
        String reviewId,
        String exchangeId,
        String reviewerStudentId,
        String reviewerName,
        String revieweeStudentId,
        String revieweeName,
        int stars,
        String comment,
        String createdAt) {

    public static ReviewDto from(Review r) {
        return new ReviewDto(
                r.getReviewId(),
                r.getExchange().getExchangeId(),
                r.getReviewer().getStudentId(),
                r.getReviewer().getName(),
                r.getReviewee().getStudentId(),
                r.getReviewee().getName(),
                r.getStars(),
                r.getComment(),
                r.getCreatedAt().toString());
    }
}
