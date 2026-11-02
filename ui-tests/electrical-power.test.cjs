const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const test=require('node:test');
const display=fs.readFileSync('tomcat/webapps/home-automation/js/display.js','utf8');
const dashboard=fs.readFileSync('tomcat/webapps/home-automation/js/dashboard.js','utf8');
test('Automation displays watts including zero and marks unavailable readings',()=>{
    let power;const context={document:{createElement:()=>({classList:{toggle(){}},remove(){power=undefined}})},uiText:s=>s,Intl};
    vm.createContext(context);vm.runInContext(display.slice(display.indexOf('function renderElectricalPower(')),context);
    const card={querySelector:()=>power,append:node=>power=node};
    context.renderElectricalPower(card,{powerAvailable:true,powerValid:true,powerWatts:0});assert.equal(power.textContent,'Electrical power: 0 W');
    context.renderElectricalPower(card,{powerAvailable:true,powerValid:false,powerWatts:null});assert.equal(power.textContent,'Electrical power: Unavailable');
    context.renderElectricalPower(card,{});assert.equal(power,undefined);
});
test('dashboard power fields show watts and do not turn invalid values into zero',()=>{
    const output={},element={querySelector:selector=>selector==='.dashboard-value'?output:null,classList:{toggle(){}},style:{}};
    const metric={id:-14,powerMetric:'watts',datatype:'Number'};
    const context={values:new Map([[-14,{value:'12.345',valid:true}]]),dashboardComponent:()=>metric,isStringComponent:()=>false,dashboardValueColor:()=>'',uiText:s=>s};
    vm.createContext(context);vm.runInContext(dashboard.split('\n').find(line=>line.startsWith('function updateValueElement(')),context);
    context.updateValueElement(element,{componentId:-14});assert.equal(output.textContent,'12.345 W');
    context.values.set(-14,{value:'',valid:false});context.updateValueElement(element,{componentId:-14});assert.equal(output.textContent,'Nicht verfügbar');
});
test('power catalog arriving after the first measurement becomes selectable without a reload',()=>{
    const context={catalog:[{id:1},{id:-14,powerMetric:'watts'}],setValues(){},renderValues(){},newDashboardColorItems:new Set()};
    vm.createContext(context);vm.runInContext(dashboard.split('\n').find(line=>line.startsWith('function handleMessage(')),context);
    context.handleMessage({powerCatalog:[{id:-24,powerMetric:'watts',datatype:'Number'}],dashboardValues:[]});
    assert.deepEqual(context.catalog.map(item=>item.id),[1,-24]);
});
