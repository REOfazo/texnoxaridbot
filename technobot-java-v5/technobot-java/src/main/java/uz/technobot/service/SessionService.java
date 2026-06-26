package uz.technobot.service;

import org.springframework.stereotype.Service;
import uz.technobot.enums.UserState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionService — har bir user uchun holatni xotirada saqlaydi.
 * Production uchun: Redis + Spring Cache bilan almashtiring.
 */
@Service
public class SessionService {

    private record Session(
        UserState state,
        String lang,
        Map<String, Object> data
    ) {
        Session withState(UserState s) { return new Session(s, lang, data); }
        Session withLang(String l)     { return new Session(state, l, data); }
    }

    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    private Session session(Long userId) {
        return sessions.computeIfAbsent(userId,
            id -> new Session(UserState.IDLE, "uz", new HashMap<>()));
    }

    // ── State ────────────────────────────────────────────────

    public UserState getState(Long userId) {
        return session(userId).state();
    }

    public void setState(Long userId, UserState state) {
        sessions.compute(userId, (id, s) ->
            s == null ? new Session(state, "uz", new HashMap<>()) : s.withState(state));
    }

    // ── Language ─────────────────────────────────────────────

    public String getLang(Long userId) {
        return session(userId).lang();
    }

    public void setLang(Long userId, String lang) {
        sessions.compute(userId, (id, s) ->
            s == null ? new Session(UserState.IDLE, lang, new HashMap<>()) : s.withLang(lang));
    }

    // ── Temp data ─────────────────────────────────────────────

    public void setData(Long userId, String key, Object value) {
        session(userId).data().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(Long userId, String key) {
        return (T) session(userId).data().get(key);
    }

    public void clearData(Long userId) {
        session(userId).data().clear();
    }

    public void clearSession(Long userId) {
        sessions.remove(userId);
    }
}
