const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');
const source = fs.readFileSync('tomcat/webapps/home-automation/js/dashboard-resize.js', 'utf8');
const context = {renderCanvas:()=>{}};
vm.createContext(context);
vm.runInContext(source, context);
const element = {getBoundingClientRect:()=>({left:100,top:50,width:200,height:100})};
const direction = (x,y,pointerType) => context.dashboardResizeDirection(element,{clientX:100+x,clientY:50+y,pointerType});

test('touch can grab wider edges and corners without changing mouse hit areas',()=>{
    assert.equal(direction(185,50,'touch'),'e');
    assert.equal(direction(185,50,'mouse'),'');
    assert.equal(direction(185,80,'touch'),'se');
    assert.equal(direction(15,20,'touch'),'nw');
    assert.equal(direction(100,50,'touch'),'');
    assert.equal(direction(199,50,'mouse'),'e');
    assert.equal(direction(-1,50,'touch'),'');
});

test('touch hit areas leave the center of small components available',()=>{
    const small = {getBoundingClientRect:()=>({left:0,top:0,width:20,height:20})};
    assert.equal(context.dashboardResizeDirection(small,{clientX:10,clientY:10,pointerType:'touch'}),'');
    assert.equal(context.dashboardResizeDirection(small,{clientX:19,clientY:19,pointerType:'touch'}),'se');
});
