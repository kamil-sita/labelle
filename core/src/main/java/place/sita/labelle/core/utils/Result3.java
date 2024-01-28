package place.sita.labelle.core.utils;

import place.sita.labelle.core.utils.ResultRouters.ElementConsumer;

public class Result3<T, E1, E2> implements Successable {
    private final int state;
    private final T t;
    private final E1 e1;
    private final E2 e2;


    private Result3(int state, T t, E1 e1, E2 e2) {
        this.state = state;
        this.t = t;
        this.e1 = e1;
        this.e2 = e2;
    }

    public static <T, E1, E2> Result3<T, E1, E2> success(T t) {
        return new Result3<>(0, t, null, null);
    }

    public static <T, E1, E2> Result3<T, E1, E2> failure1(E1 e1) {
        return new Result3<>(1, null, e1, null);
    }

    public static <T, E1, E2> Result3<T, E1, E2> failure2(E2 e2) {
        return new Result3<>(2, null, null, e2);
    }

    @Override
    public boolean isSuccess() {
        return getResult() == 0;
    }

    public int getResult() {
        return state;
    }

    public T getSuccess() {
        return t;
    }

    public E1 getFailure1() {
        return e1;
    }

    public E2 getFailure2() {
        return e2;
    }

    public Result3<T, E1, E2> onSuccess(ElementConsumer<T> onSuccess) {
        if (state == 0) {
            onSuccess.accept(t);
        }
        return this;
    }

    public Result3<T, E1, E2> onFailure1(ElementConsumer<E1> onFailure) {
        if (state == 1) {
            onFailure.accept(e1);
        }
        return this;
    }

    public Result3<T, E1, E2> onFailure2(ElementConsumer<E2> onFailure) {
        if (state == 2) {
            onFailure.accept(e2);
        }
        return this;
    }
}
