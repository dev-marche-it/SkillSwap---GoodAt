package it.skillswap.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.CommunityDto;
import it.skillswap.web.api.dto.OfferDto;
import it.skillswap.web.api.dto.RequestDto;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final ApplicationState app;

    public CommunityController(ApplicationState app) {
        this.app = app;
    }

    /**
     * Bacheca: offerte attive e richieste di tutti gli studenti, più le proprie.
     */
    @GetMapping
    public CommunityDto community(@RequestParam String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId obbligatorio");
        }

        var offers = app.getState().getOffers().stream().map(OfferDto::from).toList();
        var requests = app.getState().getRequests().stream().map(RequestDto::from).toList();

        return new CommunityDto(
                offers.stream()
                        .filter(o -> o.active() && !o.studentId().equals(studentId))
                        .toList(),
                requests.stream()
                        .filter(r -> !r.studentId().equals(studentId))
                        .toList(),
                offers.stream()
                        .filter(o -> o.studentId().equals(studentId))
                        .toList(),
                requests.stream()
                        .filter(r -> r.studentId().equals(studentId))
                        .toList());
    }
}
