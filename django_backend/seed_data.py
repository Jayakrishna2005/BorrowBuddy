import os
import django
from django.contrib.auth.hashers import make_password

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

from core.models import User, Category, Item

# Seed categories
categories = ['Stationery', 'Electronics', 'Books', 'Sports']
cat_objs = {}
for name in categories:
    cat_objs[name], _ = Category.objects.get_or_create(name=name)

# Seed users
users_data = [
    {
        'email': 'alice@gmail.com',
        'full_name': 'Alice Johnson',
        'registration_number': '190000001',
        'password': 'password',
        'is_email_verified': True,
        'points': 100,
        'trust_score': 95.0,
    },
    {
        'email': 'bob@gmail.com',
        'full_name': 'Bob Smith',
        'registration_number': '190000002',
        'password': 'password',
        'is_email_verified': True,
        'points': 80,
        'trust_score': 88.0,
    }
]

user_objs = {}
for u_data in users_data:
    user, created = User.objects.get_or_create(
        email=u_data['email'],
        defaults={
            'full_name': u_data['full_name'],
            'registration_number': u_data['registration_number'],
            'password': make_password(u_data['password']),
            'is_email_verified': u_data['is_email_verified'],
            'points': u_data['points'],
            'trust_score': u_data['trust_score']
        }
    )
    user_objs[user.email] = user
    if not created:
        user.password = make_password(u_data['password'])
        user.is_email_verified = True
        user.save()

# Seed items
items_data = [
    {
        'owner': 'alice@gmail.com',
        'category': 'Electronics',
        'title': 'Scientific Calculator',
        'description': 'Casio fx-991EX scientific calculator in great condition. Perfect for exams.',
        'condition': 'Like New',
        'quantity': 1,
        'max_borrow_days': 5,
    },
    {
        'owner': 'alice@gmail.com',
        'category': 'Books',
        'title': 'Calculus: Early Transcendentals',
        'description': 'Stewart Calculus textbook, 8th edition. Useful for Engineering Math courses.',
        'condition': 'Good',
        'quantity': 1,
        'max_borrow_days': 14,
    },
    {
        'owner': 'bob@gmail.com',
        'category': 'Sports',
        'title': 'Badminton Racket',
        'description': 'Yonex badminton racket. Comes with a carrying cover.',
        'condition': 'Fair',
        'quantity': 2,
        'max_borrow_days': 3,
    }
]

for i_data in items_data:
    Item.objects.get_or_create(
        owner=user_objs[i_data['owner']],
        title=i_data['title'],
        defaults={
            'category': cat_objs[i_data['category']],
            'description': i_data['description'],
            'condition': i_data['condition'],
            'quantity': i_data['quantity'],
            'max_borrow_days': i_data['max_borrow_days']
        }
    )

print("Seed data loaded successfully!")
