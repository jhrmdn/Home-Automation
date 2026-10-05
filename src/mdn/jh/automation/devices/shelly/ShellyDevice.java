package mdn.jh.automation.devices.shelly;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.device.DataSinkHandler;
import mdn.jh.automation.device.DataSourceHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.gui.DataSinkCreator;
import mdn.jh.automation.gui.DataSourceCreator;

public class ShellyDevice extends Device {
    private String host, username, password, model = "Shelly";
    private int generation = 1;
    private JSONArray inputs = new JSONArray(), actors = new JSONArray();

    public ShellyDevice() { super(TYPE_SHELLY); }
    public ShellyDevice(String host, String username, String password) throws Exception {
        this(); this.host=normalize(host); this.username=username == null ? "" : username; this.password=password == null ? "" : password; discover();
    }
    private String normalize(String value) { String h=value.trim(); if(!h.startsWith("http://")&&!h.startsWith("https://")) h="http://"+h; while(h.endsWith("/"))h=h.substring(0,h.length()-1); return h; }
    public int getGeneration(){return generation;} public String getHost(){return host;} public JSONArray getInputs(){return inputs;} public JSONArray getActors(){return actors;}
    public boolean isStatefulInput(int id){for(int i=0;i<inputs.length();i++){JSONObject input=inputs.optJSONObject(i);if(input!=null&&input.optInt("id",-1)==id)return !"button".equalsIgnoreCase(input.optString("inputType"));}return false;}
    public boolean hasActor(int id){for(int i=0;i<actors.length();i++){JSONObject actor=actors.optJSONObject(i);if(actor!=null&&actor.optInt("id",-1)==id)return true;}return false;}
    public boolean isCoverActor(int id){for(int i=0;i<actors.length();i++){JSONObject actor=actors.optJSONObject(i);if(actor!=null&&actor.optInt("id",-1)==id){String kind=actor.optString("kind");return "cover".equals(kind)||"roller".equals(kind);}}return false;}
    @Override public String getName(){return model+" - "+host;}

