package com.kyunggi.symant

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), View.OnClickListener {
    @JvmField
	var btest = false
    private val TAG = "SynAnt"
    override fun onClick(p1: View) {
        if (p1 !is Button) return
        val b = p1
        if (b === startButton) {
            // TODO: Implement this method
            if (isDoing) {
                Toast.makeText(this, "이미 하는 중입니다.", Toast.LENGTH_SHORT).show()
                return
            }
            val t = inputTextEdit!!.text.toString()
            if (t.length == 0 || t.compareTo("본문을 입력하세요", ignoreCase = true) == 0) {
                Toast.makeText(this, "본문을 입력하세요.", Toast.LENGTH_SHORT).show()
                return
            }
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null) {
                if (networkInfo.isConnected == false) {
                    Toast.makeText(this, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                        Toast.makeText(this, "데이터 요금이 발생할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                //Toast.makeText(this, "네트워크 연결을 확인하세요.", 1).show();
            } else {
                Toast.makeText(this, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
                return
                //Toast.makeText(this, "테스트.", 1).show();
                //btest = true;
            }
            Toast.makeText(this, "시작됨.", Toast.LENGTH_SHORT).show()
            try {
                isDoing = true
                runner = RunnerThread(this, t)
                runner!!.start()
            } catch (e: Exception) {
                val out = ByteArrayOutputStream()
                val printStream = PrintStream(out)
                e.printStackTrace(printStream)
                val stackTraceString = out.toString() // 찍은 값을 가져오고.
                Toast.makeText(this, stackTraceString, Toast.LENGTH_LONG).show() //보여 준다
                Log.e(TAG, "", e)
            }
        } else if (b === exportButton) {
            if (isDoing == true || isDone == false) {
                Toast.makeText(this, "아직 완료되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
            if (!isExternalStorageWritable) {
                Toast.makeText(this, "파일 기록에 문제가 있습니다.", Toast.LENGTH_LONG).show()
                return
            }
            val now = System.currentTimeMillis()
            //	Step2. Date 생성하기
            val date = Date(now)
            val sdf = SimpleDateFormat("yy-MM-dd-hh-mm")
            val getTime = sdf.format(date)
            //	출처: http://liveonthekeyboard.tistory.com/129 [키위남]
            var exportstr = resultTextEdit!!.text.toString()
            exportstr = exportstr.replace(",".toRegex(), ".")
            exportstr = exportstr.replace(":", ",")
            exportstr = exportstr.replace("  ".toRegex(), ",")
            var fnam: String
            val file = File("/storage/emulated/0/synant_$getTime.csv".also { fnam = it })
            var fw: FileWriter? = null
            //String text = "This is TEST string." ;
            try {
                // open file.
                fw = FileWriter(file)

                // write file.
                fw.write(exportstr)
            } catch (e: Exception) {
                Log.e(TAG, "", e)
                //e.printStackTrace() ;
            }

            // close file.
            if (fw != null) {
                // catch Exception here or throw.
                try {
                    fw.close()
                } catch (e: Exception) {
                    Log.e(TAG, "", e)
                    //e.printStackTrace();
                }
            }
            Toast.makeText(this, fnam + "에 저장 성공.", Toast.LENGTH_LONG).show()
        }
    }

    var runner: RunnerThread? = null
    var isDoing = false
    var isDone = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        inputTextEdit = findViewById<View>(R.id.inputText) as EditText
        resultTextEdit = findViewById<View>(R.id.resultText) as EditText
        startButton = findViewById<View>(R.id.startButton) as Button
        startButton!!.setOnClickListener(this)
        exportButton = findViewById<View>(R.id.exportButton) as Button
        exportButton!!.setOnClickListener(this)
        isDoing = false
        isDone = false
        try {
            System.setErr(PrintStream(File("/sdcard/synant_log.txt")))
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "log redirect failed", e)
        }
    }

    var inputTextEdit: EditText? = null
    var resultTextEdit: EditText? = null
    var startButton: Button? = null
    var exportButton: Button? = null
    fun OnFinishParse(n: Int) {
        val txt: String = "parsing 완료:$n"
        resultTextEdit!!.post { resultTextEdit!!.setText(txt) }
    }

    fun OnProgress(x: Int, n: Int) {
        val txt: String = "완료:$x/$n"
        resultTextEdit!!.post { resultTextEdit!!.setText(txt) }
    }

    fun OnFinish() {
        //try
        //{
        //Toast.makeText(this, "finish", 1).show();
        isDoing = false
        //final RunnerThread.Result result=runner.results.get(0);
        var tot = String()
        for (r in runner!!.results!!) {
            val oneresult = r.word + "(" + r.kor + ")" + ":" + Arrays.toString(r.syns)
            tot += oneresult
            tot += "\n"
        }
        val lastnindex = tot.lastIndexOf("\n")
        if (lastnindex != -1) {
            tot = tot.substring(0, lastnindex)
        }
        //tot=tot.substring(0, );
        val resultString = tot
        //result.word + "(" + result.kor + ")" + ":" + result.syns[0];
        resultTextEdit!!.post { resultTextEdit!!.setText(resultString) }
        isDone = true
        //}
    }

    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }
}