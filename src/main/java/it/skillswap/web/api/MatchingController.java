package it.skillswap.web.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.MatchResultDto;

@RestController
@RequestMapping("/api/students/{id}/matches")
public class MatchingController {

    private final ApplicationState app;

    public MatchingController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping("/one-way")
    public List<MatchResultDto> oneWay(@PathVariable String id) {
        return app.getMatchingService().findOneWayMatches(id).stream()
                .map(MatchResultDto::from)
                .toList();
    }

    @GetMapping("/swap")
    public List<MatchResultDto> swap(@PathVariable String id) {
        return app.getMatchingService().findSwapMatches(id).stream()
                .map(MatchResultDto::from)
                .toList();
    }
}
