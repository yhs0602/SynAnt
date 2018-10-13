package com.kyunggi.symant;
/*
 본문동의어찾기 프로그램
 0.본문텍스트를 파싱하여 영어단어만 모은후 set에 담는다.
 모든 단어들에대해{
 1.java HTTP로 다음이나 네이버사전에 쿼리를 날린다.
 2.결과를 받아 파싱하여 동반의어와 뜻을 추출한다.
 3.이결과를 가공하여 표시한다.
 }
 4.결과를취합하여 export

 */
//메모리는 얼마나먹을까

import android.os.*;
import android.util.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.regex.*;


public class RunnerThread extends Thread
{
	String _text;
	MainActivity mainActivity;

	private String TAG="SynAnt runner";
	public RunnerThread(MainActivity c, String __text)
	{
		_text = __text;
		mainActivity = c;
	}

	@Override
	public void run()
	{
		// TODO: Implement this method
		super.run();
		Run(_text);
	}
	Set<String> words;
	ArrayList<Result> results;
	int Run(String text)
	{
		//handler.sendEmptyMessage(0);
		words = ParseBook(text);
		mainActivity.OnFinishParse(words.size());
		results = new ArrayList<Result>();
		if (mainActivity.btest)
		{
			try
			{
				//String response=Query(q);
				Result result=SoupTest();//ParseResponse(response);
				results.add(result);
			}
			catch (Exception e)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream pinrtStream = new PrintStream(out);
				//e.printStackTrace()하면 System.out에 찍는데,
				// 출력할 PrintStream을 생성해서 건네 준다
				e.printStackTrace(pinrtStream);
				String stackTraceString = out.toString(); // 찍은 값을 가져오고.
				message = stackTraceString; //Toast.makeText(mainActivity, stackTraceString, 10).show();//보여 준다
				handler.sendEmptyMessage(0);
				Log.e(TAG,"",e);
			}
		}
		else
		{
			int i=0;
			for(String w:words)
			{
				i++;
				Log.v(TAG,"processing word:"+w);
				mainActivity.OnProgress(i,words.size());
				try
				{
					//String response=Query(q);
					Result result=QueryBySoup(w);//ParseResponse(response);
					results.add(result);
				}
				catch (Exception e)
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintStream printStream = new PrintStream(out);
					e.printStackTrace(printStream);
					String stackTraceString = out.toString();
					message = stackTraceString; //Toast.makeText(mainActivity, stackTraceString, 10).show();//보여 준다
					//handler.sendEmptyMessage(0);
					Log.e(TAG,"",e);
				}

			}
		}
		mainActivity.OnFinish();
		return 0;
	}

	private RunnerThread.Result SoupTest()
	{
		// TODO: Implement this method
		Result r=new Result();
		try
		{
			File file=new File("/storage/emulated/0/happy.html","");
			Document doc=Jsoup.parse(file,"UTF-8");
			//Elements synonyms=doc.select(".synonyms");
			Elements elems=doc.select(".synonyms");
			Element mean=doc.select(".mean").first();
			String syns=elems.text();
			syns=syns.replaceAll("유의어","");
			r.word = "happy";
			r.kor = mean.text().replaceAll("happy","");
			r.syns = new String[1];
			r.syns[0] = syns;
			//Elements ewords=synonyms.select("a[href]");
			/*
			for(Element e:synonyms)
			{
				Elements ewords=e.select("a[href]");
				
				r.word=ewords.toString();
			}
			//r.word = ;
			r.kor = "";
			r.syns = new String[1];
			r.syns[0] = doc.text();
			*/
		}
		catch (IOException e)
		{
			//Toast.makeText(mainActivity,
			message = "엥?";//,1).show();
			handler.sendEmptyMessage(0);
			Log.e(TAG,"",e);
		}
		return r;
	}

	private RunnerThread.Result QueryBySoup(String w)
	{
		// TODO: Implement this method
		String q=CreateQueryString(w);
		Result r=new Result();
		try
		{
			Document doc=Jsoup.connect(q).get();
			//Log.v(TAG,"doc="+doc.html());
			//Elements elems=doc.select(".");
			//Log.v(TAG,"elems="+elems.text());
			//Element mean=doc.select(".mean").first();
			//Log.v(TAG,"mean="+mean.text());
			//String syns=elems.html();
			//Log.v(TAG,"syns="+syns);
			//syns=syns.replaceAll("유의어","");
			r.word = w;
			r.kor ="";// mean.text().replaceAll(w,"");
			r.syns = new String[1];
			r.syns[0] ="";// syns;
			
			String str=doc.text();
			Log.v(TAG,str);
			str=str.replaceAll("예문 TTS 발음듣기","");
			String [] toks=str.split(Pattern.quote("[유의어]"));
			int len=toks.length;
			if(len>2)
			{
				r.syns=new String[len-2];
				System.arraycopy(toks,1,r.syns,0,len-2);
			}
		}
		catch (IOException e)
		{
			//Toast.makeText(mainActivity,
			message = "네트워크 상태를 확인해 주세요.";//,1).show();
			handler.sendEmptyMessage(0);
			Log.e(TAG,"",e);
		}

		return r;
	}

	Set<String> ParseBook(String text)
	{
		Set<String> results=new LinkedHashSet<String>();
		String[] words=text.split(" ");
		for (String w:words)
		{
			if (IsEnglish(w))
			{
				w = NormalizeWord(w);
				results.add(w);
			}
		}
		return results;
	}
	boolean IsEnglish(String word)
	{
		/*
		int len=word.length();
		int digits=0;
		for (int i=0;i < len;++i)
		{
			char c=word.charAt(i);
			if (Character.isDigit(c))
			{
				digits++;
			}
			else if (!Character.isLetter((c)))
			{
				if (new String("-.,!?;_~*#%()\"<>:&'/[]${}+=￦※").indexOf(c) == -1)
				{
					return false;
				}
			}
		}
		if (digits == len)return false;
		*/
		//return word.matches("")
		return true;
	}
	String NormalizeWord(String w)
	{
		w=w.replaceAll("[^a-zA-Z]","");
		return w.toLowerCase();
	}
	String CreateQueryString(String w)
	{
		return new String("https://endic.naver.com/search.nhn?query=") + w + "&searchOption=thesaurus";
	}
	class Result
	{
		public String word,normalized,kor;
		public String[] syns;
		public String[] kors;
		public Result()
		{

		}
	}
	/*
	 Result ParseResponse(String response)
	 {
	 return ;
	 }*/
 	String Query(String q) throws Exception
	{
        // TODO Auto-generated method stub

        // 한글의 경우 인코딩을 해야함.
        // 서버쪽에서는 따로 decode할 필욘 없음. 대신 new String(str.getBytes("8859_1"), "UTF-8");로 인코딩을 변경해야함
		// String str = URLEncoder.encode("한글", "UTF-8");

        URL url = new URL(q);//"http://localhost:8080/XmlTest/index.jsp");
        // open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		//  conn.setDoInput(true);            // 입력스트림 사용여부
		//  conn.setDoOutput(true);            // 출력스트림 사용여부
        conn.setUseCaches(false);        // 캐시사용 여부
        conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
        conn.setRequestMethod("GET");  // or GET
		/*
		 // Post로 Request하기
		 OutputStream os = conn.getOutputStream();
		 OutputStreamWriter writer = new OutputStreamWriter(os);
		 writer.write("title="+str);
		 writer.write("&subTitle="+str+"2");
		 writer.close();
		 os.close();
		 */
        // Response받기
		StringBuffer sb =  new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        for (;;)
		{
			String line =  br.readLine();
			if (line == null) break;
			sb.append(line + "\n");
        }

        br.close();
        conn.disconnect();

        String response = sb.toString();
     	return response;        
    }
	String message;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg)
		{
			Toast.makeText(mainActivity, message, 1).show();
			super.handleMessage(msg);
		}
	};
}
