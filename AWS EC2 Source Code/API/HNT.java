import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.simple.JSONObject;

public class HNT extends HttpServlet {
    private String   file;
    private long     today, yesterday, total;

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

        ServletContext cxt = getServletContext();

        res.setContentType("text/html;charset=euc-kr");
        req.setCharacterEncoding("euc-kr");

        PrintWriter out = res.getWriter();
        String name = req.getParameter("machine_id");

        file = "/home/ubuntu/iot_m/" + name + "/HNT/info.dat";

	BufferedReader br = new BufferedReader(new FileReader(file));
	float temp = Float.parseFloat(br.readLine());
	float humidity = Float.parseFloat(br.readLine());

        JSONObject object = new JSONObject();
	object.put("temperature", temp);
	object.put("humidity",humidity);
	
	out.print(object);
	out.flush();
	out.close();
    }

    public void destroy() {
        super.destroy();
    }
}
