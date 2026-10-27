const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');
const source = fs.readFileSync('tomcat/webapps/home-automation/js/dashboard.js', 'utf8');
function fixture() {
    const original = {id:'value-1',type:'value',componentId:42,x:20,y:40,name:'Temperature',colorRanges:[{minimum:0,color:'#123456'}]};
    const page = {items:[original]};
    let saved = 0, sequence = 1;
    const context = {editingMode:true,authState:{canWrite:true},layout:{pages:[page]},activePage:()=>page,uniqueId:()=>`copy-${sequence++}`,saveLayout:()=>saved++,uiText:text=>text,toast:()=>{}};
    vm.createContext(context);
    vm.runInContext(source.slice(source.indexOf('function copyDashboardItem(')), context);
    return {context,page,original,saved:()=>saved};
}
test('dashboard copies keep source references and independent settings',()=>{
    const f=fixture();f.context.copyDashboardItem(f.original.id);
    const copy=f.page.items[1];assert.notEqual(copy.id,f.original.id);assert.equal(copy.componentId,42);
    assert.equal(copy.x,50);assert.equal(copy.y,70);assert.equal(f.saved(),1);
    copy.colorRanges[0].color='#ffffff';assert.equal(f.original.colorRanges[0].color,'#123456');
    f.context.copyDashboardItem(f.original.id);assert.equal(f.page.items[2].x,80);assert.equal(f.page.items[2].y,100);
});
test('copy requires write permission, edit mode and available item capacity',()=>{
    const f=fixture();f.context.authState.canWrite=false;f.context.copyDashboardItem(f.original.id);
    f.context.authState.canWrite=true;f.context.editingMode=false;f.context.copyDashboardItem(f.original.id);
    assert.equal(f.page.items.length,1);assert.equal(f.saved(),0);
    f.context.editingMode=true;f.context.layout.pages.push({items:Array(499).fill({})});
    f.context.copyDashboardItem(f.original.id);assert.equal(f.page.items.length,1);assert.equal(f.saved(),0);
});
