import java.util.Scanner;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.sql.*;

public class CheckDuplicate extends HttpServlet {

	private final String dbPath = "/home/ubuntu/iot_s/DB/iot_person.db";
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
	
		ResultSet rs = statement.executeQuery("select id from info where id=\'" + id +"\';");
		if (rs.next()){
			out.println("true");
		}
		else{
			out.println("false");
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
