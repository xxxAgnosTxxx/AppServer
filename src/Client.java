import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.LocalDate;

public class Client {
    public static void main(String[] args) throws IOException{
        Socket server = new Socket("127.0.0.1", 8001);

        BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        DataInputStream fileIn = new DataInputStream(server.getInputStream());
        //запрашиваем папку
        String name = "e:/книги";
        writer.write(name+"\n");
        writer.flush();
        System.out.println(reader.readLine());
        //принимаем инфу о размере
        byte[] fileSizeBuf = new byte[8];
        fileIn.read(fileSizeBuf);
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.put(fileSizeBuf);
        buf.flip();
        long fileSize = buf.getLong();
        System.out.println("Size of file: "+fileSize/1048576+"MB");
        //прием файла
        LocalDate id = LocalDate.now();
        int idmath = (int) (Math.random()*999);
        FileOutputStream fileOut = new FileOutputStream(name+"."+id+"."+idmath+".zip");
        int i = 0, tmp;
        byte[] data = new byte[4096];
        while (i < fileSize) {
            tmp = fileIn.read(data,0,(fileSize-i<data.length) ? (int)(fileSize-i) : data.length);
            i += tmp;
            fileOut.write(data, 0, tmp);
            fileOut.flush();
        }

        fileOut.close();
        fileIn.close();
        reader.close();
        writer.close();
        server.close();
    }
}