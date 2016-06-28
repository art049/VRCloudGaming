package vrcgio;

import java.util.ArrayList;

public class AngleMeasure {
	private ArrayList<Float> pitchs;
	private ArrayList<Float> yaws;
	private float fps;

	public AngleMeasure() {
		pitchs = new ArrayList<Float>();
		yaws = new ArrayList<Float>();

	}

	public void addPitch(float newPitch) {
		pitchs.add(newPitch);
	}

	public void addYaw(float newYaw) {
		yaws.add(newYaw);
	}

}
