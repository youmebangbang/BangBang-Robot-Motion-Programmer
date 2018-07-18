package BangBangMotion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class usbConnect implements Runnable {

	private SerialPort esp32Port;
	private ArrayList<String> sdFileList;
	private InputStream inData;
	private OutputStream outData;
	private String myString;
	private Boolean threadOn;
	long lastTime;

	public usbConnect() {
		esp32Port = null;
		threadOn = true;
		lastTime = System.nanoTime();
	}

	public void sendModelOn() {
		outData = esp32Port.getOutputStream();
		myString = "MODELON" + "|";
		System.out.println(myString);

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendModelOff() {
		outData = esp32Port.getOutputStream();

		myString = "MODELOFF" + "|";
		System.out.println("saving to robot: " + myString);

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String lockModelPosition() {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Send a device info request
		myString = "LOCKPOS" + "|";

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Give enough time for response
		try {
			//more?
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder fbuilder = new StringBuilder();
		char inchar = 0;

		while (esp32Port.isOpen()) {

			try {
				if (inData.available() > 0) {
					inchar = (char) inData.read();
					if (inchar == '|') {
						break;
					}
					fbuilder.append(inchar);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//remove trailing comma

		myString = fbuilder.toString();
		return myString;

	}

	public void sendMotionSet(String outFile) {
		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		myString = "PLAYMOTION" + outFile + "|";
		System.out.println("saving to robot: " + myString);

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeFile(String fileName) {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Removing " + fileName);
		outData = esp32Port.getOutputStream();
		myString = "REMOVEFILE" + fileName + "|";
		try {
			outData.write(myString.getBytes()); //no \n with getBytes
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Give esp32 time to catch up
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createNewFile(String fileName) {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		myString = "SAVENEWFILE" + fileName + "|";
		try {
			outData.write(myString.getBytes()); //no \n with getBytes
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Give esp32 time to catch up
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateRobotPose(String theString) {

		long currentTime = System.nanoTime();
		long timeLapseMillis = (currentTime - lastTime) / 100000;

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Send updated servo values
		myString = "SETPOSE" + theString + "|";
		System.out.println(timeLapseMillis);

		if (timeLapseMillis > 2000) {
			System.out.println("UpdatingPose: " + theString);
			lastTime = currentTime;

			try {
				outData.write(myString.getBytes()); //no \n with getBytes
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}



	public void saveFileRobot(String fileName, String outFile) {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		myString = "SAVEFILE" + fileName + "^" + outFile + "|";
		System.out.println("saving to robot: " + myString);

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] getSdFile(String fileName) {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Send a device info request
		myString = "F_" + fileName + "|";

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Give enough time for response
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder fbuilder = new StringBuilder();
		char inchar = 0;

		while (esp32Port.isOpen()) {

			try {
				if (inData.available() > 0) {
					inchar = (char) inData.read();
					if (inchar == '|') {
						break;
					}
					fbuilder.append(inchar);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//remove trailing comma

		myString = fbuilder.toString();
		//myString = myString.substring(0, (myString.length() - 1));
		String[] fileItems = myString.split(",");
		return fileItems;
	}

	public ArrayList<String> getFileList() {

		inData = esp32Port.getInputStream();
		outData = esp32Port.getOutputStream();

		//Clear buffer
		try {
			while (inData.available() > 0) {
				inData.read();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Send a device info request
		myString = "SENDFILELIST" + "|";

		try {
			outData.write(myString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Give enough time for response
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Receive and build input into a string
		StringBuilder sbuilder = new StringBuilder();
		char inchar = 0;

		while (esp32Port.isOpen()) {

			try {
				if (inData.available() > 0) {
					inchar = (char) inData.read();
					//Remove excess endline chars since stringbuilder will readd them
					if (inchar == '|') {
						break;
					}
					sbuilder.append(inchar);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		myString = sbuilder.toString();
		String[] items = myString.split(",");

		ArrayList<String> theList = new ArrayList<String>();
		for (int x = 0; x < items.length; x++) {
			theList.add(items[x]);
		}
		return theList;
	}

	public void run() {

		boolean exit = false;
		
		for (;;) {

			SerialPort ports[] = SerialPort.getCommPorts();
			
			if (ports == null) {
				//no devices attached
				return;
			}

			if(exit)
			{
				return;
			}
			
			//Test each port to see if our Esp32 is on it
			for (int x = 0; x < ports.length; x++) {

				System.out.printf("Checking port %d", x);

				ports[x].openPort();
				ports[x].setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
				ports[x].setBaudRate(115200);

				//Create and clear inputstream data
				inData = ports[x].getInputStream();
				outData = ports[x].getOutputStream();

				//			try {
				//				while (ports[x].getInputStream().available() > 0) {
				//					ports[x].getInputStream().read();
				//				}
				//			} catch (IOException e2) {
				//				// TODO Auto-generated catch block
				//				e2.printStackTrace();
				//			}

				//Clear buffer

				try {
					while (inData.available() > 0) {
						System.out.println(inData.read());
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//Send a device info request
				//getBytes does NOT send \n so we must append.
				myString = "ISTHISESP32" + "|";

				try {
					outData.write(myString.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//Receive and build input into a string

				StringBuilder sbuilder = new StringBuilder();
				char inchar = 0;
				long timeOut = 0;

				while (ports[x].isOpen()) {

					try {
						if (inData.available() > 0) {
							inchar = (char) inData.read();
							if (inchar == '|') {
								break;
							}
							sbuilder.append(inchar);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timeOut++;
					if (timeOut > 1000) {
						break;
					}
				}
				myString = sbuilder.toString();
				if (myString.equals("YESESP32")) {
					//Confirmation of device received!
					System.out.println("esp32 found");
					esp32Port = ports[x];
					//shut down thread
					exit = true;
					break;

				} else {
					System.out.println("Not esp32 device!");
					ports[x].closePort();

				}

			}
		}

	}

	public SerialPort getEsp32Port() {
		return esp32Port;
	}

	public void setEsp32Port(SerialPort esp32Port) {
		this.esp32Port = esp32Port;
	}
}