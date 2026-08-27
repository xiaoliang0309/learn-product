const s = require('fs').readFileSync('index.html', 'utf8');
const m = s.match(/<script>([\s\S]*?)<\/script>/);
if (!m) { console.log('NO script tag found'); process.exit(1); }
const js = m[1];
try {
    new Function(js);
    console.log('JS syntax OK');
} catch (e) {
    console.log('JS syntax ERROR:', e.message);
    // 找错误位置
    const errMatch = e.message.match(/at.*:(\d+)/);
    if (errMatch) {
        const lineNum = parseInt(errMatch[1]);
        const lines = js.split('\n');
        const start = Math.max(0, lineNum - 3);
        const end = Math.min(lines.length, lineNum + 2);
        for (let i = start; i < end; i++) {
            console.log((i + 1) + (i + 1 === lineNum ? ' ← ERROR' : '') + ': ' + lines[i]);
        }
    }
}
