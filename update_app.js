const fs = require('fs');
let code = fs.readFileSync('public/app.js', 'utf-8');

const replacement = `
    const term = urlParams.get('term') || '1er Trimestre';
    const schoolName = urlParams.get('school') || 'ScolaPay';
    document.getElementById('pdfSchoolName').textContent = schoolName;
    document.getElementById('pdfSchoolLogoInitial').textContent = schoolName.charAt(0).toUpperCase();
`;

code = code.replace("const term = urlParams.get('term') || '1er Trimestre';", replacement);

fs.writeFileSync('public/app.js', code);
fs.writeFileSync('scolapay-web/public/app.js', code);
