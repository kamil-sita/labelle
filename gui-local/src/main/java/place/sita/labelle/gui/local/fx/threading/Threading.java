package place.sita.labelle.gui.local.fx.threading;

import javafx.application.Platform;
import place.sita.labelle.core.utils.Either;

public class Threading {

	public static KeyStone keyStone() {
		return new KeyStoneImpl();
	}

	public static Cancellable onFxThread(InFxThread inFx) {
		return onFxThread(null, inFx);
	}

	public static Cancellable onFxThread(KeyStone keyStone, InFxThread inFx) {
		CancellableImpl cancellable = new CancellableImpl();
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
		CancellableImpl cancellable = new CancellableImpl();
		KeyStoneImpl impl = null;
		if (keyStone != null) {
			impl = (KeyStoneImpl) keyStone;
			impl.replace(cancellable);
		}
		doOnSeparateThread(Either.left(inSeparate), cancellable);
		return cancellable;
	}

	private static void doOnFxThread(Either<InFxThread, InFxThreadI> thread, CancellableImpl cancellable) {
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

	private static void doOnSeparateThread(Either<InSeparateThread, InSeparateThreadI> left, CancellableImpl cancellable) {
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
			public void onFxThread(InFxThreadI inFxThread) {
				if (cancellable.continueExecution) {
					doOnFxThread(Either.right(inFxThread), cancellable);
				}
			}

			@Override
			public void onSeparateThread(InSeparateThreadI inSeparateThread) {
				if (cancellable.continueExecution) {
					doOnSeparateThread(Either.right(inSeparateThread), cancellable);
				}
			}
		};
	}

	public interface Toolkit {

		void onFxThread(InFxThreadI inFxThread);

		void onSeparateThread(InSeparateThreadI inSeparateThread);

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
	public interface InFxThreadI {

		void execute();

	}

	@FunctionalInterface
	public interface InSeparateThread {

		void execute(Toolkit toolkit);

	}

	@FunctionalInterface
	public interface InSeparateThreadI {

		void execute();

	}

	private static class CancellableImpl implements Cancellable {

		private boolean continueExecution = true;

		@Override
		public void cancelPropagation() {
			continueExecution = false;
		}
	}

}
