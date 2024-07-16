package place.sita.magicscheduler.tasktype;

public record HistoricTask(String code, String name) implements TaskTypeRef {
	@Override
	public boolean isHistoric() {
		return true;
	}
}
