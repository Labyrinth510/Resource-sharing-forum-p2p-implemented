package web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class ServerRequester {
    private InetSocketAddress serverAddress;

    public ServerRequester(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public JSONObject register(String username, String hashedValue){
        JSONObject object=new JSONObject();
        object.put("username",username);
        object.put("passwordHashedValue",hashedValue);
        String response=HTTPMethods.HTTPPost("http://"+serverAddress.getHostName()+"/api/user/register.php",object);
        return JSON.parseObject(response);

    }

    public JSONObject login(String username, String hashedValue){
        JSONObject object=new JSONObject();
        object.put("username",username);
        object.put("passwordHashedValue",hashedValue);
        String response=HTTPMethods.HTTPPost("http://"+serverAddress.getHostName()+"/api/user/login.php",object);
        return JSON.parseObject(response);
    }

    public JSONObject update(String username, String requestType, long point){
        JSONObject object=new JSONObject();
        object.put("username",username);
        object.put("request-type",requestType);
        JSONObject dataObject=new JSONObject();
        dataObject.put("point",point);
        object.put("data",dataObject);
//        System.out.println(object.toJSONString());
        String response=HTTPMethods.HTTPPut("http://"+serverAddress.getHostName()+"/api/user/update.php",object);
        return JSON.parseObject(response);
    }

    public JSONObject getuserinfo(String username){
        JSONObject object=new JSONObject();
        object.put("username",username);
        String response=HTTPMethods.HTTPPost("http://"+serverAddress.getHostName()+"/api/user/get-userinfo.php",object);
        return JSON.parseObject(response);
    }




    public JSONObject publish(String username, String filename, ArrayList<String> tags, int lowestPrestige, int pointsRequiredPerUnit, long fileSize, ArrayList<String> hashlist){
        JSONObject object=new JSONObject();
        object.put("username",username);
        JSONObject dataObject=new JSONObject();
        dataObject.put("filename",filename);
//        ArrayList<String> tags = new ArrayList<>();
        JSONArray jstags = new JSONArray();
        jstags.addAll(tags);

        dataObject.put("tags",jstags);
        dataObject.put("lowestPrestige",lowestPrestige);
        dataObject.put("pointsRequiredPerUnit",pointsRequiredPerUnit);
        dataObject.put("fileSize",fileSize);

        JSONArray hashArray=new JSONArray();
        hashArray.addAll(hashlist);
//        JSONArray jshashlist= new JSONArray();

        dataObject.put("hashList",hashArray);
        object.put("data",dataObject);
//        System.out.println(dataObject.toJSONString());
        String response=HTTPMethods.HTTPPost("http://"+serverAddress.getHostName()+"/api/file/publish.php",object);
        //System.out.println(response);
        return JSON.parseObject(response);

    }

    public JSONObject searchbyname(String searchString) {
        JSONObject object = new JSONObject();
        object.put("searchString", searchString);
        String response = HTTPMethods.HTTPPost("http://" + serverAddress.getHostName() + "/api/file/search-by-name.php", object);
        //System.out.print(response);
        return JSON.parseObject(response);
    }

    public JSONObject searchbytags(String[] searchTags) {
        JSONArray tagsArray=new JSONArray();
        JSONObject object=new JSONObject();
        ArrayList<String> tagsList=new ArrayList<>(Arrays.asList(searchTags));
        tagsArray.addAll(tagsList);
        object.put("searchTags", tagsArray);
        String response = HTTPMethods.HTTPPost("http://" + serverAddress.getHostName() + "/api/file/search-by-tags.php", object);
        System.out.println(object.toJSONString());
        System.out.println(response);
        return JSON.parseObject(response);
    }

    public JSONObject download(int fid, String username) {
        JSONObject object = new JSONObject();
        object.put("fid", fid);
        object.put("username", username);
        object.put("clientIP",getHostIp());

        System.out.println("Download send:"+object.toJSONString());
        String response = HTTPMethods.HTTPPost("http://" + serverAddress.getHostName() + "/api/file/download.php", object);

        System.out.print(response);
        return JSON.parseObject(response);
    }

    public JSONObject getgrouplist(int fid) {
        JSONObject object = new JSONObject();
        object.put("fid", fid);
        object.put("clientIP",getHostIp());
        System.out.println("Get group list send:"+object.toJSONString());
        String response = HTTPMethods.HTTPPost("http://" + serverAddress.getHostName() + "/api/group/get-group-list.php", object);
        return JSON.parseObject(response);
    }

    public JSONObject exitgroup(int fid) {
        System.out.println("request to exit group");
        JSONObject object = new JSONObject();
        object.put("fid", fid);
        object.put("clientIP",getHostIp());
        String response = HTTPMethods.HTTPDelete("http://" + serverAddress.getHostName() + "/api/group/exit-group.php", object);
        return JSON.parseObject(response);
    }

    private static String getHostIp(){
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() 
                            && ip.getHostAddress().indexOf(":")==-1){
                        System.out.println("local IP = " + ip.getHostAddress());
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


}