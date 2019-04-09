import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.sql.*;

public class Login extends HttpServlet {

	private final String dbPath = "/home/ubuntu/iot_s/DB/iot_person.db";
	private final String STATE_SUCCESS = "200";
	private final String STATE_ID_NOT = "404";
	private final String STATE_NOT_SAME = "403";
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException{
        process(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        process(req, res);
    }

    public void process(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException{

	PrintWriter out = res.getWriter();
	ServletContext cxt = getServletContext();
	Connection connection = null;
	
	try {
		connection = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(10);

        	res.setContentType("text/html;charset=euc-kr");
	        req.setCharacterEncoding("euc-kr");

        String id = req.getParameter("id");
		String password = req.getParameter("password");
	
		ResultSet rs = statement.executeQuery("select * from info where id=\'" + id +"\';");
		if (rs.next()){
			String save_pwd = rs.getString("password");
			if (password.equals(save_pwd)){
				String machine_id = rs.getString("machine_id");
				out.println(machine_id);
			}
			else
				out.println(STATE_NOT_SAME);
			}
		else {
			out.println(STATE_ID_NOT);
		}
	} catch(SQLException e){
		out.println("No Hack");
	}

	out.flush();
	out.close();
    }

    public void destroy() {
        super.destroy();
    }
}