    public void discover() throws Exception {
        JSONObject info=get("/shelly"); generation=info.optInt("gen",1); model=info.optString("model",info.optString("type","Shelly")); inputs=new JSONArray(); actors=new JSONArray();
        if(generation>=2){ JSONObject config=get("/rpc/Shelly.GetConfig"); java.util.Iterator<?> keys=config.keys(); while(keys.hasNext()){String key=String.valueOf(keys.next());if(key.startsWith("input:")) addCapability(inputs,key,config.getJSONObject(key)); else if(key.startsWith("switch:")||key.startsWith("light:")||key.startsWith("cover:")) addCapability(actors,key,config.getJSONObject(key)); } }
        else { JSONObject settings=get("/settings"), status=get("/status"); JSONArray rollers=settings.optJSONArray("rollers"), relays=settings.optJSONArray("relays");boolean coverMode="roller".equalsIgnoreCase(settings.optString("mode"))||rollers!=null;if(coverMode&&rollers!=null)for(int i=0;i<rollers.length();i++)addCapability(actors,"roller:"+i,rollers.getJSONObject(i));else if(relays!=null)for(int i=0;i<relays.length();i++)addCapability(actors,"relay:"+i,relays.getJSONObject(i)); JSONArray configuredInputs=settings.optJSONArray("inputs"), liveInputs=status.optJSONArray("inputs"); if(liveInputs!=null)for(int i=0;i<liveInputs.length();i++)addCapability(inputs,"input:"+i,configuredInputs!=null&&i<configuredInputs.length()?configuredInputs.getJSONObject(i):liveInputs.getJSONObject(i)); }
    }
    private void addCapability(JSONArray target,String key,JSONObject config) throws Exception { JSONObject c=new JSONObject(); c.put("id",Integer.parseInt(key.substring(key.indexOf(':')+1))); c.put("name",config.optString("name",key)); c.put("kind",key.substring(0,key.indexOf(':'))); c.put("inputType",config.optString("type",config.optString("btn_type","switch"))); target.put(c); }
    public boolean readInput(int id) throws Exception { JSONObject s=generation>=2?get("/rpc/Input.GetStatus?id="+id):get("/status").getJSONArray("inputs").getJSONObject(id); return s.optBoolean(generation>=2?"state":"input",false); }
    public void setActor(int id,boolean on) throws Exception { String kind="switch";for(int i=0;i<actors.length();i++){JSONObject actor=actors.optJSONObject(i);if(actor!=null&&actor.optInt("id",-1)==id){kind=actor.optString("kind","switch");break;}}if(generation>=2){if("cover".equals(kind))get("/rpc/Cover."+(on?"Open":"Close")+"?id="+id);else get("/rpc/"+("light".equals(kind)?"Light":"Switch")+".Set?id="+id+"&on="+on);}else if("roller".equals(kind))get("/roller/"+id+"?go="+(on?"open":"close"));else get("/relay/"+id+"?turn="+(on?"on":"off")); }
    public void controlCover(int id,String command)throws Exception{if(!isCoverActor(id))throw new IllegalArgumentException("Shelly channel is not a cover");String normalized=command==null?"":command.toLowerCase();if(!"open".equals(normalized)&&!"close".equals(normalized)&&!"stop".equals(normalized))throw new IllegalArgumentException("Unknown cover command");if(generation>=2)get("/rpc/Cover."+Character.toUpperCase(normalized.charAt(0))+normalized.substring(1)+"?id="+id);else get("/roller/"+id+"?go="+normalized);}
    public String readCoverState(int id)throws Exception{if(!isCoverActor(id))return "unknown";JSONObject status=generation>=2?get("/rpc/Cover.GetStatus?id="+id):get("/roller/"+id);String state=status.optString("state",status.optString("last_direction","unknown")).toLowerCase();if("open".equals(state)||"closed".equals(state)||"opening".equals(state)||"closing".equals(state)||"stopped".equals(state))return state;return status.optBoolean("is_open",false)?"open":state;}
    public JSONObject get(String path) throws Exception { String url=host+path; HttpURLConnection c=open(url,null); int code=c.getResponseCode(); if(code==401 && !password.isEmpty()){String challenge=c.getHeaderField("WWW-Authenticate");c.disconnect();c=open(url,authorization(challenge,"GET",URI.create(url).getRawPath()+(URI.create(url).getRawQuery()==null?"":"?"+URI.create(url).getRawQuery())));code=c.getResponseCode();} if(code<200||code>=300)throw new Exception("Shelly HTTP "+code); try(InputStream in=c.getInputStream();BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);return new JSONObject(b.toString());} }
    private HttpURLConnection open(String url,String auth)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);if(auth!=null)c.setRequestProperty("Authorization",auth);return c;}
    private String authorization(String challenge,String method,String uri)throws Exception{if(challenge==null||challenge.toLowerCase().startsWith("basic"))return "Basic "+Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));Map<String,String> p=new HashMap<>();for(String x:challenge.substring(challenge.indexOf(' ')+1).split(",")){String[]kv=x.trim().split("=",2);if(kv.length==2)p.put(kv[0],kv[1].replaceAll("^\"|\"$",""));}String user=username.isEmpty()?"admin":username,realm=p.get("realm"),nonce=p.get("nonce"),qop=p.getOrDefault("qop","auth").split(",")[0],nc="00000001",cnonce=Long.toHexString(System.nanoTime());String ha1=sha(user+":"+realm+":"+password),ha2=sha(method+":"+uri),response=sha(ha1+":"+nonce+":"+nc+":"+cnonce+":"+qop+":"+ha2);return "Digest username=\""+user+"\", realm=\""+realm+"\", nonce=\""+nonce+"\", uri=\""+uri+"\", algorithm=SHA-256, response=\""+response+"\", qop="+qop+", nc="+nc+", cnonce=\""+cnonce+"\"";}
    private String sha(String s)throws Exception{byte[]d=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}
    @Override public DataSourceHandler getDataSourceHandler(){if(dataSourceHandler==null)dataSourceHandler=new ShellyDataSourceHandler(this);return dataSourceHandler;}
    @Override public DataSinkHandler getDataSinkHandler(){if(dataSinkHandler==null)dataSinkHandler=new ShellyDataSinkHandler(this);return dataSinkHandler;}
    @Override public DataSourceCreator getDataSourceCreator(){return null;} @Override public DataSinkCreator getDataSinkCreator(){return null;}
    @Override protected Node getSpecificStorage(Document doc){Element e=doc.createElement("Shelly");e.setAttribute("host",host);e.setAttribute("username",username);e.setAttribute("password",password);e.setAttribute("generation",Integer.toString(generation));e.setAttribute("model",model);return e;}
    @Override public boolean initSpecific(Node node)throws Exception{NodeList n=node.getChildNodes();Node s=null;for(int i=0;i<n.getLength();i++)if("Shelly".equals(n.item(i).getNodeName()))s=n.item(i);if(s==null)return false;host=s.getAttributes().getNamedItem("host").getNodeValue();username=s.getAttributes().getNamedItem("username").getNodeValue();password=s.getAttributes().getNamedItem("password").getNodeValue();generation=Integer.parseInt(s.getAttributes().getNamedItem("generation").getNodeValue());model=s.getAttributes().getNamedItem("model").getNodeValue();NodeList children=s.getChildNodes();for(int i=0;i<children.getLength();i++){if("DataSources".equals(children.item(i).getNodeName()))dataSourceHandler=new ShellyDataSourceHandler(this,children.item(i));if("DataSinks".equals(children.item(i).getNodeName()))dataSinkHandler=new ShellyDataSinkHandler(this,children.item(i));}try{discover();}catch(Exception ignored){}return true;}
}
