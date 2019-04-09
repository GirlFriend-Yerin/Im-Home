import java.io.*;
import java.net.*;

public class IotServer{
	public static void main(String[] args) throws Exception{
		int port = 8111;
		int timeout = 5000;
		final String[] tags = { "machine_id", "data_type", "humidity", "temperature"};
		final String DATA_TYPE_INFO = "Info";
		final String DATA_TYPE_PICTURE = "Picture";
		final String DATA_TYPE_VIDEO = "Video";
		final String FilePathHead = "/home/ubuntu/iot_m/";
		final String FileInfoTail = "/HNT/info.dat";
		final String FilePictureTail = "/Snapshot/";
		final String FileVideoTail = "/Video/";

		ServerSocket server = new ServerSocket(port);

		try{
			System.out.println("Server Open : Port" + port);

			while(true){
				Socket socket = server.accept();
				
				BufferedOutputStream bos = null;
				DataInputStream dis = null;

				try{
					socket.setSoTimeout(timeout);
					System.out.println("Connect : " + socket.getInetAddress() + " : " + socket.getPort());

					dis = new DataInputStream(socket.getInputStream());

					String type = dis.readLine();
					String machineID = dis.readLine();
					type = type.substring(type.indexOf(':') + 1);
					machineID = machineID.substring(machineID.indexOf(':') + 1);
					System.out.println(type + " " + machineID);

					if (type.equals(DATA_TYPE_INFO)){
					
						File file = new File(FilePathHead + machineID + FileInfoTail);
						PrintWriter pw = new PrintWriter(new BufferedWriter (new FileWriter(file)));

						String temperature = dis.readLine();
						String humidity = dis.readLine();
						System.out.println(temperature + ", " + humidity);
						temperature = temperature.substring(temperature.indexOf(':') + 1);
						humidity = humidity.substring(humidity.indexOf(':') + 1);

						pw.println(temperature);
						pw.println(humidity);

						pw.close();
					}
					else{
						System.out.println("Picture In");
						String fileName = dis.readLine();
						String fileSize = dis.readLine();

						System.out.println(fileName + " / " + fileSize + " Bytes" );

						fileName = fileName.substring(fileName.indexOf(':') + 1);
						fileSize = fileSize.substring(fileSize.indexOf(':') + 1);

						String filePath = FilePathHead + machineID;
					       	if (type.equals(DATA_TYPE_PICTURE))
							filePath += FilePictureTail + fileName;
						else
							filePath += FileVideoTail + fileName;

						System.out.println(filePath);

						File file = new File(filePath);
						bos =  new BufferedOutputStream(new FileOutputStream(file));
						
						int size = Integer.parseInt(fileSize);
						byte[] data = new byte[size];
						dis.readFully(data);
						bos.write(data);
						bos.flush();
						bos.close();
						System.out.println("Success : " + fileName + " " + file.length()/1024 + " KB ");

					}
				
					PrintWriter pw = new PrintWriter(socket.getOutputStream());	
					pw.println("Over");
					pw.flush();
					System.out.println("Connection Over");
				} catch (FileNotFoundException e){
					System.out.println("illigal File Path exist");	break;
				} catch (SocketException e){
					System.out.println("Connection Reset");
				} catch (SocketTimeoutException e){
					System.out.println("Connection Timeout");
				} catch (NullPointerException e){
					e.printStackTrace();
					System.out.println("Data is Drooped");
				} catch (IOException e){
					System.out.println("Parameter is Dropped");
				} finally{
					socket.close();
				}
			}
		} finally {
			server.close();
		}
	}

	public static void writeInfo (String machine_id, String humidity, String temperature)
	{
		final String path = "../iot_m/" + machine_id + "/HNT/info.dat";
		FileWriter fw = null;
		try{
			fw = new FileWriter(new File(path));

			fw.write(temperature + "\r\n" + humidity);
			
			fw.flush();
			fw.close();
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			if (fw != null) try { fw.close(); } catch(IOException e) {}
		}
	}
}
