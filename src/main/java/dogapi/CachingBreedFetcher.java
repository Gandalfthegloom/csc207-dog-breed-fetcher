package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    // TODO Task 2: Complete this class
    private final BreedFetcher fetcher;
    private final Map<String, List<String>> cache = new HashMap<>();
    private int callsMade = 0;
    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException{
        // 1) Serve from cache if present (no new call, no increment)
        List<String> cached = cache.get(breed);
        if (cached != null) {
            return cached;
        }

        // 2) call the underlying fetcher -> increment
        callsMade++;

        try {
            List<String> subBreeds = fetcher.getSubBreeds(breed);

            // Cache successful result (avoid caching failures)
            // Store an unmodifiable copy to prevent external mutation of the cache
            List<String> toCache = Collections.unmodifiableList(new ArrayList<>(subBreeds));
            cache.put(breed, toCache);

            return toCache;
        } catch (BreedNotFoundException e) {
            // Do NOT cache failures; just propagate
            throw e;
        }

    }

    public int getCallsMade() {
        return callsMade;
    }
}