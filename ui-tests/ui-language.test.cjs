const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const scripts = path.join(__dirname, '../tomcat/webapps/home-automation/js');

function loadLanguage(page, preferences = {}) {
    const listeners = {}, storage = new Map(Object.entries(preferences));
    const selector = {value: '', addEventListener: (event, callback) => { listeners['select-' + event] = callback; }};
    const context = {
        location: {pathname: '/' + page, reload: () => { context.reloaded = true; }},
        localStorage: {getItem: key => storage.get(key), setItem: (key, value) => storage.set(key, value)},
        NodeFilter: {SHOW_TEXT: 4},
        document: {documentElement: {}, createTreeWalker: () => ({nextNode: () => null}),
            querySelectorAll: () => [], querySelector: () => selector},
        addEventListener: (event, callback) => { listeners[event] = callback; }
    };
    context.window = context;
    vm.createContext(context);
    for (const file of ['ui-translations.js', 'ui-language.js']) vm.runInContext(fs.readFileSync(path.join(scripts, file), 'utf8'), context);
    return {context, listeners, storage, selector};
}

test('preserves existing defaults and validates saved language choices', () => {
    assert.equal(loadLanguage('index.html').context.uiLanguage, 'en');
    assert.equal(loadLanguage('dashboard.html').context.uiLanguage, 'de');
    assert.equal(loadLanguage('dashboard.html', {'home-automation-language-dashboard': 'fr'}).context.uiLanguage, 'de');
});

test('translates static labels, runtime messages and whitespace in both directions', () => {
    const {context: german} = loadLanguage('index.html', {'home-automation-language-automation': 'de'});
    assert.equal(german.uiText('New Device'), 'Neues Gerät');
    assert.equal(german.uiText('Connected'), 'Verbunden');
    assert.equal(german.uiText(' Value '), ' Wert ');
    const {context: english} = loadLanguage('dashboard.html', {'home-automation-language-dashboard': 'en'});
    assert.equal(english.uiText('Überschrift'), 'Heading');
    assert.equal(english.uiText('Bitte mindestens eine Zeiteinheit auswählen.'), 'Please select at least one time unit.');
    assert.equal(english.uiText('Administrator'), 'Administrator');
    assert.equal(english.uiText('Löschen'), 'Delete');
});

test('translates markup labels without changing protocol values or identifiers', () => {
    const {context} = loadLanguage('index.html', {'home-automation-language-automation': 'de'});
    assert.equal(context.uiMarkup('<option value="on">Switch on</option>'), '<option value="on">Einschalten</option>');
    assert.equal(context.uiMarkup('<input id="name" placeholder="New username">'), '<input id="name" placeholder="Neuer Benutzername">');
    assert.equal(context.uiText('Living room lamp'), 'Living room lamp');
    assert.equal(context.uiText('123.456'), '123.456');
});

test('remembers each interface independently when switching languages', () => {
    const {context, listeners, storage, selector} = loadLanguage('index.html', {'home-automation-language-dashboard': 'en'});
    listeners.DOMContentLoaded();
    assert.equal(selector.value, 'en');
    selector.value = 'de'; listeners['select-change']();
    assert.equal(storage.get('home-automation-language-automation'), 'de');
    assert.equal(storage.get('home-automation-language-dashboard'), 'en');
    assert.equal(context.reloaded, true);
});

test('language selection works with unavailable storage at startup', () => {
    const source = fs.readFileSync(path.join(scripts, 'ui-language.js'), 'utf8');
    const {context} = loadLanguage('index.html');
    context.localStorage.getItem = () => { throw new Error('Storage disabled'); };
    assert.doesNotThrow(() => vm.runInContext(source, context));
    assert.equal(context.uiLanguage, 'en');
});
