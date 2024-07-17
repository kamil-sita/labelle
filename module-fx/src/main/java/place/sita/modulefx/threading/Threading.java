package place.sita.modulefx.threading;

import javafx.application.Platform;
import place.sita.modulefx.util.Either;

public class Threading {

	public static KeyStone keyStone() {
		return new KeyStoneImpl();
	}

	public static Cancellable onFxThread(InFxThread inFx) {
		return onFxThread(null, inFx);
	}

	public static Cancellable onFxThread(KeyStone keyStone, InFxThread inFx) {
		return onFxThread(keyStone, null, inFx);
	}

	public static Cancellable onFxThread(KeyStone keyStone, OnCancellation onCancellation, InFxThread inFx) {
		CancellableImpl cancellable = new CancellableImpl(onCancellation);
		KeyStoneImpl impl = null;
		if (keyStone != null) {
			impl = (KeyStoneImpl) keyStone;
			impl.replace(cancellable);
		}
		doOnFxThread(Either.left(inFx), cancellable);
		return cancellable;
	}

	public static Cancellable onSeparateThread(InSeparateThread inSeparate) {
		return onSeparateThread(null, inSeparate);
	}

	public static Cancellable onSeparateThread(KeyStone keyStone, InSeparateThread inSeparate) {
		return onSeparateThread(keyStone, null, inSeparate);
	}

	public static Cancellable onSeparateThread(KeyStone keyStone, OnCancellation onCancellation, InSeparateThread inSeparate) {
		CancellableImpl cancellable = new CancellableImpl(onCancellation);
		KeyStoneImpl impl = null;
		if (keyStone != null) {
			impl = (KeyStoneImpl) keyStone;
			impl.replace(cancellable);
		}
		doOnSeparateThread(Either.left(inSeparate), cancellable);
		return cancellable;
	}

	private static void doOnFxThread(Either<InFxThread, InFxThreadInner> thread, CancellableImpl cancellable) {
		if (Platform.isFxApplicationThread()) {
			if (cancellable.continueExecution) {
				if (thread.isLeft()) {
					thread.getLeft().execute(createToolkit(cancellable));
				} else {
					thread.getRight().execute();
				}
			}
		} else {
			if (cancellable.continueExecution) {
				Platform.runLater(() -> {
					if (cancellable.continueExecution) {
						if (thread.isLeft()) {
							thread.getLeft().execute(createToolkit(cancellable));
						} else {
							thread.getRight().execute();
						}
					}
				});
			}
		}
	}

	private static void doOnSeparateThread(Either<InSeparateThread, InSeparateThreadInner> left, CancellableImpl cancellable) {
		if (cancellable.continueExecution) {
			ThreadingSupportSupplier.doRunLater(() -> {
				if (cancellable.continueExecution) {
					if (left.isLeft()) {
						left.getLeft().execute(createToolkit(cancellable));
					} else {
						left.getRight().execute();
					}
				}
			});
		}
	}

	private static Toolkit createToolkit(CancellableImpl cancellable) {
		return new Toolkit() {
			@Override
			public void onFxThread(InFxThreadInner inFxThread) {
				if (cancellable.continueExecution) {
					doOnFxThread(Either.right(inFxThread), cancellable);
				}
			}

			@Override
			public void onSeparateThread(InSeparateThreadInner inSeparateThread) {
				if (cancellable.continueExecution) {
					doOnSeparateThread(Either.right(inSeparateThread), cancellable);
				}
			}
		};
	}

	public interface Toolkit {

		void onFxThread(InFxThreadInner inFxThread);

		void onSeparateThread(InSeparateThreadInner inSeparateThread);

	}

	/**
	 * Represents a holder of cancellable operations - if one operation
	 * was scheduled for keystone, and another one is scheduled, the previous
	 * one will be cancelled.
	 */
	public sealed interface KeyStone {
		void cancel();
	}

	private static final class KeyStoneImpl implements KeyStone {

		private CancellableImpl cancellable;

		public synchronized void replace(CancellableImpl cancellable) {
			cancel();
			this.cancellable = cancellable;
		}

		@Override
		public void cancel() {
			if (this.cancellable != null) {
				this.cancellable.cancelPropagation();
				this.cancellable = null;
			}
		}
	}

	@FunctionalInterface
	public interface InFxThread {

		void execute(Toolkit toolkit);

	}

	@FunctionalInterface
	public interface InFxThreadInner {

		void execute();

	}

	@FunctionalInterface
	public interface InSeparateThread {

		void execute(Toolkit toolkit);

	}

	@FunctionalInterface
	public interface InSeparateThreadInner {

		void execute();

	}

	private static class CancellableImpl implements Cancellable {

		private boolean continueExecution = true;
		private final OnCancellation onCancellation;
		private boolean firstTrigger = true;

		private CancellableImpl(OnCancellation onCancellation) {
			this.onCancellation = onCancellation;
		}

		@Override
		public synchronized void cancelPropagation() {
			if (onCancellation != null) {
				if (firstTrigger) {
					onCancellation.onCancel();
				}
			}
			firstTrigger = false;
			continueExecution = false;
		}
	}

	@FunctionalInterface
	public interface OnCancellation {
		void onCancel();
	}

}
