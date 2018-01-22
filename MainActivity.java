package com.kyunggi.symant;

import android.app.*;

import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class MainActivity extends Activity implements OnClickListener
{

	public boolean btest;

	@Override
	public void onClick(View p1)
	{
		if (!(p1 instanceof Button))return;
		Button b=(Button)p1;
		if (b == startButton)
		{
			// TODO: Implement this method
			if (isDoing == true)
			{
				Toast.makeText(this, "이미 하는 중입니다.", 1).show();
				return;
			}
			String t=inputTextEdit.getText().toString();
			if (t.length() == 0 || t.compareToIgnoreCase("본문을 입력하세요") == 0)
			{
				Toast.makeText(this, "본문을 입력하세요.", 1).show();
				return;
			}
			ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null)
			{
				if (networkInfo.isConnected() == false)
				{
					Toast.makeText(this, "네트워크 연결을 확인하세요.", 1).show();

					return;

				}
				else
				{
					if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
					{
						Toast.makeText(this, "데이터 요금이 발생할 수 있습니다.", 1).show();

					}
				}
				//Toast.makeText(this, "네트워크 연결을 확인하세요.", 1).show();

			}
			else
			{
				Toast.makeText(this, "네트워크 연결을 확인하세요.", 1).show();
				return;
				//Toast.makeText(this, "테스트.", 1).show();
				//btest = true;
			}


			Toast.makeText(this, "시작됨.", 1).show();
			try
			{
				isDoing = true;
				runner = new RunnerThread(this, t);
				runner.start();
			}
			catch (Exception e)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream pinrtStream = new PrintStream(out);
				//e.printStackTrace()하면 System.out에 찍는데,
				// 출력할 PrintStream을 생성해서 건네 준다
				e.printStackTrace(pinrtStream);
				String stackTraceString = out.toString(); // 찍은 값을 가져오고.
				Toast.makeText(this, stackTraceString, 2).show();//보여 준다

			}
		}
		else if (b == exportButton)
		{
			if (isDoing == true || isDone == false)
			{
				Toast.makeText(this, "아직 완료되지 않았습니다.", 1).show();
			}
			if (!isExternalStorageWritable())
			{
				Toast.makeText(this, "파일 기록에 문제가 있습니다.", 2).show();
				return;
			}
			long now = System.currentTimeMillis();
			//	Step2. Date 생성하기
			Date date = new Date(now);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd-hh-mm");
			String getTime = sdf.format(date);
			//	출처: http://liveonthekeyboard.tistory.com/129 [키위남]
			String exportstr=resultTextEdit.getText().toString();
			exportstr=exportstr.replaceAll(",",".");
			exportstr=exportstr.replace(":",",");
			exportstr=exportstr.replaceAll("  ", ",");
			
			String fnam;
			File file = new File(fnam=new String("/storage/emulated/0/synant_" + getTime + ".csv"));
			FileWriter fw = null ;
			//String text = "This is TEST string." ;

			try
			{
				// open file.
				fw = new FileWriter(file) ;

				// write file.
				fw.write(exportstr) ;

			}
			catch (Exception e)
			{
				//e.printStackTrace() ;
			}

			// close file.
			if (fw != null)
			{
				// catch Exception here or throw.
				try
				{
					fw.close() ;
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
			}
			Toast.makeText(this,fnam+"에 저장 성공.",2).show();
		}
	}
		RunnerThread runner;
	public boolean isDoing=false;
	boolean isDone=false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		inputTextEdit = (EditText) findViewById(R.id.inputText);
		resultTextEdit = (EditText) findViewById(R.id.resultText);
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		exportButton = (Button) findViewById(R.id.exportButton);
		exportButton.setOnClickListener(this);
		isDoing = false;
		isDone = false;
    }
	EditText inputTextEdit;
	EditText resultTextEdit;
	Button startButton;
	Button exportButton;

	public void OnFinishParse(int n)
	{
		final String txt=new String("parsing 완료:") + new Integer(n).toString();
		resultTextEdit.post(new Runnable() {
                public void run()
				{
                    resultTextEdit.setText(txt);

                }
            });
	}
	public void OnProgress(int x, int n)
	{
		final String txt=new String("완료:") + new Integer(x).toString() + "/" + new Integer(n).toString();
		resultTextEdit.post(new Runnable() {
                public void run()
				{
                    resultTextEdit.setText(txt);

                }
            });
	}
	public void OnFinish()
	{
		//try
		//{
		//Toast.makeText(this, "finish", 1).show();
		isDoing = false;
		//final RunnerThread.Result result=runner.results.get(0);
		String tot=new String();
		for (RunnerThread.Result r:runner.results)
		{
			String oneresult=r.word + "(" + r.kor + ")" + ":" + r.syns[0];
			tot += oneresult;
			tot += "\n";
		}
		int lastnindex=tot.lastIndexOf("\n");
		if (lastnindex != -1)
		{
			tot = tot.substring(0, lastnindex);
		}
		//tot=tot.substring(0, );
		final String resultString=tot;
		//result.word + "(" + result.kor + ")" + ":" + result.syns[0];
		resultTextEdit.post(new Runnable() {
                public void run()
				{
                    resultTextEdit.setText(resultString);

                }
            });
		isDone = true;
		//}
		/*catch (Exception e)
		 {
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 PrintStream pinrtStream = new PrintStream(out);
		 //e.printStackTrace()하면 System.out에 찍는데,
		 // 출력할 PrintStream을 생성해서 건네 준다
		 e.printStackTrace(pinrtStream);
		 String stackTraceString = out.toString(); // 찍은 값을 가져오고.
		 //Toast.makeText(this, stackTraceString, 10).show();//보여 준다

		 }*/

	}
	public boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;
	}
}
