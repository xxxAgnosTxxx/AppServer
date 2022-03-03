import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.zip.*;

public class ArchiveServer {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8001);
        System.out.println("The server started");
        int i=0;

        while (true) {
            Socket client = server.accept();
            i++;

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            DataOutputStream fileOut = new DataOutputStream(client.getOutputStream());

            String folder =  reader.readLine();
            System.out.print("The client #"+i+": "+folder);

            File directory = new File(folder);
            if(!directory.exists()){
                writer.write("The folder '"+folder+"' not found.\n");
                System.out.println(" - uncorrectly way");
                writer.flush();
            }else{
                writer.write("The folder '"+folder+"' was found and became to archivation.\n");
                System.out.println(" - starting archivation.");
                writer.flush();

                folderToZip(folder);
                directory=new File(directory+".zip");
                sendSize(fileOut, directory);
                sendFile(fileOut, directory);
                System.out.println("Sending successfully finishing.");
            }
            directory.delete();

            fileOut.close();
            reader.close();
            writer.close();
            client.close();
        }
    }

    //создание общего Zip файла
   public static void folderToZip(String folder) throws Exception {
        FileOutputStream file = new FileOutputStream(folder+".zip");
        ZipOutputStream zipFile = new ZipOutputStream(file);
        File carrier = new File(folder);
        addFromFolder(zipFile, carrier);
        System.out.println("Archivation successfully finishing. Sending started.");
        zipFile.flush();
        zipFile.close();
    }

    //создание иерархии Zip-каталога
    private static void addFromFolder(ZipOutputStream zipFile, File carrier) throws Exception {
        File[] files = carrier.listFiles();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                addFromFolder(zipFile, files[i]);
                continue;
            }
            FileInputStream fis = new FileInputStream(files[i]);
            zipFile.putNextEntry(new ZipEntry(files[i].getPath()));
            byte[] buffer = new byte[4048];
            int length= fis.read(buffer);
            while(fis.read(buffer)>0)  zipFile.write(buffer, 0, length);
            zipFile.closeEntry();
            fis.close();
        }
    }

    //отправка размера архива
    public static void sendSize(DataOutputStream fileOut, File directory) throws IOException {
        long fileSize = directory.length();
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(fileSize);
        fileOut.write(buf.array());
        fileOut.flush();
    }

    //отправка архива
    public static void sendFile(DataOutputStream fileOut, File directory) throws IOException {
        FileInputStream fileIn = new FileInputStream(directory);
        while (true) {
            byte[] buffer = new byte[4096];
            int tmp = fileIn.read(buffer);
            if(tmp == -1) {
                break;
            }
            fileOut.write(buffer, 0, tmp);
            fileOut.flush();
        }
        fileIn.close();
    }
}