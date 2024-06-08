package place.sita.labelle.state.assertions;

import place.sita.labelle.state.StateAssertion;

import java.util.Objects;
import java.util.function.Supplier;

public class SimpleChangeAssertion {
	public static StateAssertion changeIn(Supplier<Number> numberSupplier) {
		return new StateAssertion() {
			private Number initialValue;
			private boolean firstRun = true;
			@Override
			public boolean test() {
				if (firstRun) {
					initialValue = numberSupplier.get();
					firstRun = false;
					return false;
				} else {
					Number newNumber = numberSupplier.get();
					return !Objects.equals(initialValue, newNumber);
				}
			}

			@Override
			public String description() {
				return "change in supplier value";
			}
		};
	}
	public static <T> StateAssertion nonNullChangeIn(Supplier<T> supplier) {
		return new StateAssertion() {
			private T initialValue;
			private boolean firstRun = true;
			@Override
			public boolean test() {
				if (firstRun) {
					initialValue = supplier.get();
					firstRun = false;
					return false;
				} else {
					T  newT = supplier.get();
					if (newT == null) {
						return false;
					}
					return !Objects.equals(initialValue, newT);
				}
			}

			@Override
			public String description() {
				return "non-null change in supplier value";
			}
		};
	}
}
