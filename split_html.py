with open("public/index.html", "r") as f:
    content = f.read()

# Replace style block
start_style = content.find("<style>")
end_style = content.find("</style>") + 8
if start_style != -1 and end_style != -1:
    content = content[:start_style] + '<link rel="stylesheet" href="style.css">' + content[end_style:]

# Replace script block
start_script = content.find("<script type=\"module\">")
end_script = content.rfind("</script>") + 9
if start_script != -1 and end_script != -1:
    content = content[:start_script] + '<script type="module" src="app.js"></script>' + content[end_script:]

with open("public/index.html", "w") as f:
    f.write(content)
