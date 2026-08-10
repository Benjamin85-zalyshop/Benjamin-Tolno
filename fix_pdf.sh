sed -i 's/<div style="width: 60px; height: 60px; background: white; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-weight: bold; color: #0047FF; font-size: 24px;">/<div id="pdfSchoolLogoInitial" style="width: 60px; height: 60px; background: white; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-weight: bold; color: #0047FF; font-size: 24px;">/' public/index.html

sed -i 's/Z<\/div>/S<\/div>/' public/index.html

sed -i 's/<h1 style="margin: 0; font-size: 24px; font-weight: bold;">SALOME<\/h1>/<h1 id="pdfSchoolName" style="margin: 0; font-size: 24px; font-weight: bold;">ScolaPay<\/h1>/' public/index.html

sed -i "s/<p style=\"margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;\">N'Zerekore • 625667522<\/p>/<p id=\"pdfSchoolContact\" style=\"margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;\">Conakry<\/p>/" public/index.html

