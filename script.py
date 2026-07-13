import os

source_file = r'd:\PRJ\TestAsg_SU26\web\schedule_management.jsp'
target_file = r'd:\PRJ\Asg_School_Bus\web\schedule_management.jsp'

with open(source_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace light classes with dark theme classes
content = content.replace('<body class="bg-light">', '<body data-bs-theme="dark" style="background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%); color: #fff; font-family: \'Inter\', sans-serif;">\n<div class="bg-image" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: url(\'img/urban_bus_bg.png\') no-repeat center center; background-size: cover; z-index: -2; opacity: 0.2;"></div>\n<div class="bg-overlay" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.6); z-index: -1;"></div>')
content = content.replace('bg-white', 'bg-dark text-white')
content = content.replace('bg-light', 'bg-dark text-white')
content = content.replace('table-light', 'table-dark')
content = content.replace('<nav class="navbar navbar-dark bg-dark mb-4">', '<nav class="navbar navbar-dark mb-4" style="background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255,255,255,0.1);">')
content = content.replace('card shadow-sm', 'card shadow-sm bg-dark text-white border-secondary')
content = content.replace('card-body bg-light', 'card-body bg-dark')
content = content.replace('text-dark', 'text-white')

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print('Updated schedule_management.jsp')
