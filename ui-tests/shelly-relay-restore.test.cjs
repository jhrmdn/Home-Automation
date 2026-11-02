const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const test=require('node:test');
const source=fs.readFileSync('tomcat/webapps/home-automation/js/display.js','utf8');
const requestFunction=source.split('\n').find(line=>line.startsWith('function additionalDetailRequest(){'));
test('relay restoration details preserve checked, unchecked and absent values',()=>{
    let checkbox={checked:false};
    const context={document:{querySelector:selector=>selector==='#details-shelly-restore-once'?checkbox:null}};
    vm.createContext(context);vm.runInContext(requestFunction,context);
    assert.equal(context.additionalDetailRequest().shellyRestoreInputOnce,false);
    checkbox.checked=true;assert.equal(context.additionalDetailRequest().shellyRestoreInputOnce,true);
    checkbox=null;assert.equal(Object.hasOwn(context.additionalDetailRequest(),'shellyRestoreInputOnce'),false);
});
