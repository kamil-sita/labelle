package place.sita.magicscheduler.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunPolicyTest {

	@Test
	public void shouldAlwaysRunPolicyBeActive() {
		// given
		RunPolicy runPolicy = RunPolicy.always();

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldIfJobSucceededRunPolicyBeActiveIfJobSucceeded() {
		// given
		RunPolicy runPolicy = RunPolicy.ifJobSucceeded();
		ExecutionEnvironmentResult result = success();

		// when
		boolean shouldRun = runPolicy.shouldRun(result);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldIfJobSucceededRunPolicyNotBeActiveIfJobFailed() {
		// given
		RunPolicy runPolicy = RunPolicy.ifJobSucceeded();
		ExecutionEnvironmentResult result = failed();

		// when
		boolean shouldRun = runPolicy.shouldRun(result);

		// then
		assertFalse(shouldRun);
	}

	@Test
	public void shouldTrueOrTrueBeTrue() {
		// given
		RunPolicy runPolicy = RunPolicy.always().or(RunPolicy.always());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldTrueOrFalseBeTrue() {
		// given
		RunPolicy runPolicy = RunPolicy.always().or(runPolicyNever());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldFalseOrTrueBeTrue() {
		// given
		RunPolicy runPolicy = runPolicyNever().or(RunPolicy.always());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldFalseOrFalseBeFalse() {
		// given
		RunPolicy runPolicy = runPolicyNever().or(runPolicyNever());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertFalse(shouldRun);
	}

	@Test
	public void shouldTrueAndTrueBeTrue() {
		// given
		RunPolicy runPolicy = RunPolicy.always().and(RunPolicy.always());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertTrue(shouldRun);
	}

	@Test
	public void shouldTrueAndFalseBeFalse() {
		// given
		RunPolicy runPolicy = RunPolicy.always().and(runPolicyNever());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertFalse(shouldRun);
	}

	@Test
	public void shouldFalseAndTrueBeFalse() {
		// given
		RunPolicy runPolicy = runPolicyNever().and(RunPolicy.always());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertFalse(shouldRun);
	}

	@Test
	public void shouldFalseAndFalseBeFalse() {
		// given
		RunPolicy runPolicy = runPolicyNever().and(runPolicyNever());

		// when
		boolean shouldRun = runPolicy.shouldRun(null);

		// then
		assertFalse(shouldRun);
	}

	private static RunPolicy runPolicyNever() {
		return ignored -> false;
	}

	private ExecutionEnvironmentResult success() {
		return new ExecutionEnvironmentResult(false, false, false, false, true);
	}

	private ExecutionEnvironmentResult failed() {
		return new ExecutionEnvironmentResult(false, false, false, true, false);
	}

}
