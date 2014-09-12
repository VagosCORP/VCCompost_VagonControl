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

import javafx.application.Platform;
import javafx.concurrent.Task;
import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;
import vclibs.communication.Eventos.OnTimeOutListener;
import vclibs.communication.javafx.Comunic;
import vclibs.communication.javafx.TimeOut;

public class VagonControl extends Task<Integer> {

	Thread th, thto;
	Comunic comunic = null;
	TimeOut timeout = new TimeOut(2000);
	String tarea = "";
	String IP = "10.0.0.55";
	int Port = 2000;
	public float[] sen	= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	float[] sen0	= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	static final int ENCENDER = 111;
	static final int APAGAR = 112;
	static final int CONSULTAR = 113;
	static final int TEMP = 5;
	static final int BOMB = 3;
	static final int GUT_CON = 0;
	static final int GUT_OFF = 0;
	static final int GUT_ON = 1;
	static final int ERR_ON = 2;
	static final int ERR_OFF = 3;
	static final int ERR_COM = 4;
	static final int ERR_CON = 5;
	public static float ERROR = (float) 999.9999;
	public String linea = "";
	public String hora = "dd-MM-yyyy HH:mm:ss";
	SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
	Path pathRegistro;
	final String pathlogs = "C://VCCompost//log//";
	String pathbomblog = pathlogs + "bomblog.rep";
	String pathtemplog = pathlogs + "templog.rep";
	PrintWriter oStReg=null;

	TListener tListener;
	public interface TListener {
		public void OnInfoReceived(String txt);
		public void OnDataReceived(String txt);
	}
	public void setTListener(TListener listener) {
		tListener = listener;
	}

	public VagonControl(String BioRIP) {
		IP = BioRIP;
		BombaOff();
	}

	@Override
	protected Integer call() throws Exception {
		while(!isCancelled()) {
			Thread.sleep(60000);
			adquirir(1);
		}
		return null;
	}
	
	public void setIP(String newIP) {
		IP = newIP;
	}

	public int adquirir(int n250) {
		int res = 0;
		sen	= sen0;
		sen[0] = ERR_CON;
		if(comunic == null || comunic.estado != comunic.CONNECTED) {
			comunic = new Comunic(IP, Port);
			comunic.setConnectionListener(new OnConnectionListener() {
	
				@Override
				public void onConnectionstablished() {
					sen[0] = GUT_CON;
					enTimeOut(2000);
					writeilog("Conexión Establecida!", TEMP);
					if (n250 == 1) {
						comunic.enviar('A');
					} else {
						comunic.enviar('B');
						comunic.enviar(n250);
					}
				}
	
				@Override
				public void onConnectionfinished() {
					if(sen[0] == ERR_COM) {
						sen[0] = 0;
						writeilog("Error de Comunicacion", TEMP);
					}
					if(sen[0] == ERR_CON) {
						sen[0] = 0;
						writeilog("Error de Conexión", TEMP);
					}
				}
			});
			comunic.setComunicationListener(new OnComunicationListener() {
	
				@Override
				public void onDataReceived(String dato) {
					tarea += dato;
					if (dato.endsWith("/")) {
						timeout.cancel();
						comunic.Detener_Actividad();
						procesar(tarea, TEMP);
						tarea = "";
						String info =
								"\r\n     S1e = " + sen[1] + " S1i = " + sen[2] + " S2i = " + sen[3] + " S2e = " + sen[4] +
								"\r\n     Ext = " + sen[5] + " Med = " + sen[6] + " Tri = " + sen[7] + " Tre = " + sen[8];
						writelog(info, TEMP);
					}
				}
			});
			th = new Thread(comunic);
			th.setDaemon(true);
			th.start();
		}else {
			res = 1;
		}
		return res;
	}
	
