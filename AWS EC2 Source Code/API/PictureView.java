import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class PictureView extends HttpServlet {
    private final String   defaultDirPath = "/home/ubuntu/iot_m/";
    private final String   imageTail = "/Snapshot/";
    private final String   videoTail = "/Video/";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        process(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        process(req, res);
    }

    public void process(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        //PrintWriter out = res.getWriter();
	ServletContext cxt = getServletContext();

        res.setContentType("text/html;charset=euc-kr");
        req.setCharacterEncoding("euc-kr");

        String id = req.getParameter("machine_id");
	String fileName = req.getParameter("file");
	String tag = req.getParameter("tag");
	String dirPath = defaultDirPath + id;
	if (tag.equals("I"))
		dirPath += imageTail;
	else if (tag.equals("V"))
		dirPath += videoTail;

	dirPath += fileName;

	File file = new File(dirPath);
	boolean exist = file.exists();

	if (exist)
	{
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream());

		int len;
		int size = 4096;
		byte[] data = new byte[size];
		while ((len = br.read(data)) != -1){
			bos.write(data, 0, len);
		}
		bos.flush();
		bos.close();
		br.close();
	}
	else
	{
		PrintWriter out = res.getWriter();
		out.println("File Not Exist");
		out.flush();
		out.close();
	}
    }

    public void destroy() {
        super.destroy();
    }
}
