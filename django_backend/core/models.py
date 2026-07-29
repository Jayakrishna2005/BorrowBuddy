import uuid
from django.db import models

class User(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    registration_number = models.CharField(max_length=20, unique=True)
    full_name = models.CharField(max_length=255)
    email = models.EmailField(unique=True)
    trust_score = models.FloatField(default=0.0)
    items_lent = models.IntegerField(default=0)
    items_borrowed = models.IntegerField(default=0)
    gratitude_count = models.IntegerField(default=0)
    points = models.IntegerField(default=50)
    level = models.IntegerField(default=1)
    profile_photo = models.ImageField(upload_to='profiles/', null=True, blank=True)
    password = models.CharField(max_length=128, default="") # Hashed password
    otp = models.CharField(max_length=6, null=True, blank=True)
    is_email_verified = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    def update_level_based_on_points(self):
        if self.points >= 5000:
            self.level = 5
        elif self.points >= 1500:
            self.level = 4
        elif self.points >= 500:
            self.level = 3
        elif self.points >= 100:
            self.level = 2
        else:
            self.level = 1


class Category(models.Model):
    name = models.CharField(max_length=255)

class Item(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True, blank=True)
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True)
    title = models.CharField(max_length=255, db_index=True)
    description = models.TextField(blank=True, default="")
    condition = models.CharField(max_length=255, blank=True, default="Good")
    image = models.ImageField(upload_to='items/', null=True, blank=True)
    is_available = models.BooleanField(default=True, db_index=True)
    max_borrow_days = models.IntegerField(default=7)
    quantity = models.IntegerField(default=1)
    
    # Cached rating fields for performance
    average_rating = models.FloatField(default=0.0)
    reviews_count = models.IntegerField(default=0)
    
    created_at = models.DateTimeField(auto_now_add=True, db_index=True)

    def update_rating(self):
        reviews = self.reviews.all()
        self.reviews_count = reviews.count()
        if self.reviews_count > 0:
            from django.db.models import Avg
            self.average_rating = reviews.aggregate(models.Avg('rating'))['rating__avg']
        else:
            self.average_rating = 0.0
        self.save()


class Booking(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    item = models.ForeignKey(Item, on_delete=models.CASCADE)
    borrower = models.ForeignKey(User, on_delete=models.CASCADE)
    status = models.CharField(max_length=50, default='PENDING') # PENDING, APPROVED, REJECTED, COMPLETED
    request_date = models.DateTimeField(auto_now_add=True)
    due_date = models.DateTimeField(null=True, blank=True)
    return_date = models.DateTimeField(null=True, blank=True)
    penalty_amount = models.IntegerField(default=0)
    quantity = models.IntegerField(default=1)

    @property
    def item_name(self):
        return self.item.title if self.item else ""

    @property
    def item_image(self):
        if self.item and self.item.image:
            return self.item.image.url
        return None

    @property
    def owner_name(self):
        return self.item.owner.full_name if self.item and self.item.owner else ""

    @property
    def borrower_name(self):
        return self.borrower.full_name if self.borrower else ""

    @property
    def has_review(self):
        return hasattr(self, 'review')

class Message(models.Model):
    booking = models.ForeignKey(Booking, on_delete=models.CASCADE, related_name='messages')
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sent_messages')
    receiver = models.ForeignKey(User, on_delete=models.CASCADE, related_name='received_messages', null=True, blank=True)
    message_text = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=20, default='SENT') # SENT, DELIVERED, SEEN
    is_seen = models.BooleanField(default=False)
    is_typing = models.BooleanField(default=False)

class Review(models.Model):
    booking = models.OneToOneField(Booking, on_delete=models.CASCADE, related_name='review', null=True, blank=True)
    item = models.ForeignKey(Item, on_delete=models.CASCADE, related_name='reviews')
    reviewer = models.ForeignKey(User, on_delete=models.CASCADE)
    rating = models.IntegerField() # 1 to 5
    comment = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
