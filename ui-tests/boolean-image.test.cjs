const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),path=require('node:path');
const file=path.join(__dirname,'../tomcat/webapps/home-automation/js/dashboard-boolean-image.js');
function node(tag='div'){
    const attributes={},classes=new Set();
    const e={tagName:tag.toUpperCase(),children:[],dataset:{},style:{},textContent:'',classList:{toggle(k,on){on?classes.add(k):classes.delete(k)},contains:k=>classes.has(k)},
        append(child){child.parent=this;this.children.push(child)},replaceChildren(){this.children=[];this.textContent=''},querySelector(selector){return this.children.find(c=>selector==='img'?c.tagName==='IMG':c.className===selector.slice(1))||null},setAttribute(k,v){attributes[k]=v},getAttribute:k=>attributes[k],removeAttribute(k){delete attributes[k]},remove(){if(this.parent)this.parent.children=this.parent.children.filter(c=>c!==this)}};
    Object.defineProperty(e,'src',{get:()=>attributes.src,set(value){attributes.src=value;e.srcAssignments=(e.srcAssignments||0)+1}});return e;
}
function load(){
    const fields=new Map(),c={createControlContent(){},updateControlElement(){},editItem(){},handleMessage(){},setConnection(){},uiText:x=>x,dashboardImages:new Map(),pendingDashboardImages:new Set(),send(){},catalog:[{id:1,datatype:'Boolean',name:'Lamp'},{id:2,datatype:'Number'}],values:new Map(),dashboardComponent:()=>({name:'Lamp'}),document:{createElement:node,querySelector:s=>fields.get(s)||{value:'',textContent:''}},authState:{canWrite:true},editingMode:true};
    vm.createContext(c);vm.runInContext(fs.readFileSync(file,'utf8'),c);return{c,fields};
}
test('offers 40 unique color presets and filters Boolean sources',()=>{
    const {c}=load();assert.equal(vm.runInContext('booleanImagePalette.length',c),40);assert.equal(vm.runInContext('new Set(booleanImagePalette).size',c),40);
    assert.equal(c.booleanImageCatalog().length,1);assert.equal(c.booleanImageState({valid:true,value:false}),'false');assert.equal(c.booleanImageState({valid:true,value:'true'}),'true');assert.equal(c.booleanImageState({valid:false,value:'true'}),null);
});
test('switches independent on/off colors and shows unavailable for invalid values',()=>{
    const {c}=load(),box=node(),card=node();box.className='boolean-image-surface';card.append(box);
    const item={componentId:1,trueColor:'#43a047',falseColor:'#e53935'};
    c.values.set(1,{valid:true,value:'true'});c.updateBooleanImage(card,item);assert.equal(box.style.backgroundColor,'#43a047');
    c.values.set(1,{valid:true,value:'false'});c.updateBooleanImage(card,item);assert.equal(box.style.backgroundColor,'#e53935');
    c.values.set(1,{valid:false,value:'true'});c.updateBooleanImage(card,item);assert.equal(box.textContent,'?');assert.equal(card.classList.contains('invalid'),true);
    c.values.set(1,{valid:true,value:'true'});c.updateBooleanImage(card,item);assert.equal(box.textContent,'');assert.equal(card.classList.contains('invalid'),false);
});
test('preserves GIF playback during value polling and uses independent images for each state',()=>{
    const {c}=load(),box=node(),card=node();box.className='boolean-image-surface';card.append(box);
    c.dashboardImages.set('on.gif','data:image/gif;base64,AAAA');c.dashboardImages.set('off.jpg','data:image/jpeg;base64,BBBB');
    const item={componentId:1,trueImageId:'on.gif',falseImageId:'off.jpg',fit:'fill'};
    c.values.set(1,{valid:true,value:'true'});c.updateBooleanImage(card,item);const image=box.querySelector('img');
    c.updateBooleanImage(card,item);assert.equal(image.srcAssignments,1);assert.equal(image.style.objectFit,'fill');
    c.values.set(1,{valid:true,value:'false'});c.updateBooleanImage(card,item);assert.equal(image.src,'data:image/jpeg;base64,BBBB');assert.equal(image.dataset.imageId,'off.jpg');
});
test('requests uncached uploads once and ignores upload replies for a closed editor',()=>{
    const {c}=load(),sent=[];c.send=m=>sent.push(m);const box=node();
    c.paintBooleanImage(box,'picture.gif','#43a047','contain','On');c.paintBooleanImage(box,'picture.gif','#43a047','contain','On');
    assert.deepEqual(sent,['dashboardImageGet:picture.gif']);assert.doesNotThrow(()=>c.acceptBooleanImageUpload({state:'true',session:1,revision:1},{id:'picture.gif'}));
});
test('does not modify the dashboard without write permission',()=>{
    const {c}=load();c.authState.canWrite=false;let called=false;c.saveLayout=()=>called=true;
    c.addBooleanImage();c.saveBooleanImage({preventDefault(){}});assert.equal(called,false);
});

test('Boolean uploads are routed to their state rather than creating standalone image items',()=>{
    const c={handleMessage(){},createControlContent(){},updateControlElement(){},editItem(){},renderCanvas(){},setConnection(){},window:{addEventListener(){}},layout:{pages:[{id:'page',items:[]}]},document:{querySelectorAll:()=>[]},saveLayout(){throw new Error('Unexpected standalone image save')},acceptBooleanImageUpload:(upload,image)=>{c.accepted={upload,image}}};
    vm.createContext(c);vm.runInContext(fs.readFileSync(path.join(__dirname,'../tomcat/webapps/home-automation/js/dashboard-decorations.js'),'utf8'),c);
    vm.runInContext("dashboardImageUpload={kind:'boolean',state:'false',pageId:'page',session:3}",c);
    c.handleMessage({imageUploaded:{id:'off.gif',width:10,height:10}});
    assert.equal(c.accepted.upload.state,'false');assert.equal(c.accepted.image.id,'off.gif');assert.equal(c.layout.pages[0].items.length,0);
    assert.equal(vm.runInContext('dashboardImageUpload',c),null);
});
