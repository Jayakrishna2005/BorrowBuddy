import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

from core.models import User

users = User.objects.all()
for u in users:
    print(f"User: {u.email}, Verified: {u.is_email_verified}, Has Password: {bool(u.password)}, RegNo: {u.registration_number}")
