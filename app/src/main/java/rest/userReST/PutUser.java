package rest.userReST;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import entity.User;
import errors.ApiErrorMessage;
import kasper.pagh.keebin.AsyncResponse;
import DB.DatabaseHandler;
import kasper.pagh.keebin.MainActivity;


public class PutUser extends AsyncTask<String, Void, String>
{

    private User user;
    public AsyncResponse delegate = null;
    private String baseUrl;
    private Gson gson;
    private DatabaseHandler dbh;
    private String oldMail;
    private String oldPw;

    public PutUser(String oldPw, User user, AsyncResponse delegate, Context context, String baseUrl,String oldMail)
    {
        this.oldPw = oldPw;
        this.oldMail = oldMail;
        this.user = user;
        this.baseUrl = baseUrl;
        this.delegate = delegate;
        gson = new Gson();
        dbh = new DatabaseHandler(context);
    }

    private String putUser() throws IOException
    {
        JsonObject jo = new JsonObject();

        if(user.getPassword().equalsIgnoreCase(""))
        {
            jo.addProperty("firstName", user.getFirstName());
            jo.addProperty("lastName", user.getLastName());
            jo.addProperty("email", user.getEmail());
            jo.addProperty("birthday", user.getBirthday());
            jo.addProperty("sex", user.getSex());
            jo.addProperty("oldpassword", oldPw);
        }
        jo.addProperty("firstName", user.getFirstName());
        jo.addProperty("lastName", user.getLastName());
        jo.addProperty("email", user.getEmail());
        jo.addProperty("birthday", user.getBirthday());
        jo.addProperty("sex", user.getSex());
        jo.addProperty("oldpassword", oldPw);
        jo.addProperty("password", "");
        jo.addProperty("role", 1);
        Log.d("PUTUSER", "her er JO: " + jo.toString());

        InputStream input = null;
        OutputStream output = null;
        BufferedReader bufferedReader = null;
        StringBuilder sb = null;

        try
        {
            URL url = new URL(baseUrl + "users/user/" + oldMail);
            Log.d("PUTUSR", "her er url: " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("accessToken", dbh.getTokenByName("accessToken").getTokenData());
            connection.setRequestProperty("refreshToken", dbh.getTokenByName("refreshToken").getTokenData());
            output = connection.getOutputStream();
            output.write(gson.toJson(jo).getBytes("UTF-8"));
            output.close();

            connection.connect();

            input = connection.getInputStream();
            String code = "" +connection.getResponseCode();
            if(code.equalsIgnoreCase("200"));
            {
                String accessToken = connection.getHeaderField("accessToken");
                if(!dbh.getTokenByName("accessToken").getTokenData().equals(accessToken))
                {
                    dbh.updateToken("accessToken", accessToken);
                }
            }
            if(code.equalsIgnoreCase("500"))
            {
                ApiErrorMessage error = new ApiErrorMessage("user", "Fejl: Forkert input", "500");
                return error.getErrorMessage();
            }
            if(code.equalsIgnoreCase("401"))
            {
                ApiErrorMessage error = new ApiErrorMessage("user", "Fejl: Manglende rettigheder", "401");
                return error.getErrorMessage();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(input));
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            bufferedReader.close();
            return sb.toString();
        } finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }

    @Override
    protected String doInBackground(String... params)
    {
        try
        {
            return putUser();
        } catch (IOException e)
        {
            e.printStackTrace();
            ApiErrorMessage error = new ApiErrorMessage("shop", "Fejl: netværksfejl, prøv igen senere", "500");
            return error.getErrorMessage();
        }
    }

    @Override
    protected void onPostExecute(String result)
    {
        delegate.processFinished(result);
    }

}

