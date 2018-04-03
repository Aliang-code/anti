package dna.origins.commons;

public interface CallBackWithResult<T> {
    T execute(Object... args);
}
