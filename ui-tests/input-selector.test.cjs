const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),path=require('node:path');
const scripts=path.join(__dirname,'../tomcat/webapps/home-automation/js');
function element(tag='div'){
    const classes=new Set();
    return {tagName:tag.toUpperCase(),children:[],dataset:{},attributes:{},classList:{add:x=>classes.add(x),remove:x=>classes.delete(x),toggle(x,on){on?classes.add(x):classes.delete(x);},contains:x=>classes.has(x)},
        append(...items){this.children.push(...items)},replaceChildren(){this.children=[]},setAttribute(key,value){this.attributes[key]=value},add(option){this.children.push(option)},
        querySelectorAll(){return this.children.flatMap(child=>child.tagName==='LABEL'?child.children.filter(c=>c.tagName==='INPUT'):[child]).filter(child=>['SELECT','INPUT','BUTTON'].includes(child.tagName))}};
}
function loadRenderer(){
    const c={document:{createElement:element,createTextNode:text=>({text})},uiText:x=>x,Option:function(text,value){this.text=text;this.value=value}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'string-selector.js'),'utf8'),c);return c;
}
const data={inputSelector:true,selectorOptions:Array.from({length:5},(_,i)=>({label:(i+1)+' · Input '+(i+1),value:i})),selectedIndex:2};
test('five-position switch highlights selected input, sends index and respects permissions',()=>{
    const c=loadRenderer(),box=element();let selected;
    c.renderStringSelector(box,data,'rotary',true,index=>selected=index);
    assert.equal(box.children.length,5);assert.equal(box.children[2].attributes['aria-pressed'],'true');
    assert.equal(box.children[0].textContent,'1 · Input 1');box.children[4].onclick();assert.equal(selected,4);
    c.renderStringSelector(box,{...data,selectedIndex:4},'rotary',false,()=>{});
    assert.ok(box.children.every(button=>button.disabled));assert.equal(box.children[4].attributes['aria-pressed'],'true');
});
test('combo and radio have five input positions while String Selector keeps its default',()=>{
    const c=loadRenderer();
    for(const mode of ['combo','radio']){
        const box=element();c.renderStringSelector(box,data,mode,true,()=>{});
        const choices=mode==='combo'?box.children[0].children:box.children.map(label=>label.children[0]);
        assert.equal(choices.length,5);assert.equal(Number(choices[0].value),0);assert.equal(Number(choices[4].value),4);
    }
    const box=element();c.renderStringSelector(box,{stringSelector:true,selectorOptions:data.selectorOptions,selectedIndex:-1},'combo',true,()=>{});
    assert.equal(box.children[0].children.length,6);assert.equal(box.children[0].children[0].value,-1);
});
test('reusing an existing widget uses the latest target callback',()=>{
    const c=loadRenderer(),box=element();let target;
    c.renderStringSelector(box,data,'rotary',true,()=>target='old');
    c.renderStringSelector(box,data,'rotary',true,()=>target='new');box.children[0].onclick();assert.equal(target,'new');
});
test('dashboard operates input selector through its action and disables it in edit mode',()=>{
    const box=element(),title={},sent=[];
    const c={createControlContent(){},updateControlElement(){},editItem(){},values:new Map([[7,data]]),dashboardComponent:()=>({name:'Route',...data}),uiText:x=>x,authState:{canAct:true},editingMode:false,
        send:message=>sent.push(message),renderStringSelector(container,payload,mode,enabled,select){c.enabled=enabled;c.payload=payload;select(3)}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'dashboard-selector.js'),'utf8'),c);
    const card={querySelector:selector=>selector==='.selector-title'?title:box};
    c.updateControlElement(card,{type:'selector',componentId:7,displayMode:'rotary'});
    assert.equal(c.enabled,true);assert.equal(title.textContent,'Route');
    assert.deepEqual(JSON.parse(sent[0].slice('inputSelect:'.length)),{id:7,index:3,dashboard:true});
    c.editingMode=true;c.updateControlElement(card,{type:'selector',componentId:7});assert.equal(c.enabled,false);
    c.editingMode=false;c.authState.canAct=false;c.updateControlElement(card,{type:'selector',componentId:7});assert.equal(c.enabled,false);
});

