package in.codebuckets.acmemanager.server.jpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

/**
 * This is an abstract class that can be used to retrieve elements from a repository
 * in a backpressure-aware manner to prevent out of memory errors.
 *
 * @param <T> the type of the element
 */
public abstract class BackpressureAwareRepositoryRetriever<T> {

    private static final Logger logger = LogManager.getLogger();

    private boolean inTransit;

    private final AtomicLong elementsProcessed = new AtomicLong(0);
    private final Pageable pageable;

    /**
     * Create a new instance of {@link BackpressureAwareRepositoryRetriever}
     * with {@link Pageable} default page size of 50
     */
    protected BackpressureAwareRepositoryRetriever() {
        this(Pageable.ofSize(50));
    }

    /**
     * Create a new instance of {@link BackpressureAwareRepositoryRetriever}
     * with the given {@link Pageable}
     */
    protected BackpressureAwareRepositoryRetriever(Pageable pageable) {
        this.pageable = requireNonNull(pageable, "Pageable cannot be null");
    }

    protected void processElements() {
        // If we're in transit, then don't do anything.
        if (inTransit) {
            logger.info("Discarding call as it is already in transit");
            return;
        }

        try {
            // Reset the elements processed counter
            elementsProcessed.set(0);

            inTransit = true;

            Page<T> elementsPage = doQuery(pageable);

            // If there are no billing records, then return
            if (elementsPage.isEmpty()) {
                logger.debug("No elements to process in the repository");
                return;
            }

            while (true) {
                for (T element : elementsPage.getContent()) {
                    try {
                        boolean handleSuccess = handle(element);
                        if (handleSuccess) {
                            elementsProcessed.incrementAndGet();
                            logger.debug("Successfully processed element: {}", element);
                        } else {
                            logger.debug("Failed to process element: {}", element);
                        }
                    } catch (Exception e) {
                        logger.error("Unexpected error while processing element: {}", element, e);
                    }
                }

                // If we have more pages, then fetch the next page
                // Else, break the loop
                if (elementsPage.hasNext()) {
                    elementsPage = doQuery(elementsPage.nextPageable());
                } else {
                    break;
                }
            }
        } finally {
            inTransit = false;
        }
    }

    /**
     * Check if the element is in transit
     *
     * @return true if in transit, false otherwise
     */
    protected boolean inTransit() {
        return inTransit;
    }

    /**
     * Get the number of elements processed
     *
     * @return the number of elements processed
     */
    public long elementsProcessed() {
        return elementsProcessed.get();
    }

    /**
     * Handle the element
     *
     * @param element the element
     */
    protected abstract boolean handle(T element);

    /**
     * Query the repository for elements
     *
     * @return the {@link Page} of elements
     */
    protected abstract Page<T> doQuery(Pageable pageable);
}
