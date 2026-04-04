package br.com.magnus.detectionandrefactoring.gateway;

/**
 * Gateway for sending projects to downstream processing.
 * <p>
 * Implementations may use different message brokers (Redis RQueue, AWS SQS, etc.)
 * to queue projects for the next stage of processing.
 */
public interface SendProject {

    /**
     * Sends a project identifier for downstream processing.
     *
     * @param id the project identifier to send
     */
    void send(String id);
}
