package dev.protoskey.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/cities")
public class CityChangesEndpoint {
    private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
    }

    public static void broadcast(String message) {
        for (Session session : SESSIONS) {
            if (!session.isOpen()) {
                SESSIONS.remove(session);
                continue;
            }
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                SESSIONS.remove(session);
            }
        }
    }
}
