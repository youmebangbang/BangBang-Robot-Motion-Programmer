package BangBangMotion;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NewFileAlertBox {
	
	private String newFileName;

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}
	
	public void NewFileAlertBox() {
		newFileName = null;
		
	}

	public void display() {
		
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Create new file");
		window.setMinWidth(300);
		
		TextField inputText = new TextField("Enter new file name");
		Button okButton = new Button("OK");
		Button cancelButton = new Button("Cancel");
		
		okButton.setOnAction(e -> {
			newFileName = inputText.getText();
			window.close();			
		});
		
		cancelButton.setOnAction(e -> {			
			window.close();			
		});
		
		HBox layout = new HBox();
		layout.getChildren().addAll(inputText, okButton, cancelButton);
		Scene myScene = new Scene(layout, 300,100);
		
		myScene.getStylesheets().add(getClass().getResource("myStyle.css").toExternalForm());

		
		window.setScene(myScene);
		window.showAndWait();
		
		
	}
	
	
}
