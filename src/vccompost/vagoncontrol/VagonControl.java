package vccompost.vagoncontrol;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;
import vclibs.communication.Eventos.OnTimeOutListener;
import vclibs.communication.javafx.Comunic;
import vclibs.communication.javafx.TimeOut;

public class VagonControl extends Task<Integer> {

	Thread th, thto;
	Comunic comunic;
	Timeline timer;
	TimeOut timeout;
	boolean bombear = false;
	String tarea = "";
	public float[] sen	= { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	float[] sen0		= { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	public static float ERROR = (float) 23.222;
	public String hora = "dd-MM-yyyy HH:mm:ss";
	SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
	private Path pathRegistro;
	PrintWriter oStReg=null;

	TListener tListener;
	public interface TListener {
		public void OnDataReceived();
	}
	public void setTListener(TListener listener) {
		tListener = listener;
	}

	public VagonControl() {
		BombaOff();
	}
	
	public void protocolo() {
		timer = new Timeline(new KeyFrame(Duration.minutes(1),
				new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
							adquirir(1);
					}
				}));
		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();
	}

	@Override
	protected Integer call() throws Exception {
		adquirir(1);
		protocolo();
		return null;
	}
	
	public void adquirir(int n250) {
		comunic = new Comunic("20.0.0.6", 2000);
//		comunic.debug = false;
//		comunic.idebug = false;
//		comunic.edebug = false;
		comunic.ecom = false;//no palabra de finalización
		comunic.setConnectionListener(new OnConnectionListener() {

			@Override
			public void onConnectionstablished() {
				timeout = new TimeOut(2000);
//				timeout.idebug = false;
//				timeout.edebug = false;
				timeout.setTimeOutListener(new OnTimeOutListener() {

					@Override
					public void onTimeOutEnabled() {

					}

					@Override
					public void onTimeOutCancelled() {
						
					}

					@Override
					public void onTimeOut() {
						comunic.Detener_Actividad();
					}
				});
				thto = new Thread(timeout);
				thto.setDaemon(true);
				thto.start();
				if (n250 == 1) {
					comunic.enviar('A');
				} else {
					comunic.enviar('B');
					comunic.enviar(n250);
				}
			}

			@Override
			public void onConnectionfinished() {

			}
		});
		comunic.setComunicationListener(new OnComunicationListener() {

			@Override
			public void onDataReceived(String dato) {
				tarea += dato;
				if (dato.endsWith("/")) {
					timeout.cancel();
					comunic.Detener_Actividad();
					procesar(tarea);
					tarea = "";
					hora = date.format(new GregorianCalendar().getTime()).toString();
					pathRegistro=Paths.get("D://Documents//Compost//log");
					if(Files.notExists(pathRegistro)){
		                try {
		                    Files.createFile(pathRegistro);
		                } catch (IOException ex) {
		                    
		                }
		            }
					System.out.println(
							"hora=" + hora  +
							" S1e=" + sen[1] +
							" S1i=" + sen[2] +
							" S2i=" + sen[3] +
							" S2e=" + sen[4] +
							" Ext=" + sen[5] +
							" Med=" + sen[6] +
							" Tri=" + sen[7] +
							" Tre=" + sen[8]);
					if (tListener != null)
						tListener.OnDataReceived();
					try {
						oStReg=new PrintWriter(new FileWriter("D://Documents//Compost//log//templog.rep",true));
						oStReg.println("hora=" + hora  +
								" S1e=" + sen[1] +
								" S1i=" + sen[2] +
								" S2i=" + sen[3] +
								" S2e=" + sen[4] +
								" Ext=" + sen[5] +
								" Med=" + sen[6] +
								" Tri=" + sen[7] +
								" Tre=" + sen[8]);
						oStReg.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		});
		th = new Thread(comunic);
		th.setDaemon(true);
		th.start();
	}
	
	void procesar(String datos) {
		String[] p1 = datos.split("#");
		int f = p1.length;
		if (f <= 4) {
			System.out.println("Algo salio mal");
		} else if (f == 5) {
			String[] vals = p1[1].split("&");
			int l = vals.length;
			int nsen = 0;
			float vsen = 0;
			for (int i = 0; i < l; i++) {
				String[] ap = vals[i].split(";");
				if (ap.length == 3) {
					nsen = Integer.parseInt(ap[1]);
					try {
						vsen = Float.parseFloat(ap[2]);
					} catch (Exception e) {
						vsen = ERROR;
						System.out.println("Error en Sensor " + nsen);
					}
					sen[nsen] = vsen;
				}
			}
		}
	}
	
	void Bomb() {
		if(comunic.estado != comunic.CONNECTED) {
			comunic = new Comunic("20.0.0.6", 2000);
			comunic.debug = false;
			comunic.edebug = false;
			comunic.ecom = false;
			comunic.setConnectionListener(new OnConnectionListener() {
				
				@Override
				public void onConnectionstablished() {
					if(bombear)
						comunic.enviar('X');
					else
						comunic.enviar('Y');
				}
				
				@Override
				public void onConnectionfinished() {
					
				}
			});
			
		}
	}
	
	void BombaOn() {
		bombear = true;
//		Bomb();
	}
	
	void BombaOff() {
		bombear = false;
//		Bomb();
	}
}