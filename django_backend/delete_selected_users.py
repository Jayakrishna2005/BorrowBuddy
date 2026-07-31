import os
import django
from django.db.models import Q

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

from core.models import User

# Targets to delete
delete_names = ['ram', 'jai', 'sandeep']
except_email = 'ujaya78901@gmail.com'

# Build a case-insensitive lookup
query = Q()
for name in delete_names:
    query |= Q(full_name__icontains=name) | Q(email__icontains=name)

# Find and exclude exceptions
users_to_delete = User.objects.filter(query).exclude(email__iexact=except_email)

print(f"Found {users_to_delete.count()} users matching targets (ram, jai, sandeep) to delete:")
for u in users_to_delete:
    print(f"- Name: {u.full_name}, Email: {u.email}, RegNo: {u.registration_number}")

# Perform the deletion
if users_to_delete.exists():
    deleted_count, _ = users_to_delete.delete()
    print(f"\nSuccessfully deleted {deleted_count} database records.")
else:
    print("\nNo matching users found to delete.")
