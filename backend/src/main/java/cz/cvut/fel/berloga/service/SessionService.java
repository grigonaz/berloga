package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.entity.SessionEntity;
import cz.cvut.fel.berloga.repository.SessionRepository;
import cz.cvut.fel.berloga.repository.UserRepository;
import cz.cvut.fel.berloga.service.exceptions.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class SessionService {

    protected final SessionRepository sessionRepository;
    protected final HttpSession httpSession;
    protected final int sessionTTL = 1440; // in seconds
    protected final UserRepository userRepository;


    @Autowired
    public SessionService(SessionRepository sessionRepository, HttpSession httpSession, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.httpSession = httpSession;
        this.userRepository = userRepository;
    }

    /**
     * Get session from database, doesnt check his validity
     *
     * @return SessionEntity
     */
    public SessionEntity getSession() {
        List<SessionEntity> sessions = this.sessionRepository.findAllBySession(httpSession.getId());
        if (sessions.size() > 0) {
            return sessions.get(0);
        } else {
            return null;
        }
    }

    public void associateSession(Long id) {
        this.dissociateSession(id);
        SessionEntity storedSession = new SessionEntity();
        storedSession.setSession(this.httpSession.getId());
        storedSession.setUser(userRepository.findById(id).orElse(null));
        storedSession.setLastAccess(OffsetDateTime.now());
        this.sessionRepository.save(storedSession);
    }

    public void dissociateSession(Long id) {
        List<SessionEntity> sessions = this.sessionRepository.findAllBySession(httpSession.getId());
        List<SessionEntity> sessions2 = this.sessionRepository.findAllByUserId(id);
        if (sessions.size() > 0) {
            sessions.forEach(this.sessionRepository::delete);
        }
        if (sessions2.size() > 0) {
            sessions2.forEach(this.sessionRepository::delete);
        }
    }

    /**
     * Checking if user is authenticated and token is not expired
     *
     * @return true if user is logged, otherwise false
     */
    public boolean isLogged() {
        SessionEntity session = this.getSession();

        boolean valid = (session != null && session.getUser() != null);
        valid = valid && (Instant.now().getEpochSecond() -
                session.getLastAccess().toInstant().getEpochSecond()) < this.sessionTTL;
        if (valid) {
            session.setLastAccess(OffsetDateTime.now());
            this.sessionRepository.save(session);
        }
        if (session != null && !valid) {
            this.sessionRepository.delete(session);
        }
        return valid;
    }

    public void checkLogin() throws AuthorizationException {
        if(!this.isLogged()) {
            throw new AuthorizationException();
        }
    }

}
