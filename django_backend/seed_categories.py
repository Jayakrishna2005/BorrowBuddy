import os
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
import django
django.setup()
from core.models import Category
categories = ['Stationery', 'Electronics', 'Books', 'Sports']
Category.objects.exclude(name__in=categories).delete()
for name in categories:
    Category.objects.get_or_create(name=name)
print('Categories Seeded and cleaned up')
