package com.example.airsoftbomb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.MediaPlayer
import android.net.VpnService.prepare
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import com.example.airsoftbomb.databinding.ActivityMainBinding
import android.os.Handler
import android.os.Looper.prepare
import android.provider.Settings
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    var timer: CountDownTimer? = null

    //настройки
    var bombTimeSec : Int = 45
    var bombTimeMin : Int = 0
    var countPlant  = 4
    var countDefuse  = 8
    var adminPassword = "123"
    var adminPassword2 = "111222333"
    //настройки кончились

    lateinit var bindingC : ActivityMainBinding
    private lateinit var prefs: SharedPreferences


    var passwordEnter = ""
    var mainText = ""
    var rndS:Char = 'A'
    var plant = false

    var c4_beep1 = MediaPlayer()
    var c4_click = MediaPlayer()
    var c4_explode = MediaPlayer()
    var bombDef = MediaPlayer()
    var bombPlant = MediaPlayer()
    var error = MediaPlayer()
    var ctWin = MediaPlayer()
    var tWin = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingC = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingC.root)

        prefs =getSharedPreferences("settings", Context.MODE_PRIVATE)

        //инициализация функций
        randomSumble()
        adminSettings()
        numPad()
        settings()

        //инициализация плееров
        c4_beep1 = MediaPlayer.create(this,R.raw.c4_beep1)
        c4_click = MediaPlayer.create(this,R.raw.c4_click)
        c4_explode = MediaPlayer.create(this,R.raw.c4_explode1)
        bombDef = MediaPlayer.create(this,R.raw.bombdef)
        bombPlant = MediaPlayer.create(this,R.raw.bombpl)
        error = MediaPlayer.create(this,R.raw.error)
        ctWin = MediaPlayer.create(this,R.raw.ctwin_1)
        tWin = MediaPlayer.create(this,R.raw.terwin_1)

    }
    // Запоминаем данные

    private fun saveSettings(){
        val editor = prefs.edit()
        editor.putInt("BOMB_TIME_SEC", bombTimeSec).apply()
        editor.putInt("BOMB_TIME_MIN", bombTimeMin).apply()
        editor.putInt("COUNT_PLANT", countPlant).apply()
        editor.putInt("COUNT_DEFUSE", countDefuse).apply()
        editor.putString("ADMIN_PASSWORD", adminPassword).apply()
    }

    override fun onPause() {
        super.onPause()
        saveSettings()

    }
    //вспоминаем данные
    override fun onResume() = with(bindingC) {
        super.onResume()

        if(prefs.contains("BOMB_TIME_SEC")) {
            bombTimeSec = prefs.getInt("BOMB_TIME_SEC", 0)
            edTimerSec.setText(bombTimeSec.toString())
        }
        if(prefs.contains("BOMB_TIME_MIN")) {
            bombTimeMin = prefs.getInt("BOMB_TIME_MIN", 0)
            edTimerMin.setText(bombTimeMin.toString())
        }
        if(prefs.contains("COUNT_PLANT")) {
            countPlant = prefs.getInt("COUNT_PLANT", 0)
            edCount2Plant.setText(countPlant.toString())
        }
        if(prefs.contains("COUNT_DEFUSE")) {
            countDefuse = prefs.getInt("COUNT_DEFUSE", 0)
            edCount2Defuse.setText(countDefuse.toString())
        }
        if(prefs.contains("ADMIN_PASSWORD")) {
            adminPassword = prefs.getString("ADMIN_PASSWORD", "0").toString()
            edAdminPassword.setText(adminPassword)
        }


    }


    //обработка нажатий

    var count = 0
    private fun pressButton(bNum:Char) = with (bindingC){
        //beep || click звук
        c4_beep1.start()
        c4_click.start()

        //проверка на правильный ввод
        if(rndS == bNum){
            count ++
            mainText += bNum
            tvPassword.text = mainText
            randomSumble()

                //закладка бумбы
            if(count == countPlant && !plant){
                 startBombTimer(bombTimeSec,bombTimeMin)
                mainText = ""
                tvPassword.text = mainText
                tvRandomSumbol.text = "OK"
                tvRandomSumbol.textSize = 60F
                count = 0

                plant = true //запленчено

                bombPlant.start()
                val handler = Handler()
                handler.postDelayed({
                    tvRandomSumbol.text = rndS.toString()
                    tvRandomSumbol.textSize = 34F
                    randomSumble()
                }, 1000)
            }
            //разминирование бомбы
            if(plant && count == countDefuse){
                plant = false
                count = 0
                mainText = ""
                tvPassword.text = mainText
                tvRandomSumbol.text = "CT Win"
                tvRandomSumbol.textSize = 40F
                timer?.cancel()

                //звук разминирование
                bombDef.start()

                val handler = Handler()
                handler.postDelayed({
                    tvRandomSumbol.text = rndS.toString()
                    tvRandomSumbol.textSize = 34F
                    randomSumble()
                    ctWin.start()
                }, 1000)

            }
            //неудачное нажатие
        } else{
            tvPassword.text = ""
            mainText = ""
            count = 0
            //звук ошибки
            error.start()

            randomSumble()
        }
    }


    //таймер бомбы



    private fun startBombTimer(secTime:Int, minTime:Int) {


        var timeMilis:Long = (secTime*1000L) + (minTime*60000L)

        timer = object :CountDownTimer(timeMilis,1000){

                override fun onTick(p0: Long) {
                    //бип
                    c4_beep1.start()


                    bindingC.tvTimerS.setText(timeStringFromLong(p0))
                }

            override fun onFinish() {
                plant = false
                mainText = ""
                bindingC.tvPassword.text = mainText
                bindingC.tvRandomSumbol.text = "T Win"
                bindingC.tvRandomSumbol.textSize = 40F
                //звук взрыва
                c4_explode.start()
                val handler = Handler()
                handler.postDelayed({
                    bindingC.tvRandomSumbol.text = rndS.toString()
                    bindingC.tvRandomSumbol.textSize = 34F
                    randomSumble()
                    //звук победы т
                tWin.start()
                }, 1000)
            }
        }.start()
    }

    private fun timeStringFromLong(ms: Long): String
    {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60) % 60)
        return makeTimeString( minutes, seconds)
    }

    private fun makeTimeString( minutes: Long, seconds: Long): String
    {
        return String.format("%02d:%02d", minutes, seconds)
    }

    //обработка кнопок
    private fun numPad() = with(bindingC){

        b1.setOnClickListener { pressButton('1') }
        b2.setOnClickListener { pressButton('2') }
        b3.setOnClickListener { pressButton('3') }
        b4.setOnClickListener { pressButton('4') }
        b5.setOnClickListener { pressButton('5') }
        b6.setOnClickListener { pressButton('6') }
        b7.setOnClickListener { pressButton('7') }
        b8.setOnClickListener { pressButton('8') }
        b9.setOnClickListener { pressButton('9') }
        b0.setOnClickListener { pressButton('0') }
        bStar.setOnClickListener { pressButton('*') }
        bJail.setOnClickListener {
            passwordEnter = ""
            pressButton('#')
        }

    }


    //звуки


    //настройки и доступ к ним
    //случайные символы
    private fun randomSumble() {
     val rndSumbl =
        arrayOf('0','1','2',
        '3','4','5',
        '6','7','8',
        '9', '*', '#').random()
    bindingC.tvRandomSumbol.text = rndSumbl.toString()
        rndS = rndSumbl
}

    //активация админ меню
    private fun adminSettings() = with (bindingC) {

        bAdmin1.setOnClickListener {
            tvRandomSumbol.text = "1"
            passwordCheker( '1' )
        }
        bAdmin2.setOnClickListener {
            tvRandomSumbol.text = "2"
            passwordCheker( '2' )
        }
        bAdmin3.setOnClickListener {
            tvRandomSumbol.text = "3"
            passwordCheker( '3' )

        }


    }
    private fun passwordCheker(passwordEntering: Char) = with(bindingC) {
        passwordEnter += passwordEntering
        if(adminPassword == passwordEnter || passwordEnter == adminPassword2){

            allGone(GONE)

            adminPanel.visibility = VISIBLE

            Toast.makeText(this@MainActivity, "done", Toast.LENGTH_SHORT).show()
        }
        if(passwordEnter.length >9 ) passwordEnter = ""
    }

    private fun allGone (visib:Int) = with(bindingC){
        materialToolbar.visibility = visib
        b0.visibility = visib
        b1.visibility = visib
        b2.visibility = visib
        b3.visibility = visib
        b4.visibility = visib
        b5.visibility = visib
        b6.visibility = visib
        b7.visibility = visib
        b8.visibility = visib
        b9.visibility = visib
        bStar.visibility = visib
        bJail.visibility = visib
        bAdmin1.visibility = visib
        bAdmin2.visibility = visib
        bAdmin3.visibility = visib
        tvTimerS.visibility = visib
        tvRandomSumbol.visibility = visib
        tvPassword.visibility = visib

    }

    private fun hideSoftKeyboard(view: View) {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun settings()= with(bindingC){
        bSBack.setOnClickListener {
            adminPanel.visibility = GONE
            allGone(VISIBLE)
            hideSoftKeyboard(it)
            randomSumble()
            saveSettings()

            bombTimeMin = edTimerMin.text.toString().toInt()
            bombTimeSec = edTimerSec.text.toString().toInt()
            countPlant  = edCount2Plant.text.toString().toInt()
            countDefuse  = edCount2Defuse.text.toString().toInt()
            adminPassword = edAdminPassword.text.toString()
        }
    }

    fun ActivityTEst(view: View) {
        val intent = Intent(this, settingsActivity::class.java)

// start your next activity
        startActivity(intent)

    }

}

