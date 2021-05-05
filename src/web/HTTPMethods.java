package web;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPMethods {
    public static String HTTPPost(String url, Object object){
        String response="error";
        try{
            URL urlObj=new URL(url);
            HttpURLConnection connection=(HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
            connection.connect();

            String body= JSON.toJSONString(object);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(body);
            writer.close();

            int responseCode=connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                InputStream inputStream=connection.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder responseBuilder=new StringBuilder();
                while((line=reader.readLine())!=null){
                    if(line.length()>0){
                        responseBuilder.append("\n");
                    }
                    responseBuilder.append(line);
                }
                response=responseBuilder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }
    public static String HTTPPut(String url, Object object){
        String response="error";
        try{
            URL urlObj=new URL(url);
            HttpURLConnection connection=(HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
            connection.connect();

            String body= JSON.toJSONString(object);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(body);
            writer.close();

            int responseCode=connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                InputStream inputStream=connection.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder responseBuilder=new StringBuilder();
                while((line=reader.readLine())!=null){
                    if(line.length()>0){
                        responseBuilder.append("\n");
                    }
                    responseBuilder.append(line);
                }
                response=responseBuilder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }
    public static String HTTPDelete(String url, Object object){
        String response="error";
        try{
            URL urlObj=new URL(url);
            HttpURLConnection connection=(HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
            connection.connect();

            String body= JSON.toJSONString(object);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(body);
            writer.close();

            int responseCode=connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                InputStream inputStream=connection.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder responseBuilder=new StringBuilder();
                while((line=reader.readLine())!=null){
                    if(line.length()>0){
                        responseBuilder.append("\n");
                    }
                    responseBuilder.append(line);
                }
                response=responseBuilder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }
}
