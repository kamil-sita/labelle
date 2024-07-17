package place.sita.magicscheduler.tasktype.annotationdef;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.scheduler.BaseTaskSandbox;
import place.sita.magicscheduler.tasktype.TaskType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class AnnotationBasedTaskTypeFactoryTest {

	private interface Listener {
		void accept(BaseTaskSandbox sandbox, String parameter);
	}

	private static class ClassWithMethodWithSandboxAndParameter {

		private final Listener listener;

		private ClassWithMethodWithSandboxAndParameter(Listener listener) {
			this.listener = listener;
		}


		@MsTask(code = "test1", name = "test1")
		public void method(@MsSandbox BaseTaskSandbox sandbox, @MsParameter String parameter) {
			listener.accept(sandbox, parameter);
		}

	}

	@Test
	public void shouldInjectProperlyIntoATaskWithSandboxAndParameter() throws NoSuchMethodException {
		// given
		Listener listener = Mockito.mock(Listener.class);
		ClassWithMethodWithSandboxAndParameter instance = new ClassWithMethodWithSandboxAndParameter(listener);
		TaskContext taskContext = Mockito.mock(TaskContext.class);

		// when
		TaskType tt = AnnotationBasedTaskTypeFactory.build(
			ClassWithMethodWithSandboxAndParameter.class.getMethod("method", BaseTaskSandbox.class, String.class),
			instance
		);

		tt.runTask("test", taskContext);

		// then
		ArgumentCaptor<BaseTaskSandbox> captor = ArgumentCaptor.forClass(BaseTaskSandbox.class);
		verify(listener).accept(captor.capture(), Mockito.eq("test"));
		assertThat(captor.getValue()).isSameAs(taskContext);
	}

	private static class ClassWithMethodWithOnlySandbox {

		private final Listener listener;

		private ClassWithMethodWithOnlySandbox(Listener listener) {
			this.listener = listener;
		}

		@MsTask(code = "test2", name = "test2")
		public void method(@MsSandbox BaseTaskSandbox sandbox) {
			listener.accept(sandbox, null);
		}

	}

	@Test
	public void shouldInjectProperlyIntoATaskWithOnlySandbox() throws NoSuchMethodException {
		// given
		Listener listener = Mockito.mock(Listener.class);
		ClassWithMethodWithOnlySandbox instance = new ClassWithMethodWithOnlySandbox(listener);
		TaskContext taskContext = Mockito.mock(TaskContext.class);

		// when
		TaskType tt = AnnotationBasedTaskTypeFactory.build(
			ClassWithMethodWithOnlySandbox.class.getMethod("method", BaseTaskSandbox.class),
			instance
		);

		tt.runTask(null, taskContext);

		// then
		ArgumentCaptor<BaseTaskSandbox> captor = ArgumentCaptor.forClass(BaseTaskSandbox.class);
		verify(listener).accept(captor.capture(), Mockito.eq(null));
		assertThat(captor.getValue()).isSameAs(taskContext);
	}

	private static class ClassWithMethodWithOnlyParameter {

		private final Listener listener;

		private ClassWithMethodWithOnlyParameter(Listener listener) {
			this.listener = listener;
		}

		@MsTask(code = "test3", name = "test3")
		public void method(@MsParameter String parameter) {
			listener.accept(null, parameter);
		}
	}

	@Test
	public void shouldInjectProperlyIntoATaskWithOnlyParameter() throws NoSuchMethodException {
		// given
		Listener listener = Mockito.mock(Listener.class);
		ClassWithMethodWithOnlyParameter instance = new ClassWithMethodWithOnlyParameter(listener);
		TaskContext taskContext = Mockito.mock(TaskContext.class);

		// when
		TaskType tt = AnnotationBasedTaskTypeFactory.build(
			ClassWithMethodWithOnlyParameter.class.getMethod("method", String.class),
			instance
		);

		tt.runTask("test", taskContext);

		// then
		verify(listener).accept(null, "test");
	}

	private static class ClassWithMethodWithNeitherSandboxNorParameter {

		private final Listener listener;

		private ClassWithMethodWithNeitherSandboxNorParameter(Listener listener) {
			this.listener = listener;
		}

		@MsTask(code = "test4", name = "test4")
		public void method() {
			listener.accept(null, null);
		}
	}

	@Test
	public void shouldInjectProperlyIntoATaskWithNeitherSandboxNorParameter() throws NoSuchMethodException {
		// given
		Listener listener = Mockito.mock(Listener.class);
		ClassWithMethodWithNeitherSandboxNorParameter instance = new ClassWithMethodWithNeitherSandboxNorParameter(listener);
		TaskContext taskContext = Mockito.mock(TaskContext.class);

		// when
		TaskType tt = AnnotationBasedTaskTypeFactory.build(
			ClassWithMethodWithNeitherSandboxNorParameter.class.getMethod("method"),
			instance
		);

		tt.runTask(null, taskContext);

		// then
		verify(listener).accept(null, null);
	}
}
