package com.rxr.mine.RegisterAndLogin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.rxr.mine.MainActivity
import com.rxr.mine.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject

class Login : AppCompatActivity() {
    //Google Login
    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    //manul login
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityLoginBinding
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    val BASE_URL = "https://growwithonline.com/XRX/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //google login
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnVolume.setOnClickListener {
            binding.linearColumnvolume.visibility = View.GONE
            binding.progressBar2.visibility = View.VISIBLE
            signIn()
        }
        //register btn click
        binding.txtDontHaveany.setOnClickListener{
            val intent = Intent(this,Register::class.java)
            startActivity(intent)
            finish()
        }
        //login btn
        binding.btnSignInOne.setOnClickListener {
            //this is check is for every box is fillings not any one null
            validdata()
            //start show a progress bar and hide this signin btn
        }
    }
    private fun validdata() {
        val email = binding.txtEmailaddress.text.toString()
        val password = binding.txtPassword.text.toString()
        if (email.isEmpty()){
            binding.txtEmailaddress.setError("required")
            binding.txtEmailaddress.requestFocus()
        }
        else if(password.isEmpty()){
            binding.txtPassword.setError("required")
            binding.txtPassword.requestFocus()
        }
        else{
            loginUser()
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignInOne.visibility = View.GONE
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
                BASE_URL + "/api/login.php",
                jsonObject,
                Response.Listener
                { response ->
                    try {
                        val respMessage = response.getString("message")
                        Toast.makeText(this, respMessage.toString(), Toast.LENGTH_SHORT).show()
                        if (respMessage == "Login successful") {
                            val username = response.getJSONObject("token").getString("username")
                            sharedPreferences =
                                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("token", "tokenforlogin")
                            editor.putString("username", username)
                            editor.putString("userPic", userPic)
                            editor.apply()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener {
                    binding.linearColumnvolume.visibility = View.VISIBLE
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(this,"Email not register ! register with",Toast.LENGTH_SHORT).show()
                    Log.d("error", it.message.toString())
                })
            mRequestQueue!!.add(jsonObjectRequest)

        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun loginUser() {
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
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "/api/login.php",
            jsonObject,
            Response.Listener
            { response ->
                try {
                    val respMessage = response.getString("message")
                    Toast.makeText(this, respMessage.toString(), Toast.LENGTH_SHORT).show()
                    if (respMessage == "Login successful") {
                        val username = response.getJSONObject("token").getString("username")
                        sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("token", "tokenforlogin")
                        editor.putString("username", username)
                        editor.apply()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                binding.txtEmailaddress.setError("error")
                binding.txtPassword.setError("error")
                Log.d("error", it.message.toString())
                binding.progressBar.visibility = View.GONE
                binding.btnSignInOne.visibility = View.VISIBLE
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("token","0")
        if (data == "0"){
            Log.d("user","Not Login")
        }else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}