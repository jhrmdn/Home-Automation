const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

test('weekly editor offers day groups and preserves them when reopening and saving', () => {
    const elements = new Map();
    function element() {
        return {children: [], value: '', append(...children) {this.children.push(...children);},
            add(option) {this.children.push(option);}, remove() {this.removed = true;}};
    }
    const context = {
        uiText: value => value,
        Option: function(text, value) {this.text = text; this.value = value;},
        document: {
            createElement: element,
            querySelectorAll(selector) {
                const id = selector.slice(1).split(' ')[0];
                return elements.get(id).children[0].children.filter(row => !row.removed);
            }
        }
    };
    vm.createContext(context);
    const source = fs.readFileSync(path.join(__dirname, '../tomcat/webapps/home-automation/js/display.js'), 'utf8');
    for (const name of ['weeklyScheduleEditor', 'weeklySchedule']) {
        vm.runInContext(source.match(new RegExp('^function ' + name + '\\(.*$', 'm'))[0], context);
    }
    const entries = [
        {day: 'WEEKDAYS', time: '08:00', on: true},
        {day: 'WEEKEND', time: '09:30', on: true},
        {day: 'EVERY_DAY', time: '22:00', on: false},
        {day: 'MONDAY', time: '12:00', on: false}
    ];
    const editor = context.weeklyScheduleEditor('schedule', entries);
    elements.set('schedule', editor);
    const options = editor.children[0].children[0].children[0].children;
    assert.equal(options.length, 10);
    assert.deepEqual(Array.from(options.slice(-3), option => option.value), ['WEEKDAYS', 'WEEKEND', 'EVERY_DAY']);
    assert.deepEqual(JSON.parse(JSON.stringify(context.weeklySchedule('schedule'))), entries);
    editor.children[1].onclick();
    const rows = editor.children[0].children;
    rows[4].children[0].value = 'EVERY_DAY';
    assert.equal(context.weeklySchedule('schedule')[4].day, 'EVERY_DAY');
});

test('next switching point shows date, time, action and disabled state in Automation',()=>{
    const labels={'Switch on':'Ein','Switch off':'Aus','Next switching point':'Nächster Schaltpunkt','disabled':'deaktiviert','No switching times configured':'Keine Schaltpunkte konfiguriert'};
    const context={uiText:text=>labels[text]||text,document:{createElement:()=>({})}};vm.createContext(context);
    vm.runInContext(fs.readFileSync(path.join(__dirname,'../tomcat/webapps/home-automation/js/weekly-timer-display.js'),'utf8'),context);
    const data={weeklyTimer:true,weeklyTimerEnabled:true,nextSwitchTime:'27.10.2026 08:30',nextSwitchOn:true};
    assert.equal(context.weeklyNextSwitchText(data),'27.10.2026 08:30 · Ein');
    assert.equal(context.weeklyNextSwitchText({...data,nextSwitchOn:false,weeklyTimerEnabled:false}),'27.10.2026 08:30 · Aus (deaktiviert)');
    assert.equal(context.weeklyNextSwitchText({nextSwitchTime:null}),'Keine Schaltpunkte konfiguriert');
    let line;const card={querySelector:()=>line,append:node=>line=node};
    context.renderWeeklyNextSwitch(card,data);assert.equal(line.textContent,'Nächster Schaltpunkt: 27.10.2026 08:30 · Ein');
    context.renderWeeklyNextSwitch(card,{...data,nextSwitchTime:'27.10.2026 18:00',nextSwitchOn:false});
    assert.match(line.textContent,/18:00 · Aus$/);
});

test('dashboard text value localizes the next switching point without numeric conversion',()=>{
    const output={},heading={},metric={timerMetric:'nextSwitch',datatype:'String',parentComponentName:'Heating'},data={valid:true,timerMetric:'nextSwitch',nextSwitchTime:'27.10.2026 08:30',nextSwitchOn:true,weeklyTimerEnabled:true,value:'27.10.2026 08:30 · Switch on'};
    const context={uiText:text=>text==='Switch on'?'Ein':text,values:new Map([[-123,data]]),dashboardComponent:()=>metric,isStringComponent:()=>true,dashboardValueColor:()=>''};
    vm.createContext(context);vm.runInContext(fs.readFileSync(path.join(__dirname,'../tomcat/webapps/home-automation/js/weekly-timer-display.js'),'utf8'),context);
    const source=fs.readFileSync(path.join(__dirname,'../tomcat/webapps/home-automation/js/dashboard.js'),'utf8');
    vm.runInContext(source.match(/^function updateValueElement\(.*$/m)[0],context);
    context.updateValueElement({querySelector:selector=>selector==='.dashboard-value'?output:heading,classList:{toggle(){}},style:{}},{componentId:-123});
    assert.equal(output.textContent,'27.10.2026 08:30 · Ein');assert.equal(heading.textContent,'Heating');
});
