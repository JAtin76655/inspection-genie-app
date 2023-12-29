package com.summonelec.inspectiongenie.UserLogin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.jose.myfoodcourt.api.APIInterface.ApiInterface
import com.summonelec.inspectiongenie.API.APIClient.ApiClientClass
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SharedHelper.putKey
import com.summonelec.inspectiongenie.Inspection.HomeActivity
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.databinding.ActivityLoginBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.sql.ResultSet
import java.sql.SQLException

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var progressAlert: ProgressAlert
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            val struser = binding.etEmail.text.toString()
            val strpass = binding.etPassword.text.toString()
            if (struser.equals("")) {
                binding.etEmail.error = "Please enter email id"

            }
            else if(!checkemail(struser))
            {
                binding.etEmail.error = "Please enter valid email id"
            }
            else if (strpass.equals("")) {
                binding.etPassword.error = "Please enter valid password"
            } else {

                login(struser, strpass)
            }
        })
    }
    fun checkemail(s:String):Boolean
    {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val inputText = s.toString()

        if (inputText.matches(Regex(emailPattern))) {
            // Valid email pattern
            return true
        } else {
            // Invalid email pattern
            return false
        }
    }

    fun login(email: String?, password: String?) {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    val query = "SELECT * FROM user_master WHERE email='" + email + "' AND password='" + password + "' "
                    Log.e("Login query", query)
                    val statement = connection.createStatement()
                    val resultSet: ResultSet = statement.executeQuery(query)
                    val rowCount = if (resultSet.next()) resultSet.getInt(1) else 0
                    if (rowCount != 0) {
                        var id = 0
                        var name = "0"
                        var mail = "0"
                        resultSet.first()
                        id = resultSet.getInt("id")
                        name = resultSet.getString("name")
                        mail = resultSet.getString("email")
                        val password = resultSet.getString("password")
                        println("ID: $id, Name: $name, Email: $mail, Password: $password")
                        putKey(this@LoginActivity, "USERID", ""+id)
                        putKey(this@LoginActivity, "login", "yes")
                        putKey(this@LoginActivity, "user_name", name)
                        putKey(this@LoginActivity, "user_email", mail)
                       // putKey(this@LoginActivity, "user_image", result.body()?.user_image)
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        Log.e("Login message", "successs...")
                        progressAlert.dismissAlert()

                    } else {
                        Log.e("Login message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            binding.etEmail.error = "Please enter vaild email id"
                            binding.etPassword.error = "Please enter valid password"
                            Toast.makeText(this@LoginActivity, "Login Failed...", Toast.LENGTH_SHORT).show()
                        }


                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }
        }
//        val apiInterface = ApiClientClass.getInstance().create(ApiInterface::class.java)
//        //val call = apiInterface.loginUser("user_login", email, password)
//        GlobalScope.launch {
//            val result = apiInterface.loginUser(email,password)
//            if (result.isSuccessful){
//                progressAlert.dismissAlert()
//                Log.d("server login data: ", result.body().toString())
//                putKey(this@LoginActivity, "USERID", result.body()?.userId.toString())
//                putKey(this@LoginActivity, "login", "yes")
//                putKey(this@LoginActivity, "user_name", result.body()?.name)
//                putKey(this@LoginActivity, "user_email", result.body()?.email)
//                putKey(this@LoginActivity, "user_image", result.body()?.user_image)
//                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                finish()
//            }
//            else
//            {
//                progressAlert.dismissAlert()
//                Log.e("login error",result.errorBody().toString())
//            }
        // Checking the results

    }
}

