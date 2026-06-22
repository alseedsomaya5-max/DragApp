import os

files_to_include = [
    ('DALAppWriteConnection.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/DALAppWriteConnection.java', 'الطبقة المسؤولة عن الاتصال بقاعدة بيانات Appwrite وإدارة العمليات (حفظ، جلب، حذف، رفع ملفات).'),
    ('User.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/model/User.java', 'نموذج بيانات المستخدم والمريض، يحتوي على الحقول الأساسية مثل الاسم، الهاتف، وتاريخ الميلاد.'),
    ('Medication.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/model/Medication.java', 'نموذج بيانات الدواء، يربط الدواء بالمريض ويحتوي على تفاصيل الجرعة والوقت وصورة الدواء.'),
    ('MainActivity.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/MainActivity.java', 'النشاط الرئيسي للتطبيق، يدير التنقل بين الشاشات وتحديث شريط العناوين العلوي.'),
    ('HomeFragment.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/ui/HomeFragment.java', 'الشاشة الرئيسية التي تعرض قائمة الأشخاص الذين يتلقون الدواء مع إمكانية حذفهم.'),
    ('AddMedicationFragment.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/ui/AddMedicationFragment.java', 'شاشة إضافة دواء جديد، تدعم التقاط صورة للدواء واختيار وقت التنبيه.'),
    ('AddPatientFragment.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/ui/AddPatientFragment.java', 'شاشة إضافة مريض جديد، تدعم اختيار تاريخ الميلاد عبر منقي التاريخ (DatePicker).'),
    ('RemindersFragment.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/ui/RemindersFragment.java', 'شاشة تعرض جميع التنبيهات والأدوية المجدولة لجميع المرضى في مكان واحد.'),
    ('RegisterUserFragment.java', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/java/com/example/dragapp/ui/RegisterUserFragment.java', 'شاشة تسجيل حساب مستخدم جديد أو تعديل بيانات الملف الشخصي الحالي.'),
    ('strings.xml', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/res/values/strings.xml', 'ملف الموارد النصية، يحتوي على جميع النصوص المترجمة للغة العربية المستخدمة في الواجهات.'),
    ('item_patient.xml', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/res/layout/item_patient.xml', 'تصميم العنصر الفردي في قائمة المرضى، يحتوي على الاسم وزر الحذف.'),
    ('item_medication.xml', 'C:/Users/USER/AndroidStudioProjects/DragApp/app/src/main/res/layout/item_medication.xml', 'تصميم العنصر الفردي في قائمة الأدوية، يعرض صورة الدواء وتفاصيل الجرعة.')
]

html_content = '''
<html>
<head>
<meta charset='utf-8'>
<style>
    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; padding: 40px; }
    .header { text-align: center; color: #008B8B; border-bottom: 2px solid #008B8B; padding-bottom: 20px; margin-bottom: 40px; }
    .file-section { margin-bottom: 50px; page-break-inside: avoid; border: 1px solid #eee; padding: 20px; border-radius: 10px; }
    .description { direction: rtl; text-align: right; background: #f0fafa; padding: 15px; border-right: 5px solid #008B8B; margin-bottom: 15px; font-weight: bold; font-size: 16px; }
    .code-block { direction: ltr; text-align: left; background: #f4f4f4; color: #333; padding: 15px; border: 1px solid #ddd; border-radius: 5px; font-family: 'Consolas', 'Courier New', monospace; font-size: 10pt; white-space: pre-wrap; word-wrap: break-word; }
    .file-name { color: #006666; font-size: 22px; margin-bottom: 10px; font-weight: bold; border-bottom: 1px solid #ddd; padding-bottom: 5px; }
</style>
</head>
<body>
    <div class='header'>
        <h1>توثيق مشروع تطبيق منبه الأدوية (DragApp)</h1>
        <p>دليل الأكواد البرمجية والواجهات</p>
    </div>
'''

for name, path, desc in files_to_include:
    if os.path.exists(path):
        try:
            with open(path, 'r', encoding='utf-8') as f:
                code = f.read()
                html_content += f'''
                <div class='file-section'>
                    <div class='file-name'>{name}</div>
                    <div class='description'>{desc}</div>
                    <div class='code-block'>{code.replace('<', '&lt;').replace('>', '&gt;')}</div>
                </div>
                '''
        except Exception as e:
            print(f"Error reading {name}: {e}")

html_content += '''
</body>
</html>
'''

output_path = 'C:/Users/USER/AndroidStudioProjects/DragApp/DragApp_Documentation.html'
with open(output_path, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f'Done! File saved at: {output_path}')
