package de.dsa.prodis.service.registry.config;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A customize SessionLog to log the EclipseLink-JPA-Logs to files and
 * Elasticsearch like all other logs in PlantHUB.
 *
 * @see org.eclipse.persistence.logging.AbstractSessionLog#log(org.eclipse.
 *      persistence.logging.SessionLogEntry)
 */
@Component
public class JpaCustomSessionLog extends AbstractSessionLog implements SessionLog {

	private static final String RECOGNITION_BEGINNING = "EclipseLink JPA: ";
	private static final String NOT_AVAILABLE = "N/A";

	@Override
	public void log(SessionLogEntry sessionLogEntry) {
		Logger logger = LoggerFactory.getLogger(NOT_AVAILABLE);
		if (sessionLogEntry == null) {
			logger.warn(RECOGNITION_BEGINNING + "A jpa log-session was triggered with the value NULL.");
          	return;
		}

		boolean hasException = sessionLogEntry.getException() != null;
		String message = buildMessage(sessionLogEntry);
		if (sessionLogEntry.getSourceClassName() != null && (!sessionLogEntry.getSourceClassName().isEmpty())) {
			// Customize logger if possible
			logger = LoggerFactory.getLogger(sessionLogEntry.getSourceClassName());
		}

		// Log the JPA Message ---------------------------------
		switch (sessionLogEntry.getLevel()) {
		case SessionLog.SEVERE:
			if (hasException) {
				logger.error(message, sessionLogEntry.getException());
			} else {
				logger.error(message + " - See the following session-log-entry: {}", sessionLogEntry);
			}
			break;
		case SessionLog.WARNING:
			if (hasException) {
				logger.warn(message, sessionLogEntry.getException());
			} else {
				logger.warn(message + " - See the following session-log-entry: {}", sessionLogEntry);
			}
			break;
		case SessionLog.INFO:
			if (hasException) {
				logger.info(message, sessionLogEntry.getException());
			} else {
				logger.info(message + " - See the following session-log-entry: {}", sessionLogEntry);
			}
			break;
		default:
			// Includes:
			// case SessionLog.CONFIG:
			// case SessionLog.FINE:
			// case SessionLog.FINER:
			// case SessionLog.FINEST:
			// case SessionLog.ALL:
			if (hasException) {
				logger.debug(message, sessionLogEntry.getException());
			} else {
				logger.debug(message + " - See the following session-log-entry: {}", sessionLogEntry);
			}
			break;
		}
	}

	/**
	 * Build the log message.
	 * 
	 * @param sessionLogEntry the session log entry from jpa
	 * @return log message
	 */
	private String buildMessage(SessionLogEntry sessionLogEntry) {
		String message = sessionLogEntry.getMessage();
		if (message == null || message.isEmpty()) {
			if (sessionLogEntry.getException() != null) {
				message = sessionLogEntry.getException().getMessage();
			} else {
				message = "A jpa log-session was triggered, without a message or an exception.";
			}
		}
		return RECOGNITION_BEGINNING + message;

	}

}
