import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Counter extends HttpServlet {
	private String   file;
	private long     today, yesterday, total;
	private String   msgToday = "오늘";
	private String   msgYesterday = "어제";
	private String   msgTotal = "전체";
	
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		ServletContext cxt = getServletContext();
		file = getInitParameter("file");
		if(file == null) {
			file = "count.dat";
		}
		file = cxt.getRealPath(file);
		load();
		TimerTask cron = new TimerTask() {			
			public void run() {
				yesterday = today;
				today = 0;
			}			
		};
		Timer timer = new Timer();
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		timer.schedule(cron, date.getTime(), 1000*60*60*24);
	}
	
	public void service(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException {
		
		res.setContentType("text/html;charset=KSC5601");
		PrintWriter out = res.getWriter();
		today++;
		total++;
		String d = "<div class='line'>";
		String e = "</div>";
		out.println(d + msgToday + " : " + today + e);
		out.println(d + msgYesterday + " : " + yesterday + e);
		out.println(d + msgTotal + " : " + total + e);
	}
	
	public void destroy() {
		save();
		super.destroy();		
	}

	protected void load() {
		try {
			BufferedReader br= new BufferedReader(new FileReader(file));
			String rl;
			int index = 0;
			while((rl=br.readLine()) != null) {
				if(rl.trim().length() == 0)
					continue;
					
				StringTokenizer st = new StringTokenizer(rl,":",false);
				String key = st.nextToken();
				long value = Long.parseLong(st.nextToken().trim());
				if(index == 0) {
					today = value;
					msgToday = key;
				} else if(index == 1) {
					yesterday = value;
					msgYesterday = key;						
				} else {
					total = value;
					msgTotal = key;						
				}
				index++;
			}
			br.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}		
	}
	
	protected void save() {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			out.println(msgToday + ":" + today);
			out.println(msgYesterday + ":" + yesterday);
			out.println(msgTotal + ":" + total);
			out.close();
		} catch(Exception e) { 
			e.printStackTrace();
		}
	}
}
