import os
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
import django
django.setup()
from core.models import Category
categories = ['Electronics', 'Stationery', 'Books', 'Tools', 'Fashion']
for name in categories:
    Category.objects.get_or_create(name=name)
print('Categories Seeded')
