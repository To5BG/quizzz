package server.service;

import commons.GameSession;

import java.util.HashMap;
import java.util.List;

public class SessionManager {

    private final HashMap<Long, GameSession> sessions;
    private Long sessionCounter;

    public SessionManager() {
        this.sessionCounter = 1L;
        this.sessions = new HashMap<>();
    }

    /**
     * Save an id/GameSession object pair. Replaces already existing entries with same id
     *
     * @param session Session to be saved
     * @return The saved session, with an updated id
     */
    public GameSession save(GameSession session) {
        Long pointer = (sessions.containsKey(session.id)) ? session.id : sessionCounter++;
        session.id = pointer;
        sessions.put(pointer, session);
        return session;
    }

    /**
     * Remove a GameSession, if it exists, or otherwise null.
     *
     * @param id Id of session to remove
     * @return Removed session
     */
    public GameSession delete(Long id) {
        //Make sure that the SELECTING session is never deleted
        if(getById(id) == null || getById(id).sessionType == GameSession.SessionType.SELECTING) return null;
        return sessions.remove(id);
    }

    public List<GameSession> getValues() {
        return sessions.values().stream().toList();
    }

    public Long getCounter() {
        return this.sessionCounter;
    }

    /**
     * Get a GameSession reference with the provided id
     *
     * @param id Id of GameSession to fetch
     * @return A GameSession object, if one with the provided id exists, or otherwise null
     */
    public GameSession getById(Long id) {
        if (!sessions.containsKey(id)) return null;
        return sessions.get(id);
    }

    /**
     * Checks id validity for current session map
     *
     * @param id Id to be checked
     * @return True iff id is a positive integer, and an element exists with that id
     */
    public boolean isValid(Long id) {
        return id >= 1 && sessions.containsKey(id);
    }
}
