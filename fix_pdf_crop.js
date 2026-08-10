const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    const targetString = "html2canvas: { scale: 2, useCORS: true, scrollY: 0, windowWidth: 1000 },";
    const replacementString = "html2canvas: { scale: 2, useCORS: true, scrollY: 0, scrollX: 0, windowWidth: 800 },";
    
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
