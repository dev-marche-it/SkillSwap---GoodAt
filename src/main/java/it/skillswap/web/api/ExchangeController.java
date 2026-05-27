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
    public List<ExchangeDto> list(
            @RequestParam String studentId,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId obbligatorio");
        }
        return app.getState().getExchanges().stream()
                .filter(e -> all || involvesStudent(e, studentId))
                .map(e -> ExchangeDto.from(e, studentId, app.getState()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeDto> get(
            @PathVariable String id,
            @RequestParam String studentId) {
        return findExchange(id)
                .map(e -> ExchangeDto.from(e, studentId, app.getState()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ExchangeDto propose(@RequestBody Map<String, String> body) {
        String offerId = required(body, "offerId");
        String requestId = required(body, "requestId");
        String requesterId = requiredAny(body, "requesterStudentId", "studentId");
        String exchangeId = body.containsKey("exchangeId") && !body.get("exchangeId").isBlank()
                ? body.get("exchangeId").trim()
                : IdGenerator.nextExchangeId(app.getState());

        Exchange exchange = app.getExchangeService().propose(exchangeId, offerId, requestId);
        if (!exchange.getRequest().getStudent().getStudentId().equals(requesterId)) {
            throw new IllegalStateException(
                    "Puoi proporre uno scambio solo sulle tue richieste di competenza.");
        }
        app.persist();
        return ExchangeDto.from(exchange, requesterId, app.getState());
    }

    @PutMapping("/{id}/accept")
    public ExchangeDto accept(@PathVariable String id, @RequestBody Map<String, String> body) {
        String studentId = required(body, "studentId");
        Exchange exchange = app.getExchangeService().accept(id, studentId);
        app.persist();
        return ExchangeDto.from(exchange, studentId, app.getState());
    }

    @PutMapping("/{id}/complete")
    public ExchangeDto complete(@PathVariable String id, @RequestBody Map<String, String> body) {
        String studentId = required(body, "studentId");
        Exchange exchange = app.getExchangeService().complete(id, studentId);
        app.persist();
        return ExchangeDto.from(exchange, studentId, app.getState());
    }

    @PutMapping("/{id}/cancel")
    public ExchangeDto cancel(@PathVariable String id, @RequestBody Map<String, String> body) {
        String studentId = required(body, "studentId");
        Exchange exchange = app.getExchangeService().cancel(id, studentId);
        app.persist();
        return ExchangeDto.from(exchange, studentId, app.getState());
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

    private static String requiredAny(Map<String, String> body, String... keys) {
        for (String key : keys) {
            String v = body.getOrDefault(key, "").trim();
            if (!v.isEmpty()) {
                return v;
            }
        }
        throw new IllegalArgumentException("Campo obbligatorio: " + keys[0]);
    }
}
