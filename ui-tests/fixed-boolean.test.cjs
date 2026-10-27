const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
test('Boolean menu creates true and false sources through the fixed-value API',()=>{
    const root=path.join(__dirname,'../tomcat/webapps/home-automation');
    const html=fs.readFileSync(path.join(root,'index.html'),'utf8');
    assert.match(html,/onclick="addFixedBoolean\(true\)">Fixed True/);
    assert.match(html,/onclick="addFixedBoolean\(false\)">Fixed False/);
    const script=fs.readFileSync(path.join(root,'js/display.js'),'utf8');
    const sent=[],context={send:message=>sent.push(message)};vm.createContext(context);
    vm.runInContext(script.match(/^function addFixedBoolean\(.*$/m)[0],context);
    context.addFixedBoolean(true);context.addFixedBoolean(false);
    assert.deepEqual(sent.map(message=>JSON.parse(message.slice('addFixedValue:'.length))),[{valueType:'boolean',value:true},{valueType:'boolean',value:false}]);
});
