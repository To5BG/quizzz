package server.api;

import commons.GameSession;
import commons.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("api/sessions")
public class SessionController {

    private final SessionRepository repo;

    public SessionController(Random random, SessionRepository repo) {
        this.repo = repo;
    }

    @GetMapping(path = {"", "/"})
    public List<GameSession> getAllSessions() {
        return repo.findAll();
    }

    @PutMapping(path = {"", "/"})
    public ResponseEntity<GameSession> addSession(@RequestBody GameSession session) {

        if (session.players == null) return ResponseEntity.badRequest().build();
        for (Player p : session.players) {
            if (isNullOrEmpty(p.username)) return ResponseEntity.badRequest().build();
        }
        GameSession saved = repo.save(session);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Long> removeSessionById(@PathVariable("id") long id) {
        GameSession session = repo.findById(id).orElse(null);
        if (session != null) {
            long sessionId = session.id;
            repo.delete(session);
            return ResponseEntity.ok(sessionId);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameSession> getSessionById(@PathVariable("id") long id) {
        if (isInvalid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get());
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable("id") long id) {
        if (isInvalid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get().players);
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<Player> addPlayer(@PathVariable("id") long id, @RequestBody Player player) {
        if (isInvalid(id)) return ResponseEntity.badRequest().build();

        GameSession session = repo.findById(id).orElse(null);
        if (session == null) return ResponseEntity.badRequest().build();

        session.addPlayer(player);
        repo.save(session);
        return ResponseEntity.ok(player);
    }

    @DeleteMapping("/{id}/players/{playerId}")
    public ResponseEntity<Player> removePlayer(@PathVariable("id") long sessionId,
                                               @PathVariable("playerId") long playerId) {

        if (isInvalid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).orElse(null);
        if (session == null) return ResponseEntity.badRequest().build();
        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        session.removePlayer(player);
        repo.save(session);
        return ResponseEntity.ok(player);
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean isInvalid(long id) {
        return id < 0 || !repo.existsById(id);
    }
}
