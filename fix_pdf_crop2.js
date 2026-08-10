const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    // update app.js styles
    const targetString = "template.style.left = '0px';";
    const replacementString = "template.style.left = '0px';\n    template.style.width = '800px';\n    template.style.transform = 'scale(1)';\n    template.style.transformOrigin = 'top left';";
    
    if (content.includes(targetString)) {
        content = content.replace(targetString, replacementString);
        fs.writeFileSync(file, content);
        console.log("Updated " + file);
    } else {
        console.log("Could not find target string in " + file);
    }
}

updateFile('public/app.js');
updateFile('scolapay-web/public/app.js');
