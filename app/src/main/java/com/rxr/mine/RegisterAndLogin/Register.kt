package com.rxr.mine.RegisterAndLogin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.rxr.mine.MainActivity
import com.rxr.mine.R
import com.rxr.mine.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class Register : AppCompatActivity() {
    //Google Login
    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    //Manul Login
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityRegisterBinding
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private lateinit var tokenforlogin:String
    val BASE_URL = "https://growwithonline.com/XRX/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //google login
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //

        binding.btnVolume.setOnClickListener {
            binding.linearColumnvolume1.visibility = View.GONE
            binding.progressBar2.visibility = View.VISIBLE
            signIn()
        }

        //login btn click listener
        binding.txtAlreadyHaveAc.setOnClickListener {
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
            finish()
        }
        //manul registration process
        binding.btnSignUpOne.setOnClickListener {
            //this is check is for every box is fillings not any one null
            validdata()
            //start show a progress bar and hide this signin btn
        }
    }
    private fun validdata() {
        val email = binding.txtEmailaddress.text.toString()
        val password = binding.txtPassword.text.toString()
        val confermPassword = binding.txtConfirmPasswor.text.toString()
        if (email.isEmpty()){
            binding.txtEmailaddress.setError("required")
            binding.txtEmailaddress.requestFocus()
        }
        else if(password.isEmpty() || password.length < 6){
            binding.txtPassword.setError("required minimum 6 digits")
            binding.txtPassword.requestFocus()
        }
        else if(confermPassword.isEmpty() || confermPassword.length < 6){
            binding.txtConfirmPasswor.setError("required minimum 6 digits")
            binding.txtConfirmPasswor.requestFocus()
        }
        else{
            if(password == confermPassword){
                createnewuser()
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSignUpOne.visibility = View.GONE
            }else{
                binding.txtConfirmPasswor.setError("Password & ConfirmPassword Not Match")
                binding.txtConfirmPasswor.requestFocus()
                binding.progressBar.visibility = View.GONE
                binding.btnSignUpOne.visibility = View.VISIBLE
            }
        }
    }
    //google login
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            val userEmail = account.email.toString()
            val userPic = account.photoUrl.toString()
            val userPassword = "123456"

            val jsonObject = JSONObject()
            try {
                jsonObject.put("username", userEmail)
                jsonObject.put("password", userPassword)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            mRequestQueue = Volley.newRequestQueue(this)
            jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
                BASE_URL + "/api/signup.php",
                jsonObject,
                Response.Listener
                { response ->
                    try {
                        val respMessage = response.getString("message")
                        //we used this token for auto login
                        tokenforlogin = response.getString("token")
                        //now we save our token into sharedpref for auto login
                        sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("token", tokenforlogin)
                        editor.putString("username", userEmail)
                        editor.putString("userPic", userPic)
                        editor.apply()
                        if (respMessage == "User was created") {
                            //there we did code that
                            //refer code enter dialog box
                            try {
                                val dialog = Dialog(this)
                                dialog.setContentView(R.layout.row_listgroupadd)
                                dialog.window!!.setLayout(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                dialog.setCancelable(false)
                                dialog.window!!.attributes.windowAnimations = R.style.animation
                                val refercode_felled = dialog.findViewById<EditText>(R.id.edittext)
                                val cancel_btn  = dialog.findViewById<TextView>(R.id.cancel)
                                val sumbit_btn  = dialog.findViewById<TextView>(R.id.save)
                                val progressBarBTN = dialog.findViewById<ProgressBar>(R.id.referProgress)
                                sumbit_btn.setOnClickListener {
                                    if(refercode_felled.text.toString().isEmpty()) {
                                        refercode_felled.setError("required")
                                        refercode_felled.requestFocus()
                                    } else {
                                        //balance add   code
                                        sumbit_btn.visibility = View.GONE
                                        progressBarBTN.visibility = View.VISIBLE
                                        mRequestQueue = Volley.newRequestQueue(this)
                                        jsonArrayRequest = JsonArrayRequest(Request.Method.GET,
                                            BASE_URL + "/api/get_data.php",
                                            null,
                                            Response.Listener
                                            { response ->
                                                //here we try to find all refer code in loop
                                                //we are used for loop
                                                Log.d("resp", response.toString())
                                                for (i in 0 until response.length()) {
                                                    try {
                                                        val responseObj = response.getJSONObject(i)
                                                        val referCode = responseObj.getString("referral_code")
                                                        Log.d("resp", referCode.toString())
                                                        //we are used coditon for check there our textFILED data == refercode with any one
                                                        //after that we run our logic code
                                                        //if any refer code match then we give valid code
                                                        //after that we check which user codes thats it is
                                                        //and thats user refered incrise 1 and also give referal reward to that usser
                                                        //and also aplied for that user who also get referal reward
                                                        //and that user refered is true thats user not aplicable again new code entered
                                                        if (refercode_felled.text.toString() == referCode) {
                                                            //Do that next step
                                                            Toast.makeText(this,"Appiled",Toast.LENGTH_SHORT).show()
                                                            //
                                                            mRequestQueue = Volley.newRequestQueue(this)
                                                            jsonObjectRequest = JsonObjectRequest(
                                                                Request.Method.GET,
                                                                BASE_URL + "/api/get_user.php?username="+responseObj.getString("username").toString(),
                                                                null,
                                                                Response.Listener
                                                                { response ->
                                                                    try {
                                                                        //we used this token for auto login
                                                                        var balance = response.getDouble("balance")
                                                                        var totalRefer = response.getInt("total_refer")
                                                                        //
                                                                        val count = totalRefer + 1
                                                                        val jsonObject2 = JSONObject()
                                                                        try {
                                                                            jsonObject2.put("username", responseObj.getString("username").toString())
                                                                            jsonObject2.put("new_value", count.toString())
                                                                            jsonObject2.put("column_name", "total_refer")
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                        mRequestQueue = Volley.newRequestQueue(this@Register)
                                                                        jsonObjectRequest = JsonObjectRequest(
                                                                            Request.Method.POST,
                                                                            BASE_URL + "/api/update_reward.php",
                                                                            jsonObject2,
                                                                            Response.Listener
                                                                            { response ->
                                                                                try {
                                                                                    Log.d("sucesss",response.toString())
                                                                                } catch (e: Exception) {
                                                                                    e.printStackTrace()
                                                                                }
                                                                            },
                                                                            Response.ErrorListener {
                                                                                Toast.makeText(this@Register,"Something Error In Server.Please Wait Or Try Later",Toast.LENGTH_SHORT).show()
                                                                            })
                                                                        mRequestQueue!!.add(jsonObjectRequest)
                                                                        //update work
                                                                        val newBalance = balance + 10
                                                                        val jsonObject = JSONObject()
                                                                        try {
                                                                            jsonObject.put("username", responseObj.getString("username").toString())
                                                                            jsonObject.put("new_value", newBalance.toString())
                                                                            jsonObject.put("column_name", "balance")
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                        mRequestQueue = Volley.newRequestQueue(this@Register)
                                                                        jsonObjectRequest = JsonObjectRequest(
                                                                            Request.Method.POST,
                                                                            BASE_URL + "/api/update_balnce.php",
                                                                            jsonObject,
                                                                            Response.Listener
                                                                            { response ->
                                                                                try {
                                                                                    Log.d("sucesss",response.toString())
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
                                                            //
                                                            //btn visiblity gone
                                                            sumbit_btn.visibility = View.VISIBLE
                                                            progressBarBTN.visibility = View.GONE
                                                            dialog.dismiss()
                                                            val intent = Intent(this, MainActivity::class.java)
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.d("error", e.toString())
                                                    }
                                                }
                                            }, Response.ErrorListener {
                                                Toast.makeText(this, "Something Errror!", Toast.LENGTH_SHORT)
                                                    .show()
                                            })
                                        mRequestQueue!!.add(jsonArrayRequest)
                                    }
                                }
                                cancel_btn.setOnClickListener{
                                    //balance add   code
                                    dialog.dismiss()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                dialog.show()
                            } catch (e: Exception) {
                                Log.e("error", e.message.toString())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener {
                    binding.linearColumnvolume1.visibility = View.VISIBLE
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(this,"Email in use ! Login",Toast.LENGTH_SHORT).show()
                    Log.d("error", it.message.toString())
                })
            mRequestQueue!!.add(jsonObjectRequest)

        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }
    fun createnewuser() {
        //Our data from this textFiled We Get
        val email = binding.txtEmailaddress.text.toString()
        val password = binding.txtPassword.text.toString()
        //this is a way to sent our post data
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", email)
            jsonObject.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            BASE_URL + "/api/signup.php",
            jsonObject,
            Response.Listener
            { response ->
                try {
                    val respMessage = response.getString("message")
                    //we used this token for auto login
                    tokenforlogin = response.getString("token")
                    //now we save our token into sharedpref for auto login
                    sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("token", tokenforlogin)
                    editor.putString("username", email)
                    editor.apply()
                    if (respMessage == "User was created") {
                        //refer code enter dialog box
                        try {
                            val dialog = Dialog(this)
                            dialog.setContentView(R.layout.row_listgroupadd)
                            dialog.window!!.setLayout(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            dialog.setCancelable(false)
                            dialog.window!!.attributes.windowAnimations = R.style.animation
                            val refercode_felled = dialog.findViewById<EditText>(R.id.edittext)
                            val cancel_btn  = dialog.findViewById<TextView>(R.id.cancel)
                            val sumbit_btn  = dialog.findViewById<TextView>(R.id.save)
                            val progressBarBTN = dialog.findViewById<ProgressBar>(R.id.referProgress)
                            sumbit_btn.setOnClickListener {
                                if(refercode_felled.text.toString().isEmpty()) {
                                    refercode_felled.setError("required")
                                    refercode_felled.requestFocus()
                                } else {
                                    sumbit_btn.visibility = View.GONE
                                    progressBarBTN.visibility = View.VISIBLE
                                    //balance add   code
                                    mRequestQueue = Volley.newRequestQueue(this)
                                    jsonArrayRequest = JsonArrayRequest(Request.Method.GET,
                                        BASE_URL + "/api/get_data.php",
                                        null,
                                        Response.Listener
                                        { response ->
                                            //here we try to find all refer code in loop
                                            //we are used for loop
                                            Log.d("resp", response.toString())
                                            for (i in 0 until response.length()) {
                                                try {
                                                    val responseObj = response.getJSONObject(i)
                                                    val referCode = responseObj.getString("referral_code")
                                                    Log.d("resp", referCode.toString())
                                                    //we are used coditon for check there our textFILED data == refercode with any one
                                                    //after that we run our logic code
                                                    //if any refer code match then we give valid code
                                                    //after that we check which user codes thats it is
                                                    //and thats user refered incrise 1 and also give referal reward to that usser
                                                    //and also aplied for that user who also get referal reward
                                                    //and that user refered is true thats user not aplicable again new code entered
                                                    if (refercode_felled.text.toString() == referCode) {
                                                        //Do that next step
                                                        Toast.makeText(
                                                            this,
                                                            "Appiled",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        //
                                                        mRequestQueue = Volley.newRequestQueue(this@Register)
                                                        jsonObjectRequest = JsonObjectRequest(
                                                            Request.Method.GET,
                                                            BASE_URL + "/api/get_user.php?username=" + responseObj.getString(
                                                                "username"
                                                            ).toString(),
                                                            null,
                                                            Response.Listener
                                                            { response ->
                                                                try {
                                                                    //we used this token for auto login
                                                                    var balance =
                                                                        response.getDouble("balance")
                                                                    var totalRefer =
                                                                        response.getInt("total_refer")

                                                                    // we did our code now thwrw directly
                                                                    val count = totalRefer + 1
                                                                    val jsonObject2 = JSONObject()
                                                                    try {
                                                                        jsonObject2.put(
                                                                            "username",
                                                                            responseObj.getString("username")
                                                                                .toString()
                                                                        )
                                                                        jsonObject2.put(
                                                                            "new_value",
                                                                            count.toString()
                                                                        )
                                                                        jsonObject2.put(
                                                                            "column_name",
                                                                            "total_refer"
                                                                        )
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                    }
                                                                    mRequestQueue = Volley.newRequestQueue(this@Register)
                                                                    jsonObjectRequest = JsonObjectRequest(
                                                                        Request.Method.POST,
                                                                        BASE_URL + "/api/update_reward.php",
                                                                        jsonObject2,
                                                                        Response.Listener
                                                                        { response ->
                                                                            try {
                                                                                Log.d(
                                                                                    "sucesss",
                                                                                    response.toString()
                                                                                )
                                                                            } catch (e: Exception) {
                                                                                e.printStackTrace()
                                                                            }
                                                                        },
                                                                        Response.ErrorListener {
                                                                            Toast.makeText(
                                                                                this@Register,
                                                                                "Something Error In Server.Please Wait Or Try Later",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        })
                                                                    mRequestQueue!!.add(jsonObjectRequest)
                                                                    //update work
                                                                    val newBalance = balance + 10
                                                                    val jsonObject = JSONObject()
                                                                    try {
                                                                        jsonObject.put(
                                                                            "username",
                                                                            responseObj.getString("username")
                                                                                .toString()
                                                                        )
                                                                        jsonObject.put(
                                                                            "new_value",
                                                                            newBalance.toString()
                                                                        )
                                                                        jsonObject.put("column_name", "balance")
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                    }
                                                                    mRequestQueue = Volley.newRequestQueue(this@Register)
                                                                    jsonObjectRequest = JsonObjectRequest(
                                                                        Request.Method.POST,
                                                                        BASE_URL + "/api/update_balnce.php",
                                                                        jsonObject,
                                                                        Response.Listener
                                                                        { response ->
                                                                            try {
                                                                                Log.d(
                                                                                    "sucesss",
                                                                                    response.toString()
                                                                                )
                                                                            } catch (e: Exception) {
                                                                                e.printStackTrace()
                                                                            }
                                                                        },
                                                                        Response.ErrorListener {
                                                                            Log.d(
                                                                                "error",
                                                                                it.message.toString()
                                                                            )
                                                                        })
                                                                    mRequestQueue!!.add(jsonObjectRequest)

                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                }
                                                            },
                                                            Response.ErrorListener {
                                                                Log.d(
                                                                    "error",
                                                                    it.message.toString()
                                                                )
                                                            })
                                                        mRequestQueue!!.add(jsonObjectRequest)
                                                        //
                                                        //btn visiblity gone
                                                        sumbit_btn.visibility = View.VISIBLE
                                                        progressBarBTN.visibility = View.GONE
                                                        dialog.dismiss()
                                                        val intent = Intent(this, MainActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                } catch (e: Exception) {
                                                    Log.d("error", e.toString())
                                                }
                                            }
                                        }, Response.ErrorListener {
                                            Toast.makeText(this, "Something Errror!", Toast.LENGTH_SHORT)
                                                .show()
                                        })
                                    mRequestQueue!!.add(jsonArrayRequest)
                                }
                            }
                            cancel_btn.setOnClickListener{
                                //balance add   code
                                dialog.dismiss()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            dialog.show()
                        } catch (e: Exception) {
                            Log.e("error", e.message.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                binding.txtEmailaddress.setError("Email ID Already In Use")
                Log.d("error", it.message.toString())
                binding.progressBar.visibility = View.GONE
                binding.btnSignUpOne.visibility = View.VISIBLE
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }
}