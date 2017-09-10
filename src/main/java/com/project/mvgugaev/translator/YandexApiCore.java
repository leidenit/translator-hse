package com.project.mvgugaev.translator;

import android.content.Context;
import com.project.mvgugaev.translator.items.Lang;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Yandex api requests core

public class YandexApiCore {
    private static HttpClient client = new DefaultHttpClient();

    // Json to HashMap
    private static HashMap<String, String> jsonToMap(String t) throws JSONException {
        HashMap<String, String> map = new HashMap<>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.getString(key);
            map.put(key, value);
        }
        return map;
    }

    // Load languages mass
    public void setLangArrayList(Context cont) throws IOException
    {
        String address = MyApplication.getYaTranslateUrl() + "/getLangs?ui=ru&key=" + MyApplication.getYaApi();

        try {
            JSONObject dataJsonObj = new JSONObject(sendMassage(address,new ArrayList<NameValuePair>()));
            JSONObject langsJson = dataJsonObj.getJSONObject("langs");
            ArrayList<Lang> langs = new ArrayList<>();

            for (Map.Entry<String, String> entry : jsonToMap(langsJson.toString()).entrySet()) {
                langs.add(new Lang(entry.getValue(),entry.getKey()));
            }

            // Set languages ArrayList
            MyApplication.setMainLangs(langs);
            MyApplication.setLangState();
        }
        catch (JSONException e) { e.printStackTrace(); }
    }

    // Custom send massage methods
    private static String sendMassage(String address, List<NameValuePair> pairs) throws IOException
    {
        HttpPost post = new HttpPost(address);
        post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    // Get disct (and parse) use htl styled for generate dict (very easy and simple methods)
    public String getDict(String text,String langFrom,String langTo) throws IOException
    {
        String address = MyApplication.getYaDictUrl() + "/lookup?ui=ru&lang=" + langFrom + "-" + langTo + "&key=" + MyApplication.getYaDictApi();
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("text", text));

        try {
            JSONObject dataJsonObj = new JSONObject(sendMassage(address,pairs));
            if(useExists(dataJsonObj,"def\":"))
                if(dataJsonObj.getJSONArray("def").length() > 0) {
                    JSONObject parseText = dataJsonObj.getJSONArray("def").getJSONObject(0);
                    JSONArray setArray = parseText.getJSONArray("tr");

                    String res = "<p>" + parseText.getString("text") + " <span style='color:grey; font-size:8px;'> " +
                            (useExists(parseText,"gen\":") ? "(" + parseText.getString("gen") + ")" : "") + "<br>" +
                            (useExists(parseText,"anm\":") ? parseText.getString("anm") + "." : "") +
                            (useExists(parseText,"pos\":") ? parseText.getString("pos") : "") + " " + "</span></p>";

                    for (int i = 0; i < setArray.length(); i++) {
                        JSONObject transObj = setArray.getJSONObject(i);
                        res += "<p> <span style='color:grey; font-size:8px;'>" + (i + 1) + ".</span> " + transObj.getString("text");
                        try {
                            if(useExists(transObj,"syn\":")) {
                                for (int c = 0; c < transObj.getJSONArray("syn").length(); c++) {
                                    JSONObject row = transObj.getJSONArray("syn").getJSONObject(c);
                                    res += ", " + row.getString("text");
                                }
                            }
                        } catch (JSONException e) { e.printStackTrace(); }

                        try {
                            if(useExists(transObj,"mean\":")) {
                                res += " <span style='color:#FF6666;'>(";
                                for (int c = 0; c < transObj.getJSONArray("mean").length(); c++) {
                                    JSONObject row = transObj.getJSONArray("mean").getJSONObject(c);
                                    if (c != 0) res += ", ";
                                    res += row.getString("text");
                                }
                                res += ")</span>";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        res += "<p>";
                    }
                    return res;
                }
        }
        catch (JSONException e) { e.printStackTrace(); }

        return "";
    }

    // Check Json contains field
    private static boolean useExists(JSONObject jsonArray, String text){ return jsonArray.toString().contains(text); }

    // Translate text
    public String translateText(String text,String langFrom,String langTo) throws IOException
    {
        String address = MyApplication.getYaTranslateUrl() + "/translate?format=html&lang=" + langFrom + "-" + langTo + "&key=" + MyApplication.getYaApi();
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("text", text));
        try {
            JSONObject dataJsonObj = new JSONObject(sendMassage(address,pairs));
            JSONArray translateArray = new JSONArray();
            if(useExists(dataJsonObj,"text\":"))
                translateArray  = dataJsonObj.getJSONArray("text");
            return URLDecoder.decode(translateArray.toString(), "UTF-8");
        }
        catch (JSONException e) { e.printStackTrace(); }
        return "";
    }
}
