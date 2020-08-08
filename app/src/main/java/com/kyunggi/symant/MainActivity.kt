package com.kyunggi.symant

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.main.*

class MainActivity : Activity() {
    val btest = false
    private val TAG = "SynAnt"

    private fun doWork() {
        if (isDoing) {
            Toast.makeText(this, getString(R.string.already_working), Toast.LENGTH_SHORT).show()
            return
        }

        val t = inputText.text.toString()
        if (t.isEmpty()) {
            Toast.makeText(this, R.string.write_article_here, Toast.LENGTH_SHORT).show()
            return
        }

        if (isNetworkUnavailable()) return
        startButton.isEnabled = false
        exportButton.isEnabled = false
        Toast.makeText(this, getString(R.string.started), Toast.LENGTH_SHORT).show()
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
    }

    private fun isNetworkUnavailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null) {
            if (networkInfo.isConnected == false) {
                Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
                return true
            } else {
                if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(this, getString(R.string.warn_network_fee), Toast.LENGTH_SHORT).show()
                }
            }
            //Toast.makeText(this, "네트워크 연결을 확인하세요.", 1).show();
        } else {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun export() {
        if (isDoing || !isDone) {
            Toast.makeText(this, getString(R.string.not_ready), Toast.LENGTH_SHORT).show()
        }
        if (!isExternalStorageWritable) {
            Toast.makeText(this, getString(R.string.fail_write_file), Toast.LENGTH_LONG).show()
            return
        }
        exportButton.isEnabled = false
        val now = System.currentTimeMillis()
        //	Step2. Date 생성하기
        val date = Date(now)
        val sdf = SimpleDateFormat("yy-MM-dd-hh-mm")
        val getTime = sdf.format(date)
        //	출처: http://liveonthekeyboard.tistory.com/129 [키위남]
        var exportstr = resultText.text.toString()
        exportstr = exportstr.replace(",".toRegex(), ".")
        exportstr = exportstr.replace(":", ",")
        exportstr = exportstr.replace(" {2}".toRegex(), ",")
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
        exportButton.isEnabled = true
    }

    var runner: RunnerThread? = null
    var isDoing = false
    var isDone = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        startButton.setOnClickListener {
            doWork()
        }
        exportButton.setOnClickListener {
            export()
        }
        isDoing = false
        isDone = false
        try {
            System.setErr(PrintStream(File("/sdcard/synant_log.txt")))
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "log redirect failed", e)
        }
    }

    fun OnFinishParse(n: Int) {
        val txt = "parsing 완료:$n"
        runOnUiThread {
            resultText.setText(txt)
        }
    }

    fun onProgress(x: Int, n: Int) {
        val txt: String = "완료:$x/$n"
        runOnUiThread {
            resultText.setText(txt)
        }
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
        tot = tot.trim()
        //tot=tot.substring(0, );
        val resultString = tot
        //result.word + "(" + result.kor + ")" + ":" + result.syns[0];
        runOnUiThread {
            resultText.setText(resultString)
            startButton.isEnabled = true
            exportButton.isEnabled = true
        }
        isDone = true
        //}
    }

    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }
}