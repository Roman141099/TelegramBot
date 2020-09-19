package Main.UBot.com.TripleFunctions;
@FunctionalInterface
public interface TripleFunction<F, S, T> {
    public F process(F f, S s, T t);
}
