package BangBangMotion;

import javafx.beans.property.SimpleStringProperty;

public class sdFileLine {
	
	private final SimpleStringProperty sdServoValues;
	private final SimpleStringProperty sdSpeed;
	private final SimpleStringProperty sdHold;
	
	public sdFileLine(String sdServoValues, String sdSpeed, String sdHold) {
		this.sdServoValues = new SimpleStringProperty(sdServoValues);
		this.sdSpeed = new SimpleStringProperty(sdSpeed);
		this.sdHold = new SimpleStringProperty(sdHold);
		
	}

	public String getSdServoValues() {
		return sdServoValues.getValue();
	}

	public void setSdServoValues(String Values) {
		sdServoValues.setValue(Values);
	}

	public String getSdSpeed() {
		return sdSpeed.getValue();
	}

	public void setSdSpeed(String Speed) {
		sdSpeed.setValue(Speed);
	}

	public String getSdHold() {
		return sdHold.getValue();
	}

	public void setSdHold(String Hold) {
		sdHold.setValue(Hold);
	}
	
	

}
