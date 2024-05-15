package place.sita.labelle.core.utils;

import java.util.function.Function;

public class ResultRouters {

    private ResultRouters() {

    }

    public static <ArgT, SuccessT, FailureT, Cr2T extends ConditionalResult2<SuccessT, FailureT, Cr2T>> Evaluator<ArgT, Cr2T> factory(
        Function<ArgT, Result2<SuccessT, FailureT>> evaluator
    ) {
        return arg -> {
            return evaluate(evaluator, arg);
        };
    }

    public static <ArgT, SuccessT, FailureT, Cr2T extends ConditionalResult2<SuccessT, FailureT, Cr2T>> Cr2T evaluate(Function<ArgT, Result2<SuccessT, FailureT>> evaluator, ArgT arg) {
        var result = evaluator.apply(arg);
        if (result.isSuccess()) {
            var success = result.getSuccess();
            return successHandler(success);
        } else {
            var failure = result.getFailure();
            return failureHandler(failure);
        }
    }

    public static <SuccessT, FailureT, Cr2T extends ConditionalResult2<SuccessT, FailureT, Cr2T>> Cr2T successHandler(SuccessT success) {
        return (Cr2T) new BaseConditionalResult2<SuccessT, FailureT, Cr2T>() {
            @Override
            public Cr2T then(ElementConsumer<SuccessT> onSuccess) {
                onSuccess.accept(success);
                return (Cr2T) this;
            }
        };
    }

    public static <SuccessT, FailureT, Cr2T extends ConditionalResult2<SuccessT, FailureT, Cr2T>> Cr2T failureHandler(FailureT failure) {
        return (Cr2T) new BaseConditionalResult2<SuccessT, FailureT, Cr2T>() {
            @Override
            public Cr2T otherwise(ElementConsumer<FailureT> onFailure) {
                onFailure.accept(failure);
                return (Cr2T) this;
            }
        };
    }

    public interface ConditionalResult2<SuccessT, FailureT, SelfT> {

        SelfT then(ElementConsumer<SuccessT> onSuccess);

        SelfT otherwise(ElementConsumer<FailureT> onFailure);

    }

    public static abstract class BaseConditionalResult2<SuccessT, FailureT, SelfT> implements ConditionalResult2<SuccessT, FailureT, SelfT> {

        @Override
        public SelfT then(ElementConsumer<SuccessT> onSuccess) {
            return (SelfT) this;
        }

        @Override
        public SelfT otherwise(ElementConsumer<FailureT> onFailure) {
            return (SelfT) this;
        }
    }


    @FunctionalInterface
    public interface ElementConsumer<ElementT> {
        void accept(ElementT element);
    }

    @FunctionalInterface
    public interface Evaluator<T, R> {
        R evaluate(T t);
    }

}
