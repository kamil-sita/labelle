package place.sita.labelle.state;

import java.util.concurrent.TimeUnit;

public class StateChange {

	private StateChange() {

	}

	public static StateChangeBuilderWithAction withAction(Action action) {
		return new StateChangeBuilderWithAction(action);
	}

	public static StateChangeBuilderWithStateAssertion expect(StateAssertion assertion) {
		return new StateChangeBuilderWithStateAssertion(assertion);
	}

	public static class StateChangeBuilderWithStateAssertion {
		private final StateAssertion stateAssertion;

		public StateChangeBuilderWithStateAssertion(StateAssertion stateAssertion) {
			this.stateAssertion = stateAssertion;
		}

		public StateChangeBuilder withAction(Action action) {
			return new StateChangeBuilder(stateAssertion, action);
		}

	}

	public static class StateChangeBuilderWithAction {
		private final Action action;

		public StateChangeBuilderWithAction(Action action) {
			this.action = action;
		}

		public StateChangeBuilder expect(StateAssertion stateAssertion) {
			return new StateChangeBuilder(stateAssertion, action);
		}
	}

	public static class StateChangeBuilder {
		private StateAssertion stateAssertion;
		private Action action;
		private int fetchEvery = 100;
		private TimeUnit fetchEveryUnit = TimeUnit.MILLISECONDS;
		private int timeout = 15;
		private TimeUnit timeoutUnit = TimeUnit.SECONDS;

		// clone constructor
		private StateChangeBuilder(StateChangeBuilder builder) {
			this.stateAssertion = builder.stateAssertion;
			this.action = builder.action;
			this.fetchEvery = builder.fetchEvery;
			this.fetchEveryUnit = builder.fetchEveryUnit;
			this.timeout = builder.timeout;
			this.timeoutUnit = builder.timeoutUnit;
		}

		private StateChangeBuilder(StateAssertion stateAssertion, Action action) {
			this.stateAssertion = stateAssertion;
			this.action = action;
		}

		public StateChangeBuilder withPollRate(int fetchEvery, TimeUnit fetchEveryUnit) {
			StateChangeBuilder builder = new StateChangeBuilder(this);
			builder.fetchEvery = fetchEvery;
			builder.fetchEveryUnit = fetchEveryUnit;
			return builder;
		}

		public StateChangeBuilder withTimeOut(int timeout, TimeUnit timeoutUnit) {
			StateChangeBuilder builder = new StateChangeBuilder(this);
			builder.timeout = timeout;
			builder.timeoutUnit = timeoutUnit;
			return builder;
		}

		public void test() {
			long start = System.currentTimeMillis();
			boolean initialResult = stateAssertion.test();
			if (initialResult) {
				throw new AssertionError("State assertion already true: " + stateAssertion.description());
			}
			action.run();
			while (true) {
				if (stateAssertion.test()) {
					break;
				}
				if (System.currentTimeMillis() - start > timeoutUnit.toMillis(timeout)) {
					throw new IllegalStateException("Timeout while waiting for: " + stateAssertion.description());
				}
				try {
					Thread.sleep(fetchEveryUnit.toMillis(fetchEvery));
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
		}

	}

}
