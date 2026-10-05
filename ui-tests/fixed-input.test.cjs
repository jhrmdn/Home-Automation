const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),path=require('node:path');
function load(data={valueType:'integer',fixedMinimum:10,fixedMaximum:90}) {
    const fields=new Map();const field=id=>{if(!fields.has(id))fields.set(id,{value:'',addEventListener(){}});return fields.get(id);};
    const item={id:'slider',type:'fixedInput',componentId:1,inputMode:'keypad'};
    const c={Intl,Number,Map,navigator:{language:'de'},document:{documentElement:{lang:'de'},querySelector:field},
        controlCatalog(){},createControlContent(){},updateControlElement(){},handleMessage(){},setConnection(){},editItem(){},
        findEditingItem:()=>item,dashboardComponent:()=>data,saveControl:()=>{c.saved=true;},uiText:x=>x,toast:x=>{c.error=x;}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(__dirname,'../tomcat/webapps/home-automation/js/dashboard-fixed-input.js'),'utf8'),c);
    return {c,item,field};
}
test('source limits include endpoints, handle optional limits and respect float precision',()=>{
    const {c}=load();assert.equal(c.fixedValueWithinBounds(10,{valueType:'integer',fixedMinimum:10,fixedMaximum:90}),true);
    assert.equal(c.fixedValueWithinBounds(91,{valueType:'integer',fixedMinimum:10,fixedMaximum:90}),false);
    assert.equal(c.fixedValueWithinBounds(-100,{valueType:'integer',fixedMaximum:10}),true);
    assert.equal(c.fixedValueWithinBounds(.1,{valueType:'float',fixedMinimum:.1,fixedMaximum:.3}),true);
    assert.equal(c.fixedValueWithinBounds(.4,{valueType:'float',fixedMinimum:.1,fixedMaximum:.3}),false);
});
test('slider uses the intersection with current source limits and disables disjoint ranges',()=>{
    const {c}=load();const bounds=c.fixedSliderBounds({minimum:0,maximum:100},{valueType:'integer',fixedMinimum:10,fixedMaximum:90});
    assert.equal(bounds.minimum,10);assert.equal(bounds.maximum,90);assert.equal(bounds.valid,true);
    assert.equal(c.fixedSliderBounds({minimum:0,maximum:5},{fixedMinimum:10}).valid,false);
});
test('slider settings require both bounds, whole integer limits and a range inside Automation limits',()=>{
    const {c,item,field}=load();field('#fixed-input-mode').value='slider';const event={preventDefault(){}};
    field('#fixed-slider-minimum').value='';field('#fixed-slider-maximum').value='90';c.saveControl(event);assert.equal(c.saved,undefined);
    field('#fixed-slider-minimum').value='0';c.saveControl(event);assert.match(c.error,/Automation limits/);assert.equal(c.saved,undefined);
    field('#fixed-slider-minimum').value='10.5';c.saveControl(event);assert.equal(c.saved,undefined);
    field('#fixed-slider-minimum').value='10';c.saveControl(event);assert.equal(c.saved,true);assert.equal(item.minimum,10);assert.equal(item.maximum,90);assert.equal(item.inputMode,'slider');
});