test('Automation ports display positions 1–5 and labels while keeping connection indices',()=>{
    const ports=element();ports.classList.toggle=()=>{};
    const c={document:{createElement:element},uiText:x=>x};vm.createContext(c);
    const source=fs.readFileSync(path.join(scripts,'display.js'),'utf8');
    vm.runInContext(source.match(/^function renderInputPorts\(.*$/m)[0],c);
    c.renderInputPorts({querySelector:()=>ports},{inputSelector:true,inputs:Array.from({length:5},(_,i)=>({id:i,name:(i+1)+' · Mode '+i}))});
    assert.equal(ports.children[0].textContent,'1 · Mode 0');assert.equal(ports.children[4].textContent,'5 · Mode 4');
    assert.equal(ports.children[4].dataset.input,4);
});

test('position controls support two through eight outputs without an extra default position',()=>{
    const c=loadRenderer();
    for(const count of [2,8]){
        const box=element(),payload={selectorSwitch:true,selectorOptions:Array.from({length:count},(_,i)=>({label:String(i+1),value:i})),selectedIndex:count-1};
        c.renderStringSelector(box,payload,'rotary',true,()=>{});
        assert.equal(box.children.length,count);assert.equal(box.children[count-1].attributes['aria-pressed'],'true');
    }
});

test('dynamic input/output count fields preserve labels and submit the chosen count',()=>{
    for(const output of [false,true]){
        const fields=new Map(),prefix=output?'output-selector':'input-selector';
        const c={renderWebControl(){},showDetails(){},additionalDetailRequest:()=>({}),authState:{canWrite:true},uiText:x=>x,
            document:{createElement:element,querySelector:selector=>fields.get(selector.slice(1))||null},
            deviceSelect(label,id,options){const wrap=element(),select=element('select');select.id=id;select.options=options;fields.set(id,select);wrap.querySelector=()=>select;return wrap;},
            field(label,id,type,value){const wrap=element(),input=element('input');input.id=id;input.value=value;fields.set(id,input);wrap.querySelector=()=>input;return wrap;}};
        if(!output)fields.set('input-selector-type',{value:'string'});
        vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'automation-selector.js'),'utf8'),c);
        c.selectorPositionFields(element(),['Auto','Manual'],output);
        const count=fields.get(prefix+'-count');assert.equal(count.options.length,7);
        count.value='8';count.onchange();const last=fields.get(prefix+'-label-7');last.value='Eighth';last.oninput();
        count.value='2';count.onchange();count.value='8';count.onchange();assert.equal(fields.get(prefix+'-label-7').value,'Eighth');
        const request=c.additionalDetailRequest(),labels=output?request.outputLabels:request.inputLabels;
        assert.equal(labels.length,8);assert.equal(labels[0],'Auto');assert.equal(labels[7],'Eighth');
    }
});

test('Selector Switch dashboard uses its own action and exposes every output for wiring',()=>{
    const selected=[],sent=[],box=element();
    const switchData={selectorSwitch:true,selectorOptions:Array.from({length:8},(_,i)=>({label:String(i+1),value:i})),selectedIndex:7};
    const c={createControlContent(){},updateControlElement(){},editItem(){},values:new Map([[77,switchData]]),dashboardComponent:()=>({name:'Switch'}),uiText:x=>x,authState:{canAct:true,canWrite:true},editingMode:false,
        send:m=>sent.push(m),renderStringSelector(container,data,mode,enabled,select){select(7)},selectCustomOutput:(id,index)=>selected.push([id,index])};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'dashboard-selector.js'),'utf8'),c);
    c.updateControlElement({querySelector:s=>s==='.selector-title'?{}:box},{type:'selector',componentId:77,displayMode:'rotary'});
    assert.equal(sent[0],'selectorSwitch:'+JSON.stringify({id:77,index:7,dashboard:true}));
    const source=fs.readFileSync(path.join(scripts,'custom-components.js'),'utf8');
    vm.runInContext(source.slice(source.indexOf('function renderCustomOutputs('),source.indexOf('\nfunction renderEmbeddedSources(')),c);
    c.document={createElement:element};
    c.renderCustomOutputs({querySelector:()=>box},{id:77,selectorSwitch:true,outputs:Array.from({length:8},(_,i)=>({index:i,name:String(i+1),type:'Boolean',value:String(i===7),valid:true}))});
    assert.equal(box.children.length,8);assert.equal(box.children[7].dataset.output,7);
    box.children[7].onclick({stopPropagation(){}});assert.deepEqual(selected,[[77,7]]);
});
