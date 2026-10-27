const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const scripts=path.join(__dirname,'../tomcat/webapps/home-automation/js');
function editor(canWrite=true){
    const fields=new Map();function node(tag='div'){const e={tagName:tag.toUpperCase(),children:[],append(...children){this.children.push(...children)},replaceChildren(){this.children=[]},setAttribute(){}};Object.defineProperty(e,'id',{set(id){fields.set(id,e)},get(){return ''}});return e;}
    const c={showDetails(){},additionalDetailRequest:()=>({}),uiText:x=>x,authState:{canWrite},document:{createElement:node,querySelector:s=>fields.get(s.slice(1))||null},
        deviceSelect(label,id,options){const wrap=node(),select=node('select');select.id=id;select.options=options;wrap.querySelector=()=>select;return wrap;}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'cam-switch-editor.js'),'utf8'),c);return{c,fields};
}
const settings={camSwitch:true,camPositionLabels:['Off','Auto','Manual'],outputLabels:['Pump','Valve'],camMatrix:[[false,false],[true,true],[false,true]]};
test('matrix editor has independent counts, labels and one checkbox per position/output',()=>{
    const {c,fields}=editor();c.camSwitchEditor(settings);
    assert.equal(fields.get('cam-position-count').value,'3');assert.equal(fields.get('cam-output-count').value,'2');
    assert.equal(fields.get('cam-position-label-2').value,'Manual');assert.equal(fields.get('cam-bit-1-0').checked,true);assert.equal(fields.get('cam-bit-2-0').checked,false);
    fields.get('cam-bit-2-0').checked=true;fields.get('cam-bit-2-0').onchange();
    const request=c.additionalDetailRequest();assert.deepEqual(JSON.parse(JSON.stringify(request.camMatrix)),[[false,false],[true,true],[true,true]]);
    assert.deepEqual(Array.from(request.camPositionLabels),['Off','Auto','Manual']);
});
test('resizing preserves previously edited labels and matrix cells',()=>{
    const {c,fields}=editor();c.camSwitchEditor(settings);
    fields.get('cam-position-label-2').value='Custom';fields.get('cam-position-label-2').oninput();
    fields.get('cam-position-count').value='2';fields.get('cam-position-count').onchange();
    fields.get('cam-position-count').value='8';fields.get('cam-position-count').onchange();
    fields.get('cam-output-count').value='8';fields.get('cam-output-count').onchange();
    assert.equal(fields.get('cam-position-label-2').value,'Custom');assert.equal(fields.get('cam-bit-2-1').checked,true);assert.equal(fields.get('cam-bit-7-7').checked,false);
    const request=c.additionalDetailRequest();assert.equal(request.camMatrix.length,8);assert.equal(request.camMatrix[7].length,8);
});
test('read-only users cannot edit matrix cells or counts',()=>{
    const {c,fields}=editor(false);c.camSwitchEditor(settings);
    assert.equal(fields.get('cam-position-count').disabled,true);assert.equal(fields.get('cam-position-label-0').disabled,true);assert.equal(fields.get('cam-bit-0-0').disabled,true);
});
test('dashboard sends a Cam Switch action with the selected position',()=>{
    let sent;const data={camSwitch:true,selectorSwitch:true,selectedIndex:2,selectorOptions:settings.camPositionLabels.map((label,value)=>({label,value}))};
    const c={createControlContent(){},updateControlElement(){},editItem(){},values:new Map([[7,data]]),dashboardComponent:()=>({name:'Cam'}),uiText:x=>x,authState:{canAct:true},editingMode:false,send:message=>sent=message,renderStringSelector(box,payload,mode,enabled,select){assert.equal(enabled,true);select(2)}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(scripts,'dashboard-selector.js'),'utf8'),c);c.updateControlElement({querySelector:()=>({})},{type:'selector',componentId:7,displayMode:'rotary'});
    assert.equal(sent,'camSwitch:'+JSON.stringify({id:7,index:2,dashboard:true}));
});
