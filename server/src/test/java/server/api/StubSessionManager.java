package server.api;

import commons.GameSession;
import server.service.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StubSessionManager extends SessionManager {
    public List<String> calledMethods;

    public StubSessionManager() {
        calledMethods = new ArrayList<>();
    }

    @Override
    public GameSession save(GameSession session) {
        calledMethods.add("save");
        return super.save(session);
    }

    @Override
    public GameSession delete(Long id) {
        calledMethods.add("delete");
        return super.delete(id);
    }
}
