const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const test=require('node:test');
const source=fs.readFileSync('tomcat/webapps/home-automation/js/display.js','utf8');
function fixture(){
    const sent=[];
    function node(){return {children:[],dataset:{},events:{},classList:{toggle(){}},append(child){this.children.push(child)},replaceChildren(){this.children=[]},querySelector(selector){return this.children.find(child=>child.className===selector.slice(1))},querySelectorAll(){return this.children},addEventListener(name,callback){this.events[name]=callback}}}
    const box=node(),card={dataset:{id:'351'},querySelector:()=>box};
    const context={document:{createElement:node},uiText:s=>s,authState:{canAct:true},send:s=>sent.push(JSON.parse(s.slice(s.indexOf(':')+1)))};
    vm.createContext(context);
    let start=source.indexOf('function renderCoverControl(');vm.runInContext(source.slice(start,source.indexOf('\n}',start)+2),context);
    start=source.indexOf('function renderWebControl(');vm.runInContext(source.slice(start,source.indexOf('\n',start)),context);
    const item={id:351,coverControl:true,coverState:'stopped'};
    context.renderCoverControl(card,item);
    return {context,box,card,item,sent};
}
test('all cover buttons survive status updates and send their own commands',()=>{
    const f=fixture(),controls=f.box.children[0],buttons=controls.children.slice();
    f.context.renderWebControl(f.card,f.item);f.context.renderCoverControl(f.card,{...f.item,coverState:'opening'});
    assert.equal(f.box.children[0],controls);
    for(let i=0;i<3;i++){
        assert.equal(controls.children[i],buttons[i]);buttons[i].onclick({stopPropagation(){}});
    }
    assert.deepEqual(f.sent.map(request=>request.command),['open','stop','close']);
    assert.ok(f.sent.every(request=>request.id===351));
});
test('cover buttons stop drag events and respect action permissions',()=>{
    const f=fixture();let stopped=0;
    for(const button of f.box.children[0].children){
        button.events.pointerdown({stopPropagation(){stopped++}});button.events.pointerup({stopPropagation(){stopped++}});
    }
    assert.equal(stopped,6);f.context.authState.canAct=false;
    f.context.renderCoverControl(f.card,f.item);
    for(const button of f.box.children[0].children){assert.equal(button.disabled,true);button.onclick({stopPropagation(){}})}
    assert.equal(f.sent.length,0);
});
