import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import org.json.simple.JSONObject;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@MultipartConfig
@WebServlet(name = "CropAndShare", urlPatterns = {"/uploadImage"})
public class CropAndShare extends HttpServlet {

    String uploadDestination = "/home/shubham/demo/";

    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            System.out.println("Servlet Hit!!!");
            MultipartParser mp = new MultipartParser(request, 100000000);
            Part p = mp.readNextPart();
            if (p.isFile()) {
                FilePart fp1 = (FilePart) p;
                InputStream is = fp1.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int c;
                while ((c = is.read()) != -1) {
                    out.write(c);
                }
                out.flush();
                out.close();
                byte[] data = out.toByteArray();
                System.out.println("data:"+fp1.getFileName()+":"+fp1.getContentType());
                writeBytesToFile(uploadDestination+fp1.getFileName(), data);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", true);

            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.println(jsonObject);

        } catch (Exception ex) {
            System.out.println("ex:" + ex);
        }
    }

    private static void writeBytesToFile(String fileOutput, byte[] bytes)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.write(bytes);
    }
}
