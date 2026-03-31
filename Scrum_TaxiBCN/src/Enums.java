/**
 * Possible operational states of a taxi.
 */
enum TaxiStatus {
    AVAILABLE,
    BUSY,
    ON_BREAK
}

/**
 * Type of taxi: standard or adapted for reduced mobility.
 */
enum TaxiType {
    STANDARD,
    ADAPTED
}

/**
 * Lifecycle states of a service request.
 */
enum ServiceStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
