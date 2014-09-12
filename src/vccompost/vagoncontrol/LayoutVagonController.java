package vccompost.vagoncontrol;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import vccompost.vagoncontrol.VagonControl.TListener;

public class LayoutVagonController implements Initializable {
	
	VagonControl vControl;
	Thread th;
	boolean ti = false;
	
	ObservableList<Float> itemss1 = FXCollections.observableArrayList();
	ObservableList<Float> itemss2 = FXCollections.observableArrayList();
	ObservableList<Float> itemss3 = FXCollections.observableArrayList();
	ObservableList<Float> itemss4 = FXCollections.observableArrayList();
	ObservableList<Float> itemss5 = FXCollections.observableArrayList();
	ObservableList<Float> itemss6 = FXCollections.observableArrayList();
	ObservableList<Float> itemss7 = FXCollections.observableArrayList();
	ObservableList<Float> itemss8 = FXCollections.observableArrayList();
	ObservableList<String> itemsh = FXCollections.observableArrayList();
	
	@FXML Button cTemp;
	@FXML TextArea text;
	@FXML Label Label1;
	@FXML Label lblBomba;
	@FXML Button iBomba;
	@FXML Button aBomba;
	@FXML ListView<Float> list1;
	@FXML ListView<Float> list2;
	@FXML ListView<Float> list3;
	@FXML ListView<Float> list4;
	@FXML ListView<Float> list5;
	@FXML ListView<Float> list6;
	@FXML ListView<Float> list7;
	@FXML ListView<Float> list8;
	@FXML ListView<String> horas;
	
	@FXML public void tempClick() {
		vControl.adquirir(1);
	}
	
	@FXML public void iBombClick() {
		lblBomba.setText("Bomba Encendida");
		vControl.BombaOn();
		
	}
	
	@FXML public void aBombClick() {
		lblBomba.setText("Bomba Apagada");
		vControl.BombaOff();
	}
	
	void initMu() {
		ti = true;
		text.appendText("Adquisicion Iniciada!\r\n");
		vControl = new VagonControl("10.0.0.55");
		vControl.setTListener(new TListener() {

			@Override
			public void OnDataReceived(String linea) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						itemss1.add(vControl.sen[1]);
						itemss2.add(vControl.sen[2]);
						itemss3.add(vControl.sen[3]);
						itemss4.add(vControl.sen[4]);
						itemss5.add(vControl.sen[5]);
						itemss6.add(vControl.sen[6]);
						itemss7.add(vControl.sen[7]);
						itemss8.add(vControl.sen[8]);
						itemsh.add(vControl.hora);
						list1.setItems(itemss1);
						list2.setItems(itemss2);
						list3.setItems(itemss3);
						list4.setItems(itemss4);
						list5.setItems(itemss5);
						list6.setItems(itemss6);
						list7.setItems(itemss7);
						list8.setItems(itemss8);
						horas.setItems(itemsh);
						
					}
				});
			}

			@Override
			public void OnInfoReceived(String linea) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						text.appendText(linea);
					}
				});
			}		
		});
		th = new Thread(vControl);
		th.setDaemon(true);
		th.start();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initMu();
	}
}
