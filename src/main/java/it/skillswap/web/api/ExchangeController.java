package it.skillswap.web.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Exchange;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.ExchangeDto;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ApplicationState app;

    public ExchangeController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<ExchangeDto> list(@RequestParam(required = false) String studentId) {
        return app.getState().getExchanges().stream()
                .filter(e -> studentId == null || involvesStudent(e, studentId))
                .map(ExchangeDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeDto> get(@PathVariable String id) {
        return findExchange(id)
                .map(ExchangeDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ExchangeDto propose(@RequestBody Map<String, String> body) {
        String offerId = required(body, "offerId");
        String requestId = required(body, "requestId");
        String exchangeId = body.containsKey("exchangeId") && !body.get("exchangeId").isBlank()
                ? body.get("exchangeId").trim()
                : IdGenerator.nextExchangeId(app.getState());
        Exchange exchange = app.getExchangeService().propose(exchangeId, offerId, requestId);
        app.persist();
        return ExchangeDto.from(exchange);
    }

    @PutMapping("/{id}/accept")
    public ExchangeDto accept(@PathVariable String id) {
        Exchange exchange = app.getExchangeService().accept(id);
        app.persist();
        return ExchangeDto.from(exchange);
    }

    @PutMapping("/{id}/complete")
    public ExchangeDto complete(@PathVariable String id) {
        Exchange exchange = app.getExchangeService().complete(id);
        app.persist();
        return ExchangeDto.from(exchange);
    }

    @PutMapping("/{id}/cancel")
    public ExchangeDto cancel(@PathVariable String id) {
        Exchange exchange = app.getExchangeService().cancel(id);
        app.persist();
        return ExchangeDto.from(exchange);
    }

    private boolean involvesStudent(Exchange e, String studentId) {
        return e.getOffer().getStudent().getStudentId().equals(studentId)
                || e.getRequest().getStudent().getStudentId().equals(studentId);
    }

    private java.util.Optional<Exchange> findExchange(String id) {
        return app.getState().getExchanges().stream()
                .filter(e -> e.getExchangeId().equals(id))
                .findFirst();
    }

    private static String required(Map<String, String> body, String key) {
        String v = body.getOrDefault(key, "").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Campo obbligatorio: " + key);
        }
        return v;
    }
}