	public int bomba(int accion) {
		int res = 0;
		sen	= sen0;
		sen[0] = ERR_CON;
		if(comunic == null || comunic.estado != comunic.CONNECTED) {
			comunic = new Comunic(IP, Port);
			comunic.setConnectionListener(new OnConnectionListener() {
				
				@Override
				public void onConnectionstablished() {
					sen[0] = GUT_CON;
					enTimeOut(2000);
					writeilog("Conexión Establecida!", BOMB);
					if(accion == ENCENDER) {
						writeilog("Solicitud de Encendido de Bomba", BOMB);
						comunic.enviar('V');
						timeout.cancel();
						comunic.Detener_Actividad();
					}else if(accion == APAGAR) {
						writeilog("Solicitud de Apagado de Bomba", BOMB);
						comunic.enviar('W');
						timeout.cancel();
						comunic.Detener_Actividad();
						
					}else if(accion == CONSULTAR) {
						writeilog("Solicitud de Estado de Bomba", BOMB);
						comunic.enviar('Y');
					}
				}
				
				@Override
				public void onConnectionfinished() {
					if(sen[0] == ERR_COM) {
						sen[0] = 0;
						writeilog("Error de Comunicacion", BOMB);
					}
					if(sen[0] == ERR_CON) {
						sen[0] = 0;
						writeilog("Error de Conexión", BOMB);
					}
				}
			});
			comunic.setComunicationListener(new OnComunicationListener() {
				
				@Override
				public void onDataReceived(String dato) {
					tarea += dato;
					if (dato.endsWith("/")) {
						timeout.cancel();
						comunic.Detener_Actividad();
						procesar(tarea, BOMB);
						tarea = "";
						String info = "";
						if(sen[9] == GUT_ON) {
							info = "Bomba Encendida";
						}else if(sen[9] == GUT_OFF) {
							info = "Bomba Apagada";
						}else if(sen[9] == ERR_ON) {
							info = "Error al Encender Bomba";
						}else if(sen[9] == ERR_OFF) {
							info = "Error al Apagar Bomba";
						}
						writeilog(info, BOMB);
					}
				}
			});
			th = new Thread(comunic);
			th.setDaemon(true);
			th.start();
		}else {
			res = 1;
		}
		return res;
	}
	
	void enTimeOut(int ms) {
		timeout = new TimeOut(ms);
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
				sen[0] = ERR_COM;
			}
		});
		thto = new Thread(timeout);
		thto.setDaemon(true);
		thto.start();
	}
	
	void procesar(String datos, int tip) {
		String[] p1 = datos.split("#");
		int f = p1.length;
		if (f < tip) {
			System.out.println("Algo salio mal");
		} else if (f == tip) {
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
	
	void writelog(String info, int tip) {
		hora = date.format(new GregorianCalendar().getTime()).toString();
		linea = hora + ":\r\n" + info + "\r\n";
		if (tListener != null)
			tListener.OnDataReceived(linea);
		writeilog(info, tip);
	}
	
	void writeilog(String info, int tip) {
		hora = date.format(new GregorianCalendar().getTime()).toString();
		linea = hora + ": " + info + "\r\n";
		System.out.println(linea);
		if (tListener != null)
			tListener.OnInfoReceived(linea);
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				if(Files.notExists(Paths.get(pathlogs))){
		            try {
		                Files.createFile(Paths.get(pathlogs));
		            } catch (IOException ex) {
		            	System.out.println("No se Pudo crear la ruta de registros");
		            }
		        }
				try {
				        Files.createFile(Paths.get(pathbomblog));
				        Files.createFile(Paths.get(pathtemplog));
				} catch (IOException e) {
				        System.out.println("No se Pudieron crear los archivos de registro");
				}
				if(tip == BOMB) {	
					try {
						oStReg=new PrintWriter(new FileWriter(pathbomblog,true));
						oStReg.println(linea);
						oStReg.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}else if(tip == TEMP) {
					try {
						oStReg=new PrintWriter(new FileWriter(pathtemplog,true));
						oStReg.println(linea);
						oStReg.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void BombaOn() {
		bomba(ENCENDER);
	}
	
	public void BombaOff() {
		bomba(APAGAR);
	}
	public void consultaBomba() {
		bomba(CONSULTAR);
	}
}