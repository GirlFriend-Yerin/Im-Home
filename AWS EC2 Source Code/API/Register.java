import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.sql.*;

public class Register extends HttpServlet {

	private final String dbPath = "/home/ubuntu/iot_s/DB/iot_person.db";
	private final String filter = "[0-9|a-z|A-Z|_|]*";
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
		String machine_id = req.getParameter("machine_id");

		if (!id.matches(filter) || !password.matches(filter))
		{
			out.println("No Hack");
		}
		else if (machine_id.isEmpty()){
			out.println("Fail");
		}
		else
		{
			String sql = "insert into info values(\'" + id + "\', \'" + password + "\', \'" + machine_id + "\');";
			statement.executeUpdate(sql);
			out.println("Success");
		}
	
	} catch(SQLException e){
		out.println("Query Error");
	}

	out.flush();
	out.close();
    }

    public void destroy() {
        super.destroy();
    }
}
