import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

from core.models import User, Booking, Review, Message, Item

print("Deleting all bookings...")
Booking.objects.all().delete()

print("Deleting all reviews...")
Review.objects.all().delete()

print("Deleting all messages...")
Message.objects.all().delete()

print("Resetting all items to available...")
Item.objects.all().update(is_available=True, average_rating=0.0, reviews_count=0)

print("Resetting all users to starting stats...")
User.objects.all().update(
    points=50,
    level=1,
    trust_score=0.0,
    items_lent=0,
    items_borrowed=0,
    gratitude_count=0
)

print("Database reset completed successfully!")
