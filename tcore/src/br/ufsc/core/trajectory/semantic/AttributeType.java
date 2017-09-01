package br.ufsc.core.trajectory.semantic;

import java.util.function.Function;

public enum AttributeType {

	MOVE_ANGLE((Move m) -> m.getAngle()),
	MOVE_TRAVELLED_DISTANCE((Move m) -> m.getTravelledDistance()),
	MOVE_POINTS((Move m) -> m.getPoints()),
	MOVE_STREET_NAME((Move m) -> m.getStreetName()),
	STOP_CENTROID((Stop s) -> s.getCentroid()),
	STOP_STREET_NAME((Stop s) -> s.getStreetName()),
	STOP_NAME((Stop s) -> s.getStreetName()),
	STOP_STREET_NAME_MOVE_ANGLE((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStreetName();
		} else if(s.getMove() != null) {
			return s.getMove().getAngle();
		}
		return null;
	}),
	STOP_NAME_MOVE_STREET_NAME((StopMove s) -> {
		if(s.getStop() != null) {
			return s.getStop().getStopName();
		} else if(s.getMove() != null) {
			return s.getMove().getStreetName();
		}
		return null;
	});
	
	private Function func;

	private <T, R> AttributeType(java.util.function.Function<T, R> func) {
		this.func = func;
	}

	public <T, R> R getValue(T d1) {
		return (R) func.apply(d1);
	}
}