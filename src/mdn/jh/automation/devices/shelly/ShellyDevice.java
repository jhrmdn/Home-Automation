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
    public boolean isCoverActor(int id){for(int i=0;i<actors.length();i++){JSONObject actor=actors.optJSONObject(i);if(actor!=null&&actor.optInt("id",-1)==id){String kind=actor.optString("kind");if("cover".equals(kind)||"roller".equals(kind))return true;}}return false;}
    @Override public String getName(){return model+" - "+host;}

    public void discover() throws Exception {
        JSONObject info=get("/shelly"); generation=info.optInt("gen",1); model=info.optString("model",info.optString("type","Shelly")); inputs=new JSONArray(); actors=new JSONArray();
        if(generation>=2){
            JSONObject config=get("/rpc/Shelly.GetConfig");
            java.util.Iterator<?> keys=config.keys();
            while(keys.hasNext()){
                String key=String.valueOf(keys.next()), kind=key.split(":")[0];
                if(key.startsWith("input:")) addCapability(inputs,key,config.getJSONObject(key));
                else if(java.util.Set.of("switch","light","cover","rgb","rgbw","cct","rgbcct").contains(kind)) addCapability(actors,key,config.getJSONObject(key));
            }
        } else {
            JSONObject settings=get("/settings"), status=get("/status");
            boolean coverMode="roller".equalsIgnoreCase(settings.optString("mode"))
                    || (!settings.has("mode") && settings.optJSONArray("rollers")!=null && settings.getJSONArray("rollers").length()>0);
            if(coverMode)addArray(actors, "roller", settings.optJSONArray("rollers"));
            if(!coverMode && actors.length()==0) addArray(actors,"relay",settings.optJSONArray("relays"));
            // Gen1 dimmers, bulbs, RGBW controllers expose lights, not relays.
            String lightKind="color".equals(settings.optString("mode"))?"color":("SHRGBW2".equals(model)?"white":"light");
            addArray(actors,lightKind,settings.optJSONArray("lights"));
            if(actors.length()==0) {
                addArray(actors,coverMode?"roller":"relay",status.optJSONArray(coverMode?"rollers":"relays"));
                addArray(actors,lightKind,status.optJSONArray("lights"));
            }
            JSONArray configured=settings.optJSONArray("inputs"), live=status.optJSONArray("inputs");
            if(live!=null)for(int i=0;i<live.length();i++)addCapability(inputs,"input:"+i,configured!=null&&i<configured.length()?configured.getJSONObject(i):live.getJSONObject(i));
            JSONArray adc=status.optJSONArray("adcs");
            if(adc!=null)for(int i=0;i<adc.length();i++)addCapability(inputs,"adc:"+i,new JSONObject().put("type","analog"));
        }
    }
    private void addArray(JSONArray target,String kind,JSONArray values)throws Exception{
        if(values!=null)for(int i=0;i<values.length();i++)addCapability(target,kind+":"+i,values.getJSONObject(i));
    }
    private void addCapability(JSONArray target,String key,JSONObject config) throws Exception {
        JSONObject c=new JSONObject(); String kind=key.substring(0,key.indexOf(':'));
        c.put("id",Integer.parseInt(key.substring(key.indexOf(':')+1))); c.put("key",key);
        String name=config.optString("name",key); c.put("name",name.isBlank()||"null".equals(name)?key:name);
        c.put("kind",kind); c.put("inputType",config.optString("type",config.optString("btn_type","switch")));
        c.put("enabled",config.optBoolean("enable",true));
        c.put("analog",java.util.Set.of("light","white","color","rgb","rgbw","cct","rgbcct").contains(kind)||"analog".equals(c.optString("inputType"))||"count".equals(c.optString("inputType")));
        target.put(c);
    }
    public JSONObject capability(JSONArray list,String channel)throws Exception{
        JSONObject found=null;
        for(int i=0;i<list.length();i++){
            JSONObject c=list.getJSONObject(i);
            if(channel.equals(c.optString("key")))return c;
            if(channel.equals(Integer.toString(c.getInt("id")))){
                if(found!=null)throw new IllegalArgumentException("Ambiguous Shelly channel; select its component type");
                found=c;
            }
        }
        if(found==null)throw new IllegalArgumentException("Unavailable Shelly channel: "+channel);
        return found;
    }
    public ShellyDataSource createSource(String channel)throws Exception{
        JSONObject c=capability(inputs,channel);
        if(!c.optBoolean("enabled",true)||"button".equalsIgnoreCase(c.optString("inputType")))throw new IllegalArgumentException("Shelly input is disabled or event-only and cannot be polled");
        return new ShellyDataSource(c.getInt("id"),c.getString("key"),c.optBoolean("analog"));
    }
    public mdn.jh.automation.io.sink.DataSink createSink(String channel)throws Exception{
        JSONObject c=capability(actors,channel);String kind=c.getString("kind");
        if("cover".equals(kind)||"roller".equals(kind))return new ShellyCoverDataSink(this,c.getInt("id"));
        return new ShellyDataSink(this,c.getInt("id"),c.getString("key"),c.optBoolean("analog"));
    }
    public Object readInputValue(String channel)throws Exception{
        JSONObject c=capability(inputs,channel);int id=c.getInt("id");String type=c.optString("inputType");
        JSONObject status=generation>=2?get("/rpc/Input.GetStatus?id="+id):get("/status").getJSONArray("adc".equals(c.getString("kind"))?"adcs":"inputs").getJSONObject(id);
        Object value;
        if("adc".equals(c.getString("kind")))value=status.get("voltage");
        else if("analog".equals(type))value=status.get("percent");
        else if("count".equals(type))value=status.getJSONObject("counts").get("total");
        else value=status.get(generation>=2?"state":"input");
        if(value==JSONObject.NULL)throw new IllegalStateException("Shelly input has no current value");
        if(c.optBoolean("analog")){double number=((Number)value).doubleValue();if(!Double.isFinite(number))throw new IllegalStateException("Invalid Shelly measurement");return number;}
        if(value instanceof Boolean)return value;
        if(value instanceof Number)return ((Number)value).intValue()!=0;
        throw new IllegalStateException("Invalid Shelly input state");
    }
    public boolean readInput(int id)throws Exception{return (Boolean)readInputValue(Integer.toString(id));}
    private String actorEndpoint(JSONObject c){
        String kind=c.optString("kind");int id=c.optInt("id");
        if(generation<2)return "/"+kind+"/"+id+"?";
        String rpc=switch(kind){case "rgb"->"RGB";case "rgbw"->"RGBW";case "cct"->"CCT";case "rgbcct"->"RGBCCT";default->Character.toUpperCase(kind.charAt(0))+kind.substring(1);};
        return "/rpc/"+rpc+".Set?id="+id+"&";
    }
    public void setActor(String channel,boolean on)throws Exception{
        JSONObject c=capability(actors,channel);String kind=c.optString("kind");
        if("cover".equals(kind)||"roller".equals(kind)){controlCover(c.getInt("id"),on?"open":"close");return;}
        get(actorEndpoint(c)+(generation>=2?"on="+on:"turn="+(on?"on":"off")));
    }
    public void setActor(int id,boolean on)throws Exception{setActor(Integer.toString(id),on);}
    public void setActorLevel(String channel,double level)throws Exception{
        JSONObject c=capability(actors,channel);
        if(!c.optBoolean("analog")||!Double.isFinite(level)||level<0||level>100)throw new IllegalArgumentException("Shelly output level must be between 0 and 100%");
        // Gen1 color mode uses gain for overall brightness.
        String parameter=generation<2&&"color".equals(c.optString("kind"))?"gain":"brightness";
        get(actorEndpoint(c)+(generation>=2?"on="+(level>0):"turn="+(level>0?"on":"off"))+"&"+parameter+"="+level);
    }
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
