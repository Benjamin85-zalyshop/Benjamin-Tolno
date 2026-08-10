const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    // reset previous replacements
    content = content.replace("template.style.position = 'fixed';", "template.style.position = 'absolute';");
    content = content.replace("template.style.visibility = 'hidden';", "");
    
    // new replacements
    content = content.replace("template.style.top = '0';", "template.style.top = window.scrollY + 'px';");
    content = content.replace("template.style.left = '0';", "template.style.left = '0px';\n    template.style.zIndex = '-1';");
    
    // wait, I also need to make sure we don't have multiple zIndex
    fs.writeFileSync(file, content);
}

updateFile('public/app.js');
updateFile('scolapay-web/public/app.js');
