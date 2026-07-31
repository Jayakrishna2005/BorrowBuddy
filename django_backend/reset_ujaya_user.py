import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

from core.models import User

email = 'ujaya78901@gmail.com'

try:
    user = User.objects.get(email__iexact=email)
    
    # Reset stats
    user.points = 50
    user.level = 1
    user.trust_score = 0.0
    user.items_lent = 0
    user.items_borrowed = 0
    user.gratitude_count = 0
    user.save()
    
    print(f"Successfully reset stats for {email}:")
    print(f"- Points: {user.points}")
    print(f"- Level: {user.level}")
    print(f"- Trust Score: {user.trust_score}")
    print(f"- Items Lent: {user.items_lent}")
    print(f"- Items Borrowed: {user.items_borrowed}")
    print(f"- Gratitude Count: {user.gratitude_count}")

except User.DoesNotExist:
    print(f"User with email {email} does not exist in the database.")
