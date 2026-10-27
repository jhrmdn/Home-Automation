const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function load(rows) {
    const source = fs.readFileSync(path.join(__dirname, '../tomcat/webapps/home-automation/js/display.js'), 'utf8');
    const context = {
        uiText: text => text,
        additionalDetailRequest: () => ({name: 'Random Timer'}),
        document: {
            querySelector: selector => rows[selector.slice(1)] ? {} : null,
            querySelectorAll: selector => (rows[selector.slice(1).replace(/ input$/, '')] || []).map(value => ({value: String(value)}))
        }
    };
    vm.createContext(context);
    vm.runInContext(source.match(/^function durationPartsMillis\(id\).*$/m)[0], context);
    const start = source.indexOf('const additionalDetailRequestWithDurationParts=');
    const end = source.indexOf('\n', source.indexOf('additionalDetailRequest=function()', start));
    vm.runInContext(source.slice(start, end), context);
    return context;
}
const rowIds = ['minimum-on', 'maximum-on', 'minimum-off', 'maximum-off'].map(name => 'details-random-' + name + '-parts');
function randomRows(values) { return Object.fromEntries(rowIds.map((id, index) => [id, values[index]])); }

test('saves whole hours, minutes, seconds and milliseconds with zero components', () => {
    const context = load(randomRows([[0,0,0,1], [0,0,1,0], [0,1,0,0], [1,0,0,0]]));
    const result = context.additionalDetailRequest();
    assert.equal(result.minimumOnMillis, 1);
    assert.equal(result.maximumOnMillis, 1000);
    assert.equal(result.minimumOffMillis, 60000);
    assert.equal(result.maximumOffMillis, 3600000);
    assert.equal(load({row: ['1','2','3','4']}).durationPartsMillis('row'), 3723004);
});

test('rejects an all-zero duration in each individual random timer row', () => {
    rowIds.forEach((_, index) => {
        const values = [[0,0,1,0], [0,0,2,0], [0,0,1,0], [0,0,2,0]];
        values[index] = [0,0,0,0];
        assert.throws(() => load(randomRows(values)).additionalDetailRequest(), /durations must be greater than zero/);
    });
});

test('rejects fractional, negative, empty, out-of-range and unsafe durations', () => {
    for (const values of [[0,0,1.5,0], [-1,0,0,1], ['',0,0,1], [0,60,0,0], [0,0,60,0], [0,0,0,1000], [Number.MAX_SAFE_INTEGER,0,0,0]]) {
        assert.throws(() => load({row: values}).durationPartsMillis('row'), /invalid value|too large/);
    }
});

test('keeps the minimum and maximum checks for ON and OFF', () => {
    assert.throws(() => load(randomRows([[0,0,2,0],[0,0,1,0],[0,0,1,0],[0,0,2,0]])).additionalDetailRequest(), /Maximum ON time/);
    assert.throws(() => load(randomRows([[0,0,1,0],[0,0,2,0],[0,0,2,0],[0,0,1,0]])).additionalDetailRequest(), /Maximum OFF time/);
});
