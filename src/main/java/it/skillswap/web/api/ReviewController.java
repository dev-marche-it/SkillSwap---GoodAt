package it.skillswap.web.api;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Review;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.ReviewDto;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ApplicationState app;

    public ReviewController(ApplicationState app) {
        this.app = app;
    }

    @PostMapping
    public ReviewDto create(@RequestBody Map<String, Object> body) {
        String exchangeId = string(body, "exchangeId");
        String reviewerStudentId = stringAny(body, "reviewerStudentId", "studentId");
        int stars = number(body, "stars");
        String comment = body.getOrDefault("comment", "").toString();
        String reviewId = body.containsKey("reviewId") && body.get("reviewId") != null
                ? body.get("reviewId").toString().trim()
                : IdGenerator.nextReviewId(app.getState());
        if (reviewId.isEmpty()) {
            reviewId = IdGenerator.nextReviewId(app.getState());
        }
        Review review = app.getReviewService().addReview(reviewId, exchangeId, reviewerStudentId, stars, comment);
        app.persist();
        return ReviewDto.from(review);
    }

    private static String string(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null || v.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Campo obbligatorio: " + key);
        }
        return v.toString().trim();
    }

    private static int number(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v != null) {
            try {
                return Integer.parseInt(v.toString().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Voto non valido: usa un numero da 1 a 5");
            }
        }
        throw new IllegalArgumentException("Campo obbligatorio: " + key);
    }

    private static String stringAny(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object v = body.get(key);
            if (v != null && !v.toString().trim().isEmpty()) {
                return v.toString().trim();
            }
        }
        throw new IllegalArgumentException("Campo obbligatorio: " + keys[0]);
    }
}
