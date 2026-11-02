const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const test=require('node:test');
const source=fs.readFileSync('tomcat/webapps/home-automation/js/display.js','utf8');
function fixture(){
    const inputs=new Map(),sent=[],dialogs=[];
    const node=tag=>({tag,children:[],events:{},append(...children){this.children.push(...children)},querySelectorAll:()=>[],reportValidity:()=>true,addEventListener(name,callback){this.events[name]=callback},showModal(){this.open=true},close(){this.open=false;this.events.close?.()},remove(){this.removed=true}});
    const body=node('body');body.append=dialog=>dialogs.push(dialog);
    const context={document:{createElement:node,body},field:(label,id,type,value)=>{const input={value,checked:false};inputs.set(id,input);return {querySelector:()=>input}},uiText:s=>s,authState:{canWrite:true},send:s=>sent.push(JSON.parse(s.slice(s.indexOf(':')+1))),showToast:message=>{throw Error(message)}};
    vm.createContext(context);
    for(const name of ['shellyCredentialsControl','openShellyCredentialsDialog','deviceSettingsControl']){
        const start=source.indexOf('function '+name+'(');vm.runInContext(source.slice(start,source.indexOf('\n}',start)+2),context);
    }
    const device={id:7,name:'Living room',shelly:true,host:'http://192.0.2.1',username:'admin',updateFrequency:1000};
    const button=context.deviceSettingsControl(device);
    return {inputs,sent,button,dialogs,context};
}
test('Shelly list contains one button and creates credential fields only inside a modal',()=>{
    const f=fixture();assert.equal(f.button.tag,'button');assert.equal(f.inputs.size,0);
    f.button.onclick();assert.equal(f.dialogs.length,1);assert.equal(f.dialogs[0].open,true);
    assert.equal(f.inputs.size,3);assert.equal(f.sent.length,0);
    f.dialogs[0].close();assert.equal(f.dialogs[0].removed,true);assert.equal(f.sent.length,0);
});
test('Shelly popup supports changing, retaining and clearing credentials',()=>{
    const f=fixture(),input=key=>f.inputs.get('shelly-credentials-7-'+key);
    function submit(){f.dialogs.at(-1).children[0].onsubmit({preventDefault(){}})}
    f.button.onclick();assert.equal(input('username').value,'admin');assert.equal(input('password').value,'');
    input('username').value='new-user';submit();assert.equal(f.sent[0].username,'new-user');assert.equal(Object.hasOwn(f.sent[0],'password'),false);
    assert.equal(f.sent[0].host,'http://192.0.2.1');assert.equal(f.sent[0].milliseconds,1000);
    f.button.onclick();input('password').value='new-secret';submit();assert.equal(f.sent[1].password,'new-secret');assert.equal(input('password').value,'');
    f.button.onclick();input('clear-password').checked=true;submit();assert.equal(f.sent[2].password,'');
    f.context.authState.canWrite=false;f.button.onclick();assert.equal(f.dialogs.length,3);
});
