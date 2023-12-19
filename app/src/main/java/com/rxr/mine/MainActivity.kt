package com.rxr.mine

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.google.android.material.tabs.TabLayout
import java.util.concurrent.TimeUnit
import android.os.Handler
import com.facebook.ads.AudienceNetworkAds
import com.rxr.mine.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tab_background.view.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

//1030c14cda459fa5
class MainActivity : AppCompatActivity(), MaxAdListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private var timerRunning: Boolean = false
    private var curentTiming: Long = 0
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    val BASE_URL = "https://growwithonline.com/XRX/"
    var Balance: Double = 0.0
    var restTime:Long = 0
    var refercode:String = "ERROR"
    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0
    private lateinit var view: View
    private lateinit var binding: ActivityMainBinding
    private var adsDetaction = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Applovin intialization
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( this ).setMediationProvider( "max" )
        AppLovinSdk.getInstance( this ).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads
        })
        interstitialAd = MaxInterstitialAd( "1030c14cda459fa5", this)
        interstitialAd.setListener( this )

        // Load the first ad
        interstitialAd.loadAd()
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_background, null);
        //if(internet is on)
        //else
        //..default font
        setCustomView(0, 1)
        setTextAndImageWithAnimation("HOME", R.drawable.ic_home);
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    1 -> {
                        setCustomView(1, 0)
                        setTextAndImageWithAnimation("MENU", R.drawable.ic_categories)
                        //change to the fragment which you want to display
                        val intent = Intent(this@MainActivity,Profile::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        setCustomView(0, 1)
                        setTextAndImageWithAnimation("HOME", R.drawable.ic_home)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        // Restore timer state
        timerRunning = sharedPreferences.getBoolean("timerRunning", false)
        timeLeftInMillis = sharedPreferences.getLong("timeLeftInMillis", 86400000)
        val stopBal = sharedPreferences.getString("stopBal","0")
        binding.txtLanguage.text = stopBal.toString()

        userDetails()

        if (timerRunning) {
            curentTiming = sharedPreferences.getLong("endTime", 86400000)//86400000
            timeLeftInMillis = curentTiming - System.currentTimeMillis()
            restTime = ((86400000 - timeLeftInMillis) / 1000).toLong()  //86400000
            if (restTime > 86400) {        //
                restTime = 86400.toLong()    //86400
            }
            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 86400000   //86400000
                //here handle for when user update there balance
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val username = sharedPreferences.getString("username", null)
                // is this extra code for add balance when timer comlete and our app closed
                mRequestQueue    = Volley.newRequestQueue(this)
                jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET,
                    BASE_URL + "/api/get_user.php?username=" + username.toString(),
                    null,
                    Response.Listener
                    { response ->
                        try {
                            //we used this token for auto login
                            var balance = response.getDouble("balance")
                            Balance = balance.toString().toDouble()
                            Balance = (Balance + 108).toDouble()

                            val jsonObject = JSONObject()
                            try {
                                jsonObject.put("username", username.toString())
                                jsonObject.put("new_value", Balance)
                                jsonObject.put("column_name", "balance")
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                            mRequestQueue = Volley.newRequestQueue(this)
                            jsonObjectRequest = JsonObjectRequest(
                                Request.Method.POST,
                                BASE_URL + "/api/update_balnce.php",
                                jsonObject,
                                Response.Listener
                                { response ->
                                    try {
                                        Log.d("sucesss", response.toString())
                                        Toast.makeText(this, "Your Token Added", Toast.LENGTH_SHORT)
                                            .show()
                                        userDetails()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                Response.ErrorListener {
                                    Log.d("error", it.message.toString())
                                })
                            mRequestQueue!!.add(jsonObjectRequest)


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        Log.d("error", it.message.toString())
                    })
                mRequestQueue!!.add(jsonObjectRequest)
                //end of our logic is there
                restTime = 0.toLong()
                timerRunning = false
            } else {
                startTimer()
            }
        }

        binding.txtStart.setOnClickListener {
            adsDetaction = 1
            if ( interstitialAd.isReady() )
            {
                interstitialAd.showAd();
            }
            else {
                if (!timerRunning) {
                    startTimer()
                }
                binding.txtStart.isEnabled = false
            }
        }

        //extra 3 btn hanadales
        binding.linearColumntrashqxx.setOnClickListener {
            AppLovinSdk.getInstance(this).showMediationDebugger()
//            adsDetaction = 2
//            if ( interstitialAd.isReady() )
//            {
//                interstitialAd.showAd();
//            }else {
//            binding.txtLanguage.visibility = View.GONE
//            binding.contentProgressbar.visibility = View.VISIBLE
//            val cName = "bonus"
//            getBonus(cName)
//            }
        }
        binding.linearColumntrashq.setOnClickListener {
            adsDetaction = 3
            if ( interstitialAd.isReady() )
            {
                interstitialAd.showAd();
            }else {
            binding.txtLanguage.visibility = View.GONE
            binding.contentProgressbar.visibility = View.VISIBLE
            getHoursbonus()
            }
        }
        binding.linearColumngroupadd.setOnClickListener {
            adsDetaction = 4
            if ( interstitialAd.isReady() )
            {
                interstitialAd.showAd();
            }else {
            binding.txtLanguage.visibility = View.GONE
            binding.contentProgressbar.visibility = View.VISIBLE
            val cName = "reward"
            getOtherBonus(cName)
            }
        }
        binding.linearColumngroupaddx.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "The invitation code is "+refercode+"\n" +
                        "\n" +
                        "RXR Network is a new digital currency. To claim your RXR Network, follow this link  \n" +
                        " https://play.google.com/store/apps/details?id=com.rxr.mine")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        binding.btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "The invitation code is "+refercode+"\n" +
                        "\n" +
                        "RXR Network is a new digital currency. To claim your RXR Network, follow this link  \n" +
                        " https://play.google.com/store/apps/details?id=com.rxr.mine")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        //botom menu handale
    }

    private fun getOtherBonus(cName: String) {
        val link = "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            link,
            null,
            Response.Listener
            { response ->
                try {
                    //save
                    val servDate = response.getString("date") // dd/mm/yy
                    val sdf = SimpleDateFormat("MM/dd/yyyy")
                    val curDate = sdf.format(Date())
                    //retrive
                    //first wee check user is click or not
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username",null)
                    mRequestQueue = Volley.newRequestQueue(this)
                    jsonObjectRequest = JsonObjectRequest(
                        Request.Method.GET,
                        BASE_URL + "/api/get_user.php?username="+username.toString(),
                        null,
                        Response.Listener
                        { response ->
                            try {
                                //we used this token for auto login
                                val bonus = response.getString(cName)
                                val locDate = sharedPreferences.getString("locDatevvv",curDate)
                                if (bonus == "0"){
                                    if (servDate == curDate){
                                        Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                        updateBalance()
                                        Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                        val bonuss = "1"
                                        val editor = sharedPreferences.edit()
                                        editor.putString("locDatevvv", servDate)
                                        editor.apply()
                                        updateBonus(bonuss,cName)
                                        binding.txtLanguage.visibility = View.VISIBLE
                                        binding.contentProgressbar.visibility = View.GONE
                                    }
                                    //
                                }else if(locDate != servDate){
                                    val bonuss = "false"
                                    val editor = sharedPreferences.edit()
                                    editor.putString("locDatevvv", servDate)
                                    editor.apply()
                                    Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                    updateBalance()
                                    Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                    binding.txtLanguage.visibility = View.VISIBLE
                                    binding.contentProgressbar.visibility = View.GONE
                                } else{
                                    binding.txtLanguage.visibility = View.VISIBLE
                                    binding.contentProgressbar.visibility = View.GONE
                                    Toast.makeText(this,"Allredy Claimed",Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener {
                            Log.d("error", it.message.toString())
                            binding.txtLanguage.visibility = View.VISIBLE
                            binding.contentProgressbar.visibility = View.GONE
                        })
                    mRequestQueue!!.add(jsonObjectRequest)
                }
                catch(e:Exception){
                }
            },
            Response.ErrorListener {
                Log.d("error", it.message.toString())
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun getHoursbonus() {
                    //save
                    val sdf = SimpleDateFormat("HH")
                    val curDate = sdf.format(Date())
                    //retrive
                    //first wee check user is click or not
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username",null)
                    mRequestQueue = Volley.newRequestQueue(this)
                    jsonObjectRequest = JsonObjectRequest(
                        Request.Method.GET,
                        BASE_URL + "/api/get_user.php?username="+username.toString(),
                        null,
                        Response.Listener
                        { response ->
                            try {
                                //we used this token for auto login
                                val bonus = response.getString("hourly")
                                val locDate = sharedPreferences.getString("locDatenew",curDate)
                                if (bonus == "0"){
                                    if (locDate == curDate){
                                        Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                        updateBalance()
                                        Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                        val bonuss = "1"
                                        val editor = sharedPreferences.edit()
                                        editor.putString("locDatenew", curDate)
                                        editor.apply()
                                        binding.txtLanguage.visibility = View.VISIBLE
                                        binding.contentProgressbar.visibility = View.GONE
                                        val cName = "hourly"
                                        updateBonus(bonuss,cName)
                                    }else{
                                        binding.txtLanguage.visibility = View.VISIBLE
                                        binding.contentProgressbar.visibility = View.GONE
                                    }
                                    //
                                }else if(locDate != curDate){
                                    val editor = sharedPreferences.edit()
                                    editor.putString("locDatenew", curDate)
                                    editor.apply()
                                    Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                    updateBalance()
                                    Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                    binding.txtLanguage.visibility = View.VISIBLE
                                    binding.contentProgressbar.visibility = View.GONE
                                } else{
                                    binding.txtLanguage.visibility = View.VISIBLE
                                    binding.contentProgressbar.visibility = View.GONE
                                    Toast.makeText(this,"Allredy Claimed",Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                binding.txtLanguage.visibility = View.VISIBLE
                                binding.contentProgressbar.visibility = View.GONE
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener {
                            binding.txtLanguage.visibility = View.VISIBLE
                            binding.contentProgressbar.visibility = View.GONE
                            Log.d("error", it.message.toString())
                            Toast.makeText(this,"Try After Sometime",Toast.LENGTH_SHORT).show()
                        })
                    mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun setCustomView(selectedtab: Int, non1: Int) {
        Objects.requireNonNull(tabLayout.getTabAt(selectedtab))?.setCustomView(view)
        Objects.requireNonNull(tabLayout.getTabAt(non1))?.setCustomView(null)
    }

    private fun setTextAndImageWithAnimation(text: String, images: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, android.R.anim.slide_in_left)
        animation.setInterpolator(AccelerateDecelerateInterpolator())
        view.tv1.setText(text)
        view.iv1.setImageResource(images)
        view.tv1.startAnimation(animation)
        view.iv1.startAnimation(animation)
    }
    //botom menu handale
    private fun getBonus(cName: String) {
        val link = "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            link,
            null,
            Response.Listener
            { response ->
                try {
                    //save
                    val servDate = response.getString("date") // dd/mm/yy
                    val sdf = SimpleDateFormat("MM/dd/yyyy")
                    val curDate = sdf.format(Date())
                    //retrive
                    //first wee check user is click or not
                        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val username = sharedPreferences.getString("username",null)
                        mRequestQueue = Volley.newRequestQueue(this)
                        jsonObjectRequest = JsonObjectRequest(
                            Request.Method.GET,
                            BASE_URL + "/api/get_user.php?username="+username.toString(),
                            null,
                            Response.Listener
                            { response ->
                                try {
                                    //we used this token for auto login
                                    val bonus = response.getString(cName)
                                    val locDate = sharedPreferences.getString("locDate",curDate)
                                    if (bonus == "0"){
                                        if (servDate == curDate){
                                            Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                            updateBalance()
                                            Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                            val bonuss = "1"
                                            val editor = sharedPreferences.edit()
                                            editor.putString("locDate", servDate)
                                            editor.apply()
                                            updateBonus(bonuss,cName)
                                            binding.txtLanguage.visibility = View.VISIBLE
                                            binding.contentProgressbar.visibility = View.GONE
                                        }
                                        //
                                    }else if(locDate != servDate){
                                        val editor = sharedPreferences.edit()
                                        editor.putString("locDate", servDate)
                                        editor.apply()
                                        Balance =  (Balance + 10) - (restTime.toDouble() * 0.00125)
                                        updateBalance()
                                        Toast.makeText(this,"Credited",Toast.LENGTH_SHORT).show()
                                        binding.txtLanguage.visibility = View.VISIBLE
                                        binding.contentProgressbar.visibility = View.GONE
                                    } else{
                                        binding.txtLanguage.visibility = View.VISIBLE
                                        binding.contentProgressbar.visibility = View.GONE
                                        Toast.makeText(this,"Allredy Claimed",Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            Response.ErrorListener {
                                Log.d("error", it.message.toString())
                                binding.txtLanguage.visibility = View.VISIBLE
                                binding.contentProgressbar.visibility = View.GONE
                            })
                        mRequestQueue!!.add(jsonObjectRequest)
                }
                catch(e:Exception){
                }
            },
            Response.ErrorListener {
                Log.d("error", it.message.toString())
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun updateBonus(bonuss: String, cName: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username",null)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", username.toString())
            jsonObject.put("new_value", bonuss)
            jsonObject.put("column_name", cName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "/api/update_reward.php",
            jsonObject,
            Response.Listener
            { response ->
                try {
                    Log.d("sucesss",response.toString())
                    userDetails()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                    binding.txtLanguage.visibility = View.VISIBLE
                    binding.contentProgressbar.visibility = View.GONE
                    Toast.makeText(this,"Something Error In Server.Please Wait Or Try Later",Toast.LENGTH_SHORT).show()
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun userDetails() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username",null)
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            BASE_URL + "/api/get_user.php?username="+username.toString(),
            null,
            Response.Listener
            { response ->
                try {
                    //we used this token for auto login
                    var balance = response.getDouble("balance")
                    var totalRefer = response.getInt("total_refer")
                    var referCode = response.getString("referral_code")
                    binding.txtZero.text = totalRefer.toString()
                    binding.txtLanguage.text = balance.toString()
                    Balance = balance
                    refercode = referCode
                    Balance = Balance + (restTime.toDouble() * 0.00125.toDouble()).toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Log.d("error", it.message.toString())
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun startTimer() {
        curentTiming = System.currentTimeMillis() + timeLeftInMillis
        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerUI()
                    //we run handeler which update every secound balance
                    val newBal = Balance + 0.00125.toDouble()//5
                    Balance = newBal //5
                    binding.txtLanguage.text = newBal.toString()
                    binding.txtLanguage.setTextAppearance(this@MainActivity,R.style.txtIntersemibold14)
            }

            override fun onFinish() {
                timerRunning = false
                timeLeftInMillis = 86400000     //86400000
                binding.txtStart.isEnabled = true
                binding.txtStart.text = "START"
                binding.txtStart.setTextAppearance(this@MainActivity,R.style.txtPoppinsregular24)
                //here we update our text box
                updateBalance()
            }
        }.start()

        timerRunning = true
    }

    private fun updateBalance() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username",null)
        val newBalance = Balance
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", username.toString())
            jsonObject.put("new_value", newBalance.toString())
            jsonObject.put("column_name", "balance")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "/api/update_balnce.php",
            jsonObject,
            Response.Listener
            { response ->
                try {
                    Log.d("sucesss",response.toString())
                    Toast.makeText(this,"Your Token Added",Toast.LENGTH_SHORT).show()
                    userDetails()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Log.d("error", it.message.toString())
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun updateTimerUI() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d:%02d",hours, minutes, seconds)
        binding.txtStart.text = timeLeftFormatted
        binding.txtStart.setTextAppearance(this,R.style.txtPoppinsregular24)
    }

    override fun onStop() {
        super.onStop()
        // Save timer state
        val editor = sharedPreferences.edit()
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.putLong("endTime",curentTiming)
        editor.putString("stopBal",Balance.toString())
        editor.apply()
    }
    //
    override fun onAdLoaded(p0: MaxAd) {
        retryAttempt = 0.0
    }

    override fun onAdDisplayed(p0: MaxAd) {}

    override fun onAdHidden(p0: MaxAd) {
        if (adsDetaction == 1){
            if (!timerRunning) {
                startTimer()
            }
            binding.txtStart.isEnabled = false
        }
        else if (adsDetaction == 2){
            binding.txtLanguage.visibility = View.GONE
            binding.contentProgressbar.visibility = View.VISIBLE
            val cName = "bonus"
            getBonus(cName)
        }
        else if (adsDetaction == 3){
            binding.txtLanguage.visibility = View.GONE
            binding.contentProgressbar.visibility = View.VISIBLE
            getHoursbonus()
        }
        else if (adsDetaction == 4){
            binding.txtLanguage.visibility = View.GONE
            binding.contentProgressbar.visibility = View.VISIBLE
            val cName = "reward"
            getOtherBonus(cName)
        }
        interstitialAd.loadAd()
    }

    override fun onAdClicked(p0: MaxAd) {}

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
        retryAttempt++
        val delayMillis = TimeUnit.SECONDS.toMillis( Math.pow( 2.0, Math.min( 6.0, retryAttempt ) ).toLong() )

        Handler().postDelayed( { interstitialAd.loadAd() }, delayMillis )
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        interstitialAd.loadAd()
    }
}
//60-40 = 20