package place.sita.labelle.core.utils;

import place.sita.labelle.core.utils.ResultRouters.ElementConsumer;

public class Result2<T, E> implements Successable {
    private final boolean isSuccess;
    private final T t;
    private final E e;


    private Result2(boolean isSuccess, T t, E e) {
        this.isSuccess = isSuccess;
        this.t = t;
        this.e = e;
    }

    public static <T, E> Result2<T, E> success(T t) {
        return new Result2<>(true, t, null);
    }

    public static <T, E> Result2<T, E> failure(E e) {
        return new Result2<>(false, null, e);
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    public T getSuccess() {
        return t;
    }

    public E getFailure() {
        return e;
    }

    public Result2<T, E> onSuccess(ElementConsumer<T> onSuccess) {
        if (isSuccess()) {
            onSuccess.accept(t);
        }
        return this;
    }

    public Result2<T, E> onFailure(ElementConsumer<E> onFailure) {
        if (!isSuccess()) {
            onFailure.accept(e);
        }
        return this;
    }
}
