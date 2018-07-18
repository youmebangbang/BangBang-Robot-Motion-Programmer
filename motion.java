package BangBangMotion;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

public class motion extends Application {
	private TableView<sdFileLine> myTable;
	private ArrayList<String> sdFiles;
	private String filesListSelected;
	//private int filesListSelectedIndex;
	private String myString;
	private ListView<String> filesList;
	private ListView<String> currentMotionSet;
	private ListView<String> currentMotionSetLabels;
	private usbConnect usbConnection;
	private Slider[] sliderArray;
	private sdFileLine copiedRow;

	public void myTableRowSelected(sdFileLine myLine) {
		//Table row was clicked so update servo values to motion set lists
		//Get the index of the selected line so we can update the values in the table later
		//filesListSelectedIndex = myTable.getSelectionModel().getSelectedIndex();
		String myString = myLine.getSdServoValues();
		String[] mySet = myString.split(",");

		//Update list of servo values
		currentMotionSet.getItems().clear();
		for (int x = 0; x < 16; x++) {

			currentMotionSet.getItems().add(mySet[x]);
			sliderArray[x].setValue(Integer.parseInt(mySet[x]));
		}

		//Update robot pose
		usbConnection.updateRobotPose(myString + ",");

	}

	public void myListItemSelected() {
		System.out.println("my list selected");
		filesListSelected = filesList.getSelectionModel().getSelectedItem();
		int index = filesList.getSelectionModel().getSelectedIndex();

		//fetch info from SD card
		String[] fileItems = usbConnection.getSdFile(filesListSelected);
		String servoValueTemp = "";
		String servoSpeedTemp = "";
		String servoHoldTemp = "";
		int lineNum = 0;

		//Parse file items into table view list

		myTable.getItems().clear();

		for (lineNum = 0; lineNum < (fileItems.length / 18); lineNum++) {
			servoValueTemp = fileItems[lineNum * 18];
			for (int x = 1; x < 16; x++) {
				servoValueTemp = servoValueTemp + "," + fileItems[x + (lineNum * 18)];
			}

			servoSpeedTemp = fileItems[16 + (lineNum * 18)];
			servoHoldTemp = fileItems[17 + (lineNum * 18)];

			sdFileLine myLine = new sdFileLine(servoValueTemp, servoSpeedTemp, servoHoldTemp);
			myTable.getItems().add(myLine);

		}

	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		/*
		ModelViewer modelViewer = new ModelViewer();
		MeshView[] meshArray = modelViewer.buildMesh();
		Group meshGroup = new Group();
		meshGroup.getChildren().addAll(meshArray);
		meshArray[0].setScaleX(4);
		meshArray[0].setScaleY(4);
		meshArray[0].setScaleZ(4);
		
		RotateTransition rotate = new RotateTransition(Duration.seconds(10), meshGroup);
		rotate.setAxis(Rotate.Z_AXIS);
		rotate.setFromAngle(0);
		rotate.setToAngle(360);
		rotate.setInterpolator(Interpolator.LINEAR);
		rotate.setCycleCount(RotateTransition.INDEFINITE);
		
		meshArray[0].setOnMouseDragged(e -> {
			meshArray[0].getTransforms().add(new Rotate(e.getX(), 0, 0, 0, Rotate.X_AXIS));
			meshArray[0].getTransforms().add(new Rotate(e.getY(), 0, 0, 0, Rotate.Z_AXIS));
		});
		*/

		primaryStage.setTitle("BangBang Robot Motion Programmer v1.0");
		primaryStage.setMinWidth(1300);
		primaryStage.setMinHeight(1000);

		Label headerText = new Label("::BangBang Robot\n::Motion Programmer\n::v1.0");
		headerText.getStyleClass().add("headerLabel");
		//setStyle("");

		Button connectEsp32 = new Button("Connect to esp32");
		Button playMotionSet = new Button("Play motion set");
		Button saveMotionSetRobot = new Button("Save motion set\nto robot");
		Button saveMotionSetPC = new Button("Save motion set\nto PC");
		Button makeNewFile = new Button("Create a\n new file");
		Button removeFile = new Button("Remove file");
		Button addRow = new Button("Add row above selected");
		Button deleteRow = new Button("delete selected row");
		Button updateServoValues = new Button("Update servo values");
		Button copySelectedRow = new Button("Copy row");
		Button pasteRow = new Button("Paste row");
		Button lockModelPos = new Button("Lock model position");
		lockModelPos.setDisable(true);

		CheckBox modelCheck = new CheckBox();
		Label modelCheckText = new Label("Enable motion model");
		modelCheckText.getStyleClass().add("checkBoxLabel");

		sliderArray = new Slider[16];

		for (int x = 0; x < 16; x++) {
			sliderArray[x] = new Slider();
			sliderArray[x].setMin(0);
			sliderArray[x].setMax(180);
			sliderArray[x].setShowTickLabels(false);
			sliderArray[x].setShowTickMarks(false);
			sliderArray[x].getStyleClass().add("sliders");
			final int index = x;

			sliderArray[x].valueProperty().addListener((ov, old_val, new_val) -> {

				//Update currentMotionSet list
				Integer intval = new_val.intValue();
				currentMotionSet.getItems().set(index, intval.toString());

				myString = "";

				for (int y = 0; y < 16; y++) {
					myString = myString + currentMotionSet.getItems().get(y) + ",";
				}

				usbConnection.updateRobotPose(myString);

			});

		}

		Timeline timeLine = new Timeline(new KeyFrame(Duration.seconds(.5), evt -> {
			connectEsp32.setStyle("-fx-background-color: rgb(255, 0, 0);");
		}), new KeyFrame(Duration.seconds(1), evt -> {
			connectEsp32.setStyle("-fx-background-color: #DFE7F1;");
		}));
		timeLine.setCycleCount(Animation.INDEFINITE);

		makeNewFile.setMinHeight(75);
		makeNewFile.setMinWidth(150);
		removeFile.setMinHeight(50);
		removeFile.setMinWidth(150);

		filesList = new ListView<String>();
		filesList.setPrefHeight(210);
		filesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		currentMotionSet = new ListView<String>();
		currentMotionSet.setMinHeight(480);
		currentMotionSet.setMaxWidth(100);
		currentMotionSet.setStyle("-fx-font-size: 15;");
		currentMotionSet.setEditable(true);
		currentMotionSet.setCellFactory(TextFieldListCell.forListView());

		currentMotionSetLabels = new ListView<String>();
		currentMotionSetLabels.setMinHeight(480);
		currentMotionSetLabels.setMaxWidth(100);
		currentMotionSetLabels.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
		currentMotionSetLabels.setEditable(false);

		currentMotionSetLabels.getItems().add("SERVO 0:");
		currentMotionSetLabels.getItems().add("SERVO 1:");
		currentMotionSetLabels.getItems().add("SERVO 2:");
		currentMotionSetLabels.getItems().add("SERVO 3:");
		currentMotionSetLabels.getItems().add("SERVO 4:");
		currentMotionSetLabels.getItems().add("SERVO 5:");
		currentMotionSetLabels.getItems().add("SERVO 6:");
		currentMotionSetLabels.getItems().add("SERVO 7:");
		currentMotionSetLabels.getItems().add("SERVO 8:");
		currentMotionSetLabels.getItems().add("SERVO 9:");
		currentMotionSetLabels.getItems().add("SERVO 10:");
		currentMotionSetLabels.getItems().add("SERVO 11:");
		currentMotionSetLabels.getItems().add("SERVO 12:");
		currentMotionSetLabels.getItems().add("SERVO 13:");
		currentMotionSetLabels.getItems().add("SERVO 14:");
		currentMotionSetLabels.getItems().add("SERVO 15:");

		// Set editing related event handlers (OnEditCommit)
		currentMotionSet.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
			@Override
			public void handle(ListView.EditEvent<String> t) {
				currentMotionSet.getItems().set(t.getIndex(), t.getNewValue());
				System.out.println("setOnEditCommit");
			}

		});

		currentMotionSet.setOnEditCancel(new EventHandler<ListView.EditEvent<String>>() {
			@Override
			public void handle(ListView.EditEvent<String> t) {
				System.out.println("setOnEditCancel");
			}
		});

		myTable = new TableView<>();
		myTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		myTable.setEditable(true);
		myTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		myTable.setMinWidth(800);
		myTable.setMaxHeight(300);
		TableColumn<sdFileLine, String> servoValue = new TableColumn<>("Servo Value");
		servoValue.setMinWidth(600);
		servoValue.setCellValueFactory(new PropertyValueFactory<sdFileLine, String>("sdServoValues"));
		servoValue.setCellFactory(TextFieldTableCell.<sdFileLine>forTableColumn());
		servoValue.setEditable(false);

		TableColumn<sdFileLine, String> servoSpeed = new TableColumn<>("Speed");
		servoSpeed.setMinWidth(50);
		servoSpeed.setCellValueFactory(new PropertyValueFactory<sdFileLine, String>("sdSpeed"));
		servoSpeed.setCellFactory(TextFieldTableCell.<sdFileLine>forTableColumn());

		// Set editing related event handlers (OnEditCommit)    
		servoSpeed.setOnEditCommit(new EventHandler<CellEditEvent<sdFileLine, String>>() {
			@Override
			public void handle(CellEditEvent<sdFileLine, String> t) {
				((sdFileLine) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setSdSpeed(t.getNewValue());
			}
		});

		TableColumn<sdFileLine, String> servoHold = new TableColumn<>("Hold");
		servoHold.setMinWidth(50);
		servoHold.setCellValueFactory(new PropertyValueFactory<sdFileLine, String>("sdHold"));
		servoHold.setCellFactory(TextFieldTableCell.<sdFileLine>forTableColumn());

		// Set editing related event handlers (OnEditCommit)        
		servoHold.setOnEditCommit(new EventHandler<CellEditEvent<sdFileLine, String>>() {
			@Override
			public void handle(CellEditEvent<sdFileLine, String> t) {
				((sdFileLine) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setSdHold(t.getNewValue());
			}
		});

		myTable.getColumns().addAll(servoValue, servoSpeed, servoHold);

		removeFile.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}

			usbConnection.removeFile(filesList.getSelectionModel().getSelectedItem());
			filesList.getItems().clear();
			sdFiles = usbConnection.getFileList();

			filesList.getItems().clear();
			for (int x = 0; x < sdFiles.size(); x++) {

				myString = (String) sdFiles.get(x);
				filesList.getItems().add(myString);
			}

		});

		lockModelPos.setOnAction(e -> {

			String servoVals = usbConnection.lockModelPosition();
			System.out.println("lock pos: " + servoVals);
			sdFileLine myLine = new sdFileLine(servoVals, "50", "0");
			myTable.getItems().add(myLine);
			String temp[] = servoVals.split(",");
			currentMotionSet.getItems().clear();
			for (int x = 0; x < 16; x++) {

				currentMotionSet.getItems().add(temp[x]);
				sliderArray[x].setValue(Integer.parseInt(temp[x]));

			}

		});

		modelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {

			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {

				if (usbConnection.getEsp32Port() == null) {
					timeLine.play();
					connectEsp32.setText("No devices connected!\nClick to try again");
					return;
				}

				System.out.println(modelCheck.isSelected());
				if (modelCheck.isSelected()) {
					usbConnection.sendModelOn();
					lockModelPos.setDisable(false);
				} else {
					usbConnection.sendModelOff();
					lockModelPos.setDisable(true);

				}

			}
		});

		//Create button action to copy selected row
		copySelectedRow.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}
			copiedRow = myTable.getSelectionModel().getSelectedItem();

		});

		//Create button action to paste copied row
		pasteRow.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}
			
			
			int index = myTable.getSelectionModel().getSelectedIndex();
			myTable.getItems().set(index, copiedRow);

		});

		//Create button action for updating servo values from currentMotionSet list
		updateServoValues.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}
			myString = "";
			//Build string to put back into table
			for (int x = 0; x < 15; x++) {
				myString = myString + currentMotionSet.getItems().get(x) + ",";
			}
			myString = myString + currentMotionSet.getItems().get(15);
			sdFileLine myLine = new sdFileLine(myString, "0", "0");

			myTable.getItems().set(myTable.getSelectionModel().getSelectedIndex(), myLine);

		});

		//Create button action for playing motion file 
		playMotionSet.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}
			if(modelCheck.isSelected()) {
				System.out.println("cant play motion, model on");
				return;
			}
			String fileOut = "";
			sdFileLine myLine;

			//Get all data from the table into a single string
			for (int x = 0; x < myTable.getItems().size(); x++) {

				myLine = myTable.getItems().get(x);
				fileOut = fileOut + myLine.getSdServoValues() + "," + myLine.getSdSpeed() + "," + myLine.getSdHold()
						+ ",";
			}
			System.out.println("Sending motion set: " + fileOut);
			usbConnection.sendMotionSet(fileOut);

		});

		//Create button action to add new row
		addRow.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}
			int index = myTable.getSelectionModel().getSelectedIndex();
			myTable.getItems().add(index, new sdFileLine("90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90", "50", "0"));

		});

		deleteRow.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}

			myTable.getItems().remove(myTable.getSelectionModel().getSelectedItem());
		});

		//Create button action to make a new file for editing
		makeNewFile.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}

			NewFileAlertBox alertBox = new NewFileAlertBox();
			alertBox.display();

			if (alertBox.getNewFileName() == null) {
				//alert box was cancelled
				return;
			}

			filesListSelected = "/" + alertBox.getNewFileName();

			usbConnection.createNewFile(filesListSelected);
			sdFiles = usbConnection.getFileList();

			filesList.getItems().clear();
			for (int x = 0; x < sdFiles.size(); x++) {

				myString = (String) sdFiles.get(x);
				filesList.getItems().add(myString);
			}

			//Add one working row to the table for the new file
			myTable.getItems().clear();
			myTable.getItems().add(new sdFileLine("90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90", "0", "0"));
		});

		//Create button action to save to robot SD
		saveMotionSetRobot.setOnAction(e -> {
			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}

			String fileOut = "";
			sdFileLine myLine;

			//Get all data from the table into a single string
			for (int x = 0; x < myTable.getItems().size(); x++) {

				myLine = myTable.getItems().get(x);
				fileOut = fileOut + myLine.getSdServoValues() + "," + myLine.getSdSpeed() + "," + myLine.getSdHold()
						+ ",";

			}
			usbConnection.saveFileRobot(filesListSelected, fileOut);

		});

		//Create button action to save to PC
		saveMotionSetPC.setOnAction(e -> {

			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");
				return;
			}

			String fileOut = "";
			sdFileLine myLine;
			File desktopDir = new File(System.getProperty("user.home"), "Desktop");

			//Get all data from the table into a single string
			for (int x = 0; x < myTable.getItems().size(); x++) {

				myLine = myTable.getItems().get(x);
				fileOut = fileOut + myLine.getSdServoValues() + "," + myLine.getSdSpeed() + "," + myLine.getSdHold()
						+ ",";
			}

			String pathToDesktop = desktopDir.getPath();

			try {
				FileOutputStream out = new FileOutputStream(new File(pathToDesktop + filesListSelected));
				PrintWriter pw = new PrintWriter(out);
				pw.print(fileOut);
				pw.close();

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		//Create button action to connect to esp32
		usbConnection = new usbConnect();

		connectEsp32.setOnAction(e -> {
			timeLine.stop();
			connectEsp32.setText("Connecting...");
			usbConnection.run();

			if (usbConnection.getEsp32Port() == null) {
				timeLine.play();
				connectEsp32.setText("No devices connected!\nClick to try again");

			} else {
				connectEsp32.setStyle("-fx-background-color: #DFE7F1;");
				connectEsp32.setText("Connection Success!");

				//retrieve the list of files from the SD card module
				sdFiles = usbConnection.getFileList();
				for (int x = 0; x < sdFiles.size(); x++) {

					myString = (String) sdFiles.get(x);
					filesList.getItems().add(myString);
				}
			}
		});

		//Handle double click event on list of files
		filesList.setOnMouseClicked(f -> {
			if (f.getButton() == MouseButton.PRIMARY && f.getClickCount() == 2) {
				myListItemSelected();
			}
		});

		//Handle double click event for table row
		myTable.setRowFactory(tv -> {
			TableRow<sdFileLine> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					sdFileLine rowData = row.getItem();
					System.out.println(
							rowData.getSdServoValues() + " " + rowData.getSdSpeed() + " " + rowData.getSdHold());
					myTableRowSelected(rowData);
				}
			});
			return row;
		});

		HBox top_layout = new HBox(20);
		VBox left_layout = new VBox(10);
		HBox center_layout = new HBox(5);
		HBox bottom_layout = new HBox(5);
		VBox right_layout = new VBox(5);
		HBox tableButton_layout = new HBox(5);
		VBox sliders_layout = new VBox(16);
		HBox copyPaste_layout = new HBox(5);
		HBox model_layout = new HBox(5);
		VBox bottomRight_layout = new VBox(20);

		model_layout.getChildren().addAll(modelCheckText, modelCheck);
		bottomRight_layout.getChildren().addAll(updateServoValues, model_layout, lockModelPos);

		tableButton_layout.getChildren().addAll(addRow, deleteRow);
		copyPaste_layout.getChildren().addAll(copySelectedRow, pasteRow);

		for (int x = 0; x < 16; x++) {
			sliders_layout.getChildren().add(sliderArray[x]);
		}

		right_layout.getChildren().addAll(playMotionSet, saveMotionSetRobot, saveMotionSetPC);
		right_layout.setAlignment(Pos.TOP_LEFT);
		right_layout.setMinWidth(100);

		left_layout.getChildren().addAll(makeNewFile, filesList, removeFile);
		left_layout.setPadding(new Insets(0, 10, 0, 10));
		left_layout.setMaxWidth(150);

		bottom_layout.getChildren().addAll(currentMotionSetLabels, currentMotionSet, sliders_layout,
				bottomRight_layout);

		center_layout.getChildren().addAll(myTable);
		center_layout.setAlignment(Pos.TOP_CENTER);

		top_layout.getChildren().addAll(headerText, connectEsp32);
		top_layout.setPadding(new Insets(20, 20, 20, 20));
		top_layout.setAlignment(Pos.CENTER_LEFT);

		GridPane gridpane = new GridPane();
		gridpane.setHgap(10);
		gridpane.setVgap(10);
		gridpane.add(top_layout, 0, 0, 4, 1);
		gridpane.add(left_layout, 0, 1, 1, 2);
		gridpane.add(myTable, 1, 1, 2, 1);
		gridpane.add(tableButton_layout, 1, 2);
		gridpane.add(right_layout, 4, 1);
		//gridpane.add(meshGroup, 0, 3, 2, 2);
		gridpane.add(copyPaste_layout, 2, 2);
		gridpane.add(bottom_layout, 2, 3, 3, 1);
		gridpane.getStyleClass().add("grid");
		Group myGroup = new Group();
		myGroup.getChildren().addAll(gridpane);
		myGroup.getStylesheets().add(getClass().getResource("myStyle.css").toExternalForm());
		Scene gridScene = new Scene(myGroup, 1350, 1000);

		primaryStage.setScene(gridScene);
		primaryStage.show();
	}
}