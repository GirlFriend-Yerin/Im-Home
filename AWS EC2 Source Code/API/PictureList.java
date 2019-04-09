import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class PictureList extends HttpServlet {
    private final String   extension = ".jpg";
    private final int      pageCount = 30;
    private final String   defaultDirPath = "/home/ubuntu/iot_m/";
    private final String   dirTail = "/Snapshot";

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

        PrintWriter out = res.getWriter();
	ServletContext cxt = getServletContext();

        res.setContentType("text/html;charset=euc-kr");
        req.setCharacterEncoding("euc-kr");

        String id = req.getParameter("machine_id");
	int page = Integer.parseInt(req.getParameter("page")); 
	String dirPath = defaultDirPath + id + dirTail;

	File file = new File(dirPath);
	boolean exist = file.exists();

	JSONObject object = new JSONObject();
	object.put("exist", exist);

	if (exist){
		JSONArray fileList = new JSONArray();
		File[] files = file.listFiles((dir, name) -> name.endsWith(extension));

		if ( (page+1) * pageCount > files.length )
		{
			for (int i = page*pageCount ; i < files.length; i++)
				fileList.add(files[i].getName());
		}
		else
		{
			for (int i = page *pageCount ; i < (page + 1) * pageCount ; i++)
				fileList.add(files[i].getName());
		}
		object.put("List", fileList);
	}

	out.println(object.toJSONString());
	out.flush();
	out.close();
    }

    public void destroy() {
        super.destroy();
    }
}
