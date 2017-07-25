package br.ufsc.core.trajectory.semantic;

/**
 * 
 * @author Andr� Salvaro Furtado
 *
 */
public class Move {
	
	private int moveId;
	private double startTime;
	private double endTime;
	private Stop start;
	private Stop end;
	private int begin;
	private int length;
	private double angle;

	public Move(int moveId, Stop start, Stop end, double startTime, double endTime, int begin, int length) {
		this(moveId, start, end, startTime, endTime, begin, length, 0.0);
	}

	public Move(int moveId, Stop start, Stop end, double startTime, double endTime, int begin, int length, double angle) {
		this.moveId = moveId;
		this.start = start;
		this.end = end;
		this.startTime = startTime;
		this.endTime = endTime;
		this.begin = begin;
		this.length = length;
		this.angle = angle;
	}

	public int getMoveId() {
		return moveId;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public Stop getStart() {
		return start;
	}

	public void setStart(Stop start) {
		this.start = start;
	}

	public Stop getEnd() {
		return end;
	}

	public void setEnd(Stop end) {
		this.end = end;
	}

	public void setMoveId(int moveId) {
		this.moveId = moveId;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return "Move [moveId=" + moveId + ", startTime=" + startTime + ", endTime=" + endTime + ", start=" + start + ", end=" + end
				+ ", begin=" + begin + ", length=" + length + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + moveId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if (moveId != other.moveId)
			return false;
		return true;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
}
