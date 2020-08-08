package com.kyunggi.symant

import android.util.Log
import android.widget.Toast
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern

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
class RunnerThread(val mainActivity: MainActivity, var _text: String) : Thread() {
    private val TAG = "SynAnt runner"

    var words: Set<String>? = null
    var results: ArrayList<Result>? = null
    override fun run() {
        val text = _text
        //handler.sendEmptyMessage(0);
        words = ParseBook(text)
        mainActivity.OnFinishParse(words!!.size)
        results = ArrayList()
        if (mainActivity.btest) {
            try {
                //String response=Query(q);
                val result = SoupTest() //ParseResponse(response);
                results!!.add(result)
            } catch (e: Exception) {
                val out = ByteArrayOutputStream()
                val pinrtStream = PrintStream(out)
                e.printStackTrace(pinrtStream)
                val stackTraceString = out.toString() // 찍은 값을 가져오고.
                mainActivity.runOnUiThread {
                    Toast.makeText(mainActivity, stackTraceString, Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "", e)
            }
        } else {
            var i = 0
            for (w in words!!) {
                i++
                Log.v(TAG, "processing word:$w")
                mainActivity.onProgress(i, words!!.size)
                try {
                    //String response=Query(q);
                    val result = queryBySoup(w) //ParseResponse(response);
                    results!!.add(result)
                } catch (e: Exception) {
                    val out = ByteArrayOutputStream()
                    val printStream = PrintStream(out)
                    e.printStackTrace(printStream)
                    Log.e(TAG, "", e)
                }
            }
        }
        mainActivity.OnFinish()
    }

    private fun SoupTest(): Result {
        val r = Result()
        try {
            val file = File("/storage/emulated/0/happy.html", "")
            val doc = Jsoup.parse(file, "UTF-8")
            //Elements synonyms=doc.select(".synonyms");
            val elems = doc.select(".synonyms")
            val mean = doc.select(".mean").first()
            var syns = elems.text()
            syns = syns.replace("유의어".toRegex(), "")
            r.word = "happy"
            r.kor = mean.text().replace("happy".toRegex(), "")
            r.syns = arrayOfNulls(1)
            r.syns[0] = syns
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
        } catch (e: IOException) {
            //Toast.makeText(mainActivity,
            mainActivity.runOnUiThread {
                Toast.makeText(mainActivity, "엥", Toast.LENGTH_SHORT).show()
            }
            Log.e(TAG, "", e)
        }
        return r
    }

    private fun queryBySoup(w: String): Result {
        val q = w.toQueryStringSynonym()
        val r = Result()
        try {
            val doc = Jsoup.connect(q).get()
            //Log.v(TAG,"doc="+doc.html());
            //Elements elems=doc.select(".");
            //Log.v(TAG,"elems="+elems.text());
            //Element mean=doc.select(".mean").first();
            //Log.v(TAG,"mean="+mean.text());
            //String syns=elems.html();
            //Log.v(TAG,"syns="+syns);
            //syns=syns.replaceAll("유의어","");
            r.word = w
            r.kor = "" // mean.text().replaceAll(w,"");
            r.syns = arrayOfNulls(1)
            r.syns[0] = "" // syns;
            var str = doc.text()
            Log.v(TAG, str)
            str = str.replace("예문 TTS 발음듣기".toRegex(), "")
            val toks = str.split(Pattern.quote("[유의어]").toRegex()).toTypedArray()
            val len = toks.size
            if (len > 2) {
                r.syns = arrayOfNulls(len - 2)
                System.arraycopy(toks, 1, r.syns, 0, len - 2)
            }
        } catch (e: IOException) {
            //Toast.makeText(mainActivity,
            mainActivity.runOnUiThread{
                Toast.makeText(mainActivity, "네트워크 상태를 확인해 주세요.", Toast.LENGTH_SHORT).show()
            }
            Log.e(TAG, "", e)
        }
        return r
    }

    fun ParseBook(text: String): Set<String> {
        val results: MutableSet<String> = LinkedHashSet()
        val words = text.split(" ".toRegex()).toTypedArray()
        for (w in words) {
            if (w.isEnglish()) {
                val normalizedW = w.normalize()
                results.add(normalizedW)
            }
        }
        return results
    }



    inner class Result {
        var word: String? = null
        var normalized: String? = null
        var kor: String? = null
        var syns: Array<String?> = arrayOfNulls(1)
        var kors: Array<String> = arrayOf()
    }

    /*
	 Result ParseResponse(String response)
	 {
	 return ;
	 }*/
    @Throws(Exception::class)
    fun Query(q: String?): String {

        // 한글의 경우 인코딩을 해야함.
        // 서버쪽에서는 따로 decode할 필욘 없음. 대신 new String(str.getBytes("8859_1"), "UTF-8");로 인코딩을 변경해야함
        // String str = URLEncoder.encode("한글", "UTF-8");
        val url = URL(q) //"http://localhost:8080/XmlTest/index.jsp");
        // open connection
        val conn = url.openConnection() as HttpURLConnection
        //  conn.setDoInput(true);            // 입력스트림 사용여부
        //  conn.setDoOutput(true);            // 출력스트림 사용여부
        conn.useCaches = false // 캐시사용 여부
        conn.readTimeout = 20000 // 타임아웃 설정 ms단위
        conn.requestMethod = "GET" // or GET
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
        val sb = StringBuffer()
        val br = BufferedReader(InputStreamReader(conn.inputStream))
        while (true) {
            val line = br.readLine() ?: break
            sb.append(line)
            sb.append(System.getProperty("line.separator"))
        }
        br.close()
        conn.disconnect()
        return sb.toString()
    }
}