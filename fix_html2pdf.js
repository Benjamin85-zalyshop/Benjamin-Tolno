const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    content = content.replace("template.style.display = 'block';", "template.style.display = 'block';");
    content = content.replace("template.style.position = 'absolute';", "template.style.position = 'fixed';");
    content = content.replace("template.style.top = '-9999px';", "template.style.top = '0';");
    content = content.replace("template.style.left = '-9999px';", "template.style.left = '0';\n    template.style.zIndex = '-1000';\n    template.style.visibility = 'hidden';");
    fs.writeFileSync(file, content);
}

updateFile('public/app.js');
updateFile('scolapay-web/public/app.js');
